package cn.dreamtof.audit.aspect;

import cn.dreamtof.audit.annotation.AuditEntityId;
import cn.dreamtof.audit.annotation.AuditLog;
import cn.dreamtof.audit.config.AutoAuditProperties;
import cn.dreamtof.audit.utils.IdExtractor;
import cn.dreamtof.audit.utils.QueryHelper;
import cn.dreamtof.audit.core.AuditTransactionContext;
import cn.dreamtof.audit.core.SmartAuditUpdater;
import cn.dreamtof.core.config.VirtualTaskManager;
import cn.dreamtof.core.context.ContextPropagator;
import cn.dreamtof.core.utils.FastBeanMeta;
import cn.dreamtof.core.utils.FastReflectUtils;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static cn.dreamtof.log.core.LogMarkers.AUDIT;

/**
 * 👑 自动审计切面 (极速进化版)
 * <p>
 * 优化点：
 * 1. DTO 嗅探使用 FastBeanMeta Accessor 避免反射耗时。
 * 2. deepClone 深度拷贝利用 Lambda 实例化和属性迁移，性能极大提升。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(2)
@ConditionalOnProperty(prefix = "dreamtof.auto-audit", name = "enabled", havingValue = "true")
public class AutoAuditAspect {

    private final AutoAuditProperties properties;

    /**
     * 核心生命周期管理
     */
    @Around("@annotation(auditLog)")
    public Object doAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        if (!properties.isEnabled()) return joinPoint.proceed();

        // 处理嵌套调用，共享上下文
        AuditTransactionContext.ContextPayload existingPayload = AuditTransactionContext.get();
        if (existingPayload != null) {
            Class<?>[] innerEntities = resolveEntities(auditLog);
            if (innerEntities.length > 0) {
                existingPayload.getAllowedEntities().addAll(Arrays.asList(innerEntities));
            }
            return joinPoint.proceed();
        }

        AuditTransactionContext.ContextPayload payload = getContextPayload(auditLog);

        // 使用 Java 21 ScopedValue 传递上下文，性能优于 ThreadLocal
        return ScopedValue.where(AuditTransactionContext.CONTEXT, payload).call(() -> {

            // 1. 预抓取 Snapshot：识别变更前的旧值
            List<AuditTarget> targets = identify(payload.getAllowedEntities(), joinPoint.getArgs());
            Map<Class<?>, Map<Serializable, Object>> oldSnapshots = fetchBatch(targets);

            oldSnapshots.forEach((clazz, idMap) ->
                    idMap.forEach((id, entity) -> {
                        // 【性能点】：使用高性能深拷贝解耦 MyBatis 缓存引用
                        Object detachedOldState = FastReflectUtils.deepCopy(entity);
                        payload.getPrefetchedOldStates().put(clazz.getName() + ":" + id, detachedOldState);
                                    // 添加这一行：标记 snapshotCache，防止重复
            AuditTransactionContext.hasSnapshot(clazz, id);
                        AuditTransactionContext.addRecord(new AuditTransactionContext.Record(
                                clazz, id, detachedOldState, null, AuditTransactionContext.OperationType.UPDATE
                        ));
                    }));

            long start = System.currentTimeMillis();
            try {
                // 执行业务逻辑
                return joinPoint.proceed();

            } finally {
                long cost = System.currentTimeMillis() - start;

                if (!payload.getRecords().isEmpty()) {
                    // 同步回填最新的数据值（主要为了回查自增 ID 或 DB 默认值）
                    fillNewStatesSync(payload);

                    // 定义投递任务
                    Runnable auditTask = ContextPropagator.wrap(() ->
                            SmartAuditUpdater.processAsyncRecords(payload, cost));

                    // 事务提交钩子：只有 Commit 成功才发送日志
                    if (TransactionSynchronizationManager.isSynchronizationActive()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                VirtualTaskManager.executeWithLimit(auditTask, VirtualTaskManager.L500);
                            }
                        });
                    } else {
                        VirtualTaskManager.executeWithLimit(auditTask, VirtualTaskManager.L500);
                    }
                }
            }
        });
    }

    /**
     * 在事务结束前，批量同步记录的最新状态
     */
    private void fillNewStatesSync(AuditTransactionContext.ContextPayload payload) {
        if (payload.isPartial() || !payload.isRefresh()) {
            payload.getRecords().forEach(r -> {
                if (r.getType() != AuditTransactionContext.OperationType.INSERT && r.getNewState() == null) {
                    r.setType(AuditTransactionContext.OperationType.DELETE);
                }
            });
            return;
        }

        Map<Class<?>, List<Serializable>> grouped = payload.getRecords().stream()
                .map(r -> {
                    Serializable id = r.getId() != null ? r.getId() : IdExtractor.extractId(r.getNewState());
                    return new AbstractMap.SimpleEntry<>(r, id);
                })
                .filter(e -> e.getValue() != null)
                .collect(Collectors.groupingBy(e -> e.getKey().getClazz(),
                        Collectors.mapping(AbstractMap.SimpleEntry::getValue, Collectors.toList())));

        grouped.forEach((clazz, ids) -> {
            try {
                List<Object> entities = QueryHelper.executeBatchSelect(clazz, ids);
                Map<Serializable, Object> snapshotMap = entities.stream()
                        .collect(Collectors.toMap(IdExtractor::extractId, v -> v, (v1, v2) -> v1));

                for (AuditTransactionContext.Record record : payload.getRecords()) {
                    if (record.getClazz().equals(clazz)) {
                        Serializable rid = record.getId() != null ? record.getId() : IdExtractor.extractId(record.getNewState());
                        Object latest = snapshotMap.get(rid);
                        if (latest != null) {
                            record.setNewState(latest);
                        } else if (record.getType() != AuditTransactionContext.OperationType.INSERT) {
                            record.setType(AuditTransactionContext.OperationType.DELETE);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn(AUDIT, "[自动审计] 批量同步新状态失败: {}", e.getMessage());
            }
        });
    }

    /**
     * 高性能识别审计目标：通过 FastBeanMeta Accessor 提取 ID
     */
  // AutoAuditAspect.java - identify() 方法改造
private List<AuditTarget> identify(Set<Class<?>> allowedClasses, Object[] args) {
    List<AuditTarget> list = new ArrayList<>();
    if (allowedClasses.isEmpty() || args == null) return list;

    for (Class<?> c : allowedClasses) {
        TableInfo info = TableInfoFactory.ofEntityClass(c);
        if (info == null || info.getPrimaryKeyList().isEmpty()) continue;

        for (Object arg : args) {
            if (arg == null) continue;

            if (c.isInstance(arg)) {
                // 单个实体
                Serializable id = IdExtractor.extractId(arg);
                if (id != null) list.add(new AuditTarget(c, id, arg));
                
            } else if (arg instanceof Collection<?> coll) {
                // 👇 新增：处理 List<实体> 或 List<ID>
                Field pkField = FastReflectUtils.getField(c, info.getPrimaryKeyList().getFirst().getProperty());
                for (Object item : coll) {
                    if (item == null) continue;
                    if (c.isInstance(item)) {
                        // List<实体>
                        Serializable id = IdExtractor.extractId(item);
                        if (id != null) list.add(new AuditTarget(c, id, item));
                    } else if (item instanceof Serializable s && pkField != null 
                            && (pkField.getType().isInstance(item) || isPrimitiveMatch(pkField.getType(), item))) {
                        // List<ID>
                        list.add(new AuditTarget(c, s, null));
                    }
                }
                
            } else if (arg instanceof Serializable s && !(arg instanceof String str && str.length() > 64)) {
                // 单个 ID
                Field pkField = FastReflectUtils.getField(c, info.getPrimaryKeyList().getFirst().getProperty());
                if (pkField != null && (pkField.getType().isInstance(arg) || isPrimitiveMatch(pkField.getType(), arg))) {
                    list.add(new AuditTarget(c, s, null));
                }
            } else {
                // DTO 嗅探
                extractFromDto(list, c, arg);
            }
        }
    }
    return list;
}
    /**
     * 深度嗅探 DTO：利用 Accessor 取代反射 get
     */
    private void extractFromDto(List<AuditTarget> list, Class<?> targetClazz, Object dto) {
        FastBeanMeta meta = FastBeanMeta.of(dto.getClass());
        // 这里的 writeableAccessors 已被 FastBeanMeta 预热且缓存
        for (FastBeanMeta.FieldAccessor acc : meta.getWriteableAccessors()) {
            AuditEntityId ann = acc.field().getAnnotation(AuditEntityId.class);
            if (ann != null && (ann.target() == void.class || ann.target() == targetClazz)) {
                try {
                    Object val = acc.getter().apply(dto);
                    if (val instanceof Serializable s) list.add(new AuditTarget(targetClazz, s, dto));
                    else if (val instanceof Collection<?> coll) {
                        coll.forEach(i -> { if (i instanceof Serializable s) list.add(new AuditTarget(targetClazz, s, dto)); });
                    }
                } catch (Exception e) {
                    log.warn(AUDIT, "[自动审计] DTO 嗅探失败: {}", e.getMessage());
                }
            }
        }
    }

    private static Map<Class<?>, Map<Serializable, Object>> fetchBatch(List<AuditTarget> targets) {
        if (targets.isEmpty()) return Collections.emptyMap();
        Map<Class<?>, Map<Serializable, Object>> result = new HashMap<>();
        Map<Class<?>, List<Serializable>> grouped = targets.stream()
                .collect(Collectors.groupingBy(AuditTarget::clazz, Collectors.mapping(AuditTarget::id, Collectors.toList())));

        grouped.forEach((clazz, ids) -> {
            List<Object> entities = QueryHelper.executeBatchSelect(clazz, ids);
            Map<Serializable, Object> map = entities.stream()
                    .collect(Collectors.toMap(IdExtractor::extractId, v -> v, (v1, v2) -> v1));
            result.put(clazz, map);
        });
        return result;
    }

    private boolean isPrimitiveMatch(Class<?> type, Object arg) {
        if (type == Long.class || type == long.class) return arg instanceof Long;
        if (type == Integer.class || type == int.class) return arg instanceof Integer;
        return false;
    }

    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || clazz == String.class || clazz == Integer.class ||
                clazz == Long.class || clazz == Boolean.class || clazz == Double.class || clazz.isEnum();
    }

    private AuditTransactionContext.ContextPayload getContextPayload(AuditLog auditLog) {
        return new AuditTransactionContext.ContextPayload(
                auditLog.module(), auditLog.action(),
                auditLog.partial().resolve(properties.isDefaultPartial()),
                auditLog.skipNull().resolve(properties.isDefaultSkipNull()),
                auditLog.systemFields().resolve(properties.isDefaultSystemFields()),
                auditLog.refresh().resolve(properties.isDefaultRefresh()),
                new HashSet<>(Arrays.asList(resolveEntities(auditLog)))
        );
    }

    private Class<?>[] resolveEntities(AuditLog al) {
        if (al.entities().length > 0) return al.entities();
        return al.entity() != Object.class ? new Class<?>[]{al.entity()} : new Class<?>[0];
    }

    private record AuditTarget(Class<?> clazz, Serializable id, Object input) {}
}