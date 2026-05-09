package cn.dreamtof.audit.core;

import cn.dreamtof.audit.annotation.AuditReference;
import cn.dreamtof.audit.cache.AuditBeanMeta;
import cn.dreamtof.audit.cache.AuditMetaCache;
import cn.dreamtof.audit.enums.AuditField;
import cn.dreamtof.audit.utils.IdExtractor;
import cn.dreamtof.core.context.OperationContext;
import cn.dreamtof.core.utils.FastBeanMeta;
import cn.dreamtof.core.utils.JsonUtils;
import cn.dreamtof.log.core.LogMarkers;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cn.dreamtof.log.core.LogMarkers.AUDIT;

/**
 * ⚡ SmartAuditUpdater: 极速差异审计引擎
 * <p>
 * 优化点：改用 AuditBeanMeta 预取的 Accessor 进行字段比对，彻底消除 compare 过程中的反射和 Label 查找开销。
 */
@Slf4j
public class SmartAuditUpdater<S, T> {

    private final S source; // 旧对象
    private final T target; // 新对象

    private boolean isPartial;
    private boolean skipNullFields;
    private boolean includeSystemFields;
    private AuditTransactionContext.OperationType operationType;
    private final Set<String> manualIgnoreFields = new HashSet<>(Arrays.asList(
            "serialVersionUID", "updateTime", "editTime", "modifyTime", "createTime"
    ));
    private final List<Map<String, Object>> changes = new ArrayList<>();
    private Map<String, String> translationMap = Collections.emptyMap();

    private SmartAuditUpdater(S source, T target) {
        this.source = source;
        this.target = target;
    }

    public static <S, T> SmartAuditUpdater<S, T> copy(S source, T target) {
        return new SmartAuditUpdater<>(source, target);
    }

    public SmartAuditUpdater<S, T> partial(boolean partial) { this.isPartial = partial; return this; }
    public SmartAuditUpdater<S, T> skipNullFields(boolean skip) { this.skipNullFields = skip; return this; }
    public SmartAuditUpdater<S, T> includeSystemFields(boolean include) { this.includeSystemFields = include; return this; }
    private SmartAuditUpdater<S, T> operationType(AuditTransactionContext.OperationType type) {
        this.operationType = type;
        return this;
    }

    /**
     * 批量处理异步审计记录
     * * @param payload 审计上下文载体
     * @param cost 业务耗时
     */
    public static void processAsyncRecords(AuditTransactionContext.ContextPayload payload, long cost) {
        List<SmartAuditUpdater<?, ?>> updaters = new ArrayList<>();
        for (AuditTransactionContext.Record record : payload.getRecords()) {
            if (record.getOldState() == null && record.getNewState() == null) continue;
            if (record.getType() == AuditTransactionContext.OperationType.INSERT && record.getNewState() == null) continue;

            updaters.add(SmartAuditUpdater.copy(record.getOldState(), record.getNewState())
                    .partial(payload.isPartial())
                    .skipNullFields(payload.isSkipNull())
                    .operationType(record.getType())
                    .includeSystemFields(payload.isSystemFields()));
        }
        executeBatch(payload.getModule(), payload.getAction(), updaters, cost);
    }

    /**
     * 执行批量比对与投递
     */
    private static void executeBatch(String module, String action, List<SmartAuditUpdater<?, ?>> updaters, long cost) {
        if (CollectionUtils.isEmpty(updaters)) return;

        Map<String, String> globalTranslations = Collections.emptyMap();
        try {
            // 预先抓取所有需要翻译的外键 Label
            globalTranslations = preFetchTranslations(updaters);
        } catch (Exception e) {
            log.warn(AUDIT, "[自动审计] 字典翻译预抓取失败: {}", e.getMessage());
        }

        OperationContext.OperationInfo ctx = OperationContext.get();
        List<AuditResult> results = new ArrayList<>();

        for (var updater : updaters) {
            updater.translationMap = globalTranslations;
            AuditResult result = updater.compare(action);
            if (result != null) results.add(result);
        }

        if (results.isEmpty()) return;
        if (results.size() == 1) {
            logSingleAudit(ctx, module, action, cost, results.get(0));
        } else {
            logBatchAudit(ctx, module, action, cost, results);
        }
    }

    /**
     * 核心差异比对引擎
     * 作用：对比新旧快照，生成人类可读的变更记录。
     * 修复：1. 增加了对逻辑删除字段的特殊识别；2. 实现了集合顺序变化的溯源。
     */
    private AuditResult compare(String action) {
        Object activeObj = target != null ? target : source;
        if (activeObj == null) return null;

        Class<?> clazz = activeObj.getClass();
        // 基础操作类型判定
        String operation = target == null ? "DELETE" : (source == null ? "CREATE" : "UPDATE");

        // 1. 简略模式直接返回
        if (isPartial) {
            AuditResult res = new AuditResult();
            res.entity = clazz.getSimpleName();
            res.entityId = IdExtractor.extractId(activeObj);
            res.operation = operation;
            res.description = String.format("%s%s(ID:%s) [简略模式]",
                    (target == null ? "删除" : (source == null ? "创建" : "更新")),
                    AuditMetaCache.getDisplayName(clazz), res.entityId);
            res.changes = Collections.emptyList();
            return res;
        }

        // 2. 获取预编译审计元数据 (已整合 FastBeanMeta)
        AuditBeanMeta meta = AuditBeanMeta.of(clazz);
        AuditResult res = new AuditResult();
        res.entity = clazz.getSimpleName();
        res.entityId = IdExtractor.extractId(activeObj);

        // 3. 极速比对循环
        for (AuditBeanMeta.AuditField auditField : meta.getFields()) {
            try {
                // 通过 Lambda 访问器直接拿值，避免反射
                Object oldVal = source != null ? auditField.getValue(source) : null;
                Object newVal = target != null ? auditField.getValue(target) : null;

                // 溯源核心：Objects.deepEquals 对于 List/数组 会校验顺序
                if (Objects.deepEquals(oldVal, newVal)) continue;

                Field field = auditField.accessor().field();
                // 过滤不需要记录的字段
                if (shouldIgnoreField(field, newVal)) continue;

                // 4. 逻辑删除特殊识别：如果标记为逻辑删除的字段变为“删除态”，强制将操作标记为 DELETE
                Column column = field.getAnnotation(Column.class);
                if (column != null && column.isLogicDelete()) {
                    // 约定：1 或 true 代表已删除
                    if (Objects.equals(newVal, 1) || Objects.equals(newVal, true) || "1".equals(newVal)) {
                        operation = "DELETE";
                    }
                }

                // 5. 记录变更内容
                recordChange(auditField.getFieldName(), auditField.label(),
                        tryLookupTranslation(oldVal, field),
                        tryLookupTranslation(newVal, field));
            } catch (Exception e) {
                log.warn(LogMarkers.AUDIT, "[自动审计] 比对失败: {}#{}", clazz.getSimpleName(), auditField.getFieldName());
            }
        }

        // 如果没有任何变更且不是创建/删除，则不记录
        if (changes.isEmpty() && source != null && target != null) return null;

        res.setOperation(operation);
        res.setDescription(buildHumanReadableDescription(action, clazz, activeObj, changes));
        res.setChanges(changes);
        return res;
    }

    /**
     * 完整修复：字段过滤逻辑
     * 调整：逻辑删除字段不再忽略，确保“删除溯源”不丢失
     */
    private boolean shouldIgnoreField(Field field, Object newVal) {
        if (manualIgnoreFields.contains(field.getName())) return true;

        Column column = field.getAnnotation(Column.class);
        if (column != null) {
            // 【核心修复】：不再忽略逻辑删除字段 (column.isLogicDelete())
            // 只有明确标记 ignore=true 或版本号字段才忽略
            if (column.ignore() || column.version()) return true;

            boolean isSystemField = StringUtils.isNotEmpty(column.onInsertValue()) || StringUtils.isNotEmpty(column.onUpdateValue());
            if (isSystemField && !includeSystemFields) return true;
        }

        if (skipNullFields && newVal == null) return true;
        return field.isAnnotationPresent(Id.class) && skipNullFields;
    }
    /**
     * 外键 ID 批量翻译预处理
     */
    private static Map<String, String> preFetchTranslations(List<SmartAuditUpdater<?, ?>> updaters) {
        Map<String, Map<String, Set<Object>>> dsRequestMap = new HashMap<>();

        for (var updater : updaters) {
            Object activeTarget = updater.target != null ? updater.target : updater.source;
            if (activeTarget == null) continue;

            // 获取类中所有标记了 @AuditReference 的字段
            FastBeanMeta meta = FastBeanMeta.of(activeTarget.getClass());
            for (FastBeanMeta.FieldAccessor acc : meta.getWriteableAccessors()) {
                AuditReference ref = acc.field().getAnnotation(AuditReference.class);
                if (ref != null && ref.target() != void.class) {
                    try {
                        Object v1 = updater.source != null ? acc.getter().apply(updater.source) : null;
                        Object v2 = updater.target != null ? acc.getter().apply(updater.target) : null;
                        if (v1 == null && v2 == null) continue;

                        TableInfo targetInfo = TableInfoFactory.ofEntityClass(ref.target());
                        if (targetInfo == null) continue;

                        String dsKey = targetInfo.getDataSource();
                        String key = String.format("%s:%s:%s", ref.target().getName(), ref.label(), ref.idCol());

                        Set<Object> idSet = dsRequestMap
                                .computeIfAbsent(dsKey, k -> new HashMap<>())
                                .computeIfAbsent(key, x -> new HashSet<>());

                        if (v1 != null) extractAndAddIds(idSet, v1);
                        if (v2 != null) extractAndAddIds(idSet, v2);
                    } catch (Exception ignore) {}
                }
            }
        }

        if (dsRequestMap.isEmpty()) return Collections.emptyMap();
        Map<String, String> finalResults = new ConcurrentHashMap<>();

        // 跨数据源/分表查询逻辑
        dsRequestMap.forEach((dsKey, requestMap) -> {
            requestMap.forEach((gKey, idSet) -> {
                String[] parts = gKey.split(":");
                Class<?> targetClass;
                try { targetClass = Class.forName(parts[0]); } catch (Exception e) { return; }

                TableInfo info = TableInfoFactory.ofEntityClass(targetClass);
                String labelCol = info.getColumnByProperty(parts[1]);
                if (StringUtils.isBlank(labelCol)) labelCol = parts[1];
                String idCol = parts[2];
                String tableName = info.getTableName();

                List<Object> allIds = new ArrayList<>(idSet);
                final int BATCH_SIZE = 50;

                for (int i = 0; i < allIds.size(); i += BATCH_SIZE) {
                    List<Object> batchIds = allIds.subList(i, Math.min(i + BATCH_SIZE, allIds.size()));
                    String previousDs = DataSourceKey.get();
                    try {
                        if (dsKey != null) DataSourceKey.use(dsKey);
                        QueryWrapper wrapper = QueryWrapper.create()
                                .select("'" + gKey + "' as _g", idCol + " as _i", labelCol + " as _v")
                                .from(tableName)
                                .where(new QueryColumn(idCol).in(batchIds));

                        List<Row> rows = Db.selectListByQuery(wrapper);
                        if (rows != null) {
                            for (Row row : rows) {
                                finalResults.put(row.getString("_g") + ":" + row.getString("_i"), row.getString("_v"));
                            }
                        }
                    } finally {
                        if (previousDs != null) DataSourceKey.use(previousDs);
                    }
                }
            });
        });
        return finalResults;
    }

    private Object tryLookupTranslation(Object value, Field field) {
        if (value == null) return null;
        AuditReference ref = field.getAnnotation(AuditReference.class);
        if (ref == null || ref.target() == void.class) return value;

        if (value instanceof Collection<?> coll) {
            return coll.stream().map(v -> tryLookupTranslation(v, field)).collect(Collectors.toList());
        }

        String key = String.format("%s:%s:%s:%s", ref.target().getName(), ref.label(), ref.idCol(), value);
        return translationMap.getOrDefault(key, value + "(未知)");
    }

    private static void extractAndAddIds(Set<Object> idSet, Object val) {
        if (val instanceof Collection<?> coll) idSet.addAll(coll);
        else idSet.add(val);
    }

    private void recordChange(String p, String n, Object o, Object v) {
        Map<String, Object> m = new HashMap<>();
        m.put("field", p); m.put("name", n); m.put("old", o); m.put("new", v);
        changes.add(m);
    }

    private String buildHumanReadableDescription(String action, Class<?> clazz, Object activeObj, List<Map<String, Object>> changes) {
        StringBuilder desc = new StringBuilder();
        desc.append(target == null ? "删除" : (source == null ? "创建" : "更新")).append(AuditMetaCache.getDisplayName(clazz));
        Object id = IdExtractor.extractId(activeObj);
        if (id != null) desc.append("(ID:").append(id).append(")");
        desc.append("：");

        if (changes.isEmpty()) {
            desc.append("无内容变更");
        } else {
            desc.append(changes.stream().map(c -> {
                String fName = (String) c.get("name");
                Object oVal = c.get("old"), nVal = c.get("new");
                if (source == null) return String.format("【%s】设置为\"%s\"", fName, nVal);
                if (target == null) return String.format("【%s】原值为\"%s\"", fName, oVal);
                if (oVal == null) return String.format("【%s】设置为\"%s\"", fName, nVal);
                if (nVal == null) return String.format("【%s】被置空(原值\"%s\")", fName, oVal);
                return String.format("【%s】从\"%s\"改为\"%s\"", fName, oVal, nVal);
            }).collect(Collectors.joining("；")));
        }
        return desc.toString();
    }

    private static void logSingleAudit(OperationContext.OperationInfo ctx, String module, String action, long cost, AuditResult res) {
        try {
            Map<String, Object> logPacket = buildBaseLogPacket(ctx, module, action, cost);
            logPacket.put(AuditField.ENTITY.getValue(), res.entity);
            logPacket.put(AuditField.ENTITY_ID.getValue(), res.entityId);
            logPacket.put(AuditField.OPERATION.getValue(), res.operation);
            logPacket.put(AuditField.DESCRIPTION.getValue(), res.description);
            logPacket.put(AuditField.CHANGES.getValue(), res.changes);
            log.info(LogMarkers.BUSINESS_AUDIT, "{}", JsonUtils.toJsonString(logPacket));
        } catch (Exception e) {
            log.error(AUDIT,"[自动审计] 日志序列化失败", e);
        }
    }

    private static void logBatchAudit(OperationContext.OperationInfo ctx, String module, String action, long cost, List<AuditResult> results) {
        try {
            Map<String, Object> logPacket = buildBaseLogPacket(ctx, module, action, cost);
            logPacket.put(AuditField.ENTITY.getValue(), "批量操作(" + results.stream().map(AuditResult::getEntity).distinct().collect(Collectors.joining(",")) + ")");
            logPacket.put(AuditField.ENTITY_ID.getValue(), "多个主键");
            logPacket.put(AuditField.OPERATION.getValue(), "BATCH");

            StringBuilder descBuilder = new StringBuilder();
            descBuilder.append(StringUtils.isNotBlank(action) ? action : "批量操作").append(" (共").append(results.size()).append("项变更)：\n");

            List<Map<String, Object>> allChanges = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                AuditResult res = results.get(i);
                descBuilder.append("(").append(i + 1).append(") ").append(res.description).append(i < results.size() - 1 ? "\n" : "");
                for (Map<String, Object> change : res.changes) {
                    Map<String, Object> enrichedChange = new LinkedHashMap<>(change);
                    enrichedChange.put("_entity", res.entity);
                    enrichedChange.put("_id", res.entityId);
                    allChanges.add(enrichedChange);
                }
            }
            logPacket.put(AuditField.DESCRIPTION.getValue(), descBuilder.toString());
            logPacket.put(AuditField.CHANGES.getValue(), allChanges);
            log.info(LogMarkers.BUSINESS_AUDIT, "{}", JsonUtils.toJsonString(logPacket));
        } catch (Exception e) {
            log.error(AUDIT,"[自动审计] 批量审计日志处理失败", e);
        }
    }

    private static Map<String, Object> buildBaseLogPacket(OperationContext.OperationInfo ctx, String module, String action, long cost) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(AuditField.TRACE_ID.getValue(), ctx.getTraceId());
        m.put(AuditField.OPERATOR_ID.getValue(), ctx.getOperatorId());
        m.put(AuditField.OPERATOR_NAME.getValue(), ctx.getOperatorName());
        m.put(AuditField.MODULE.getValue(), StringUtils.isNotBlank(module) ? module : ctx.getBusinessModule());
        m.put(AuditField.ACTION.getValue(), StringUtils.isNotBlank(action) ? action : ctx.getBusinessAction());
        m.put(AuditField.COST_TIME.getValue(), cost);
        m.put(AuditField.TIMESTAMP.getValue(), System.currentTimeMillis());
        return m;
    }

    @Data
    private static class AuditResult {
        String entity;
        Object entityId;
        String description;
        List<Map<String, Object>> changes;
        String operation;
    }
}