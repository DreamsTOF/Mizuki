package cn.dreamtof.query.core;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.utils.FastBeanMeta;
import cn.dreamtof.query.annotation.QueryMapping;
import cn.dreamtof.query.config.SmartQueryConfig;
import cn.dreamtof.query.enums.MatchType;
import cn.dreamtof.query.structure.SmartQueryStructure;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.mybatis.Mappers;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.core.util.LambdaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 🚀 FlexSmartQuery - 智能查询引擎 (Tier 0 极速版)
 * 将前端请求全自动转化为复杂的、带有防笛卡尔积保护的树形 SQL 查询。
 */
@Slf4j
public class FlexSmartQuery<E, R> {

    // ==========================================
    // 引擎协议注解 (打在 DTO/VO 的属性或 Getter 方法上)
    // ==========================================
    @Target({ElementType.FIELD, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface PageNo {}
    @Target({ElementType.FIELD, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface PageSize {}
    @Target({ElementType.FIELD, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface SortList {}
    @Target({ElementType.FIELD, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) public @interface Seek {}


    private record OverrideRule(MatchType type, BiFunction<QueryColumn, Object, QueryCondition> customFn) {}
    private record AliasRule(Class<?> targetEntity, String targetProperty, String localKey, MatchType matchType) {}
    private record TargetColumn(String alias, QueryColumn column) {}

    private final Class<E> entityClass;
    private final Class<R> resultClass;
    private final QueryWrapper queryWrapper;
    private final SmartQueryStructure structure;

    private QueryWrapper currentActiveWrapper;
    private final Map<Class<?>, SmartQueryAssembler.EntityFactory<?>> entityFactories = new HashMap<>();
    private final Map<String, OverrideRule> overrides = new HashMap<>();
    private final Map<String, AliasRule> manualAliasRules = new HashMap<>();
    private boolean useOrLogic = false;

    /**
     * @description 私有构造器，初始化查询引擎的核心骨架。
     * @reason 强制开发者使用静态工厂方法 `of()` 创建实例，确保每次构建都是全新的、干净的上下文。
     */
    private FlexSmartQuery(Class<E> entityClass, Class<R> resultClass) {
        this.entityClass = entityClass;
        this.resultClass = resultClass;
        TableInfo rootTable = SmartQueryContext.getTableInfo(entityClass);
        this.queryWrapper = QueryWrapper.create().from(rootTable.getTableName()).as("t0");
        this.structure = new SmartQueryStructure(entityClass, resultClass);
        if (!SmartQueryConfig.FACTORIES.isEmpty()) this.entityFactories.putAll(SmartQueryConfig.FACTORIES);
    }

    // ==========================================
    // 1. 初始化与绑定构建
    // ==========================================

    /**
     * @description 静态入口：基于主表 Entity 创建查询引擎。
     * @reason 提供 DSL 风格的链式调用起点，隐藏复杂的实例化逻辑。
     */
    public static <E> FlexSmartQuery<E, E> of(Class<E> entityClass) {
        return new FlexSmartQuery<>(entityClass, entityClass);
    }

    /**
     * @description 绑定目标返回结果的 VO 类。
     * @reason 引擎只有知道你要组装成什么 VO（解析它的 @SmartFetch 和 @Relation），才能知道底层需要 Join 哪些表，从而生成结构树 (SmartQueryStructure)。
     */
    public <V> FlexSmartQuery<E, V> bind(Class<V> voClass) {
        FlexSmartQuery<E, V> next = new FlexSmartQuery<>(this.entityClass, voClass);
        next.structure.parseVoTree(voClass, next.structure.getRootNode(), 0);
        next.structure.applyToWrapper(next.queryWrapper, next.structure.getRootNode());
        next.entityFactories.putAll(this.entityFactories);
        next.overrides.putAll(this.overrides);
        next.manualAliasRules.putAll(this.manualAliasRules);
        next.useOrLogic = this.useOrLogic;
        return next;
    }

    // ==========================================
    // 2. 自定义规则注入
    // ==========================================

    /**
     * @description 注册特定实体类的自定义装配工厂。
     * @reason 有些复杂的 JSON 字段、或需要依赖 Spring Bean (如 Redis 查询) 的字段无法用反射自动映射，必须允许开发者插入自定义装配逻辑。
     */
    public <T> FlexSmartQuery<E, R> registerFactory(Class<T> entityClass, SmartQueryAssembler.EntityFactory<T> factory) {
        this.entityFactories.put(entityClass, factory); return this;
    }

    /**
     * @description 批量合并装配工厂。
     * @reason 方便将全局配置类中的工厂一次性灌入当前引擎。
     */
    public FlexSmartQuery<E, R> withFactories(Map<Class<?>, SmartQueryAssembler.EntityFactory<?>> factories) {
        if (factories != null) this.entityFactories.putAll(factories); return this;
    }

    /**
     * @description 手动映射 DTO 字段到目标表的字段（跨表搜索）。
     * @reason 如果开发者不想写 @QueryMapping 注解，提供一种全 Lambda、防硬编码的类型安全配置方式。
     */
    public <D, T> FlexSmartQuery<E, R> map(LambdaGetter<D> dtoGetter, Class<T> targetEntity, LambdaGetter<T> targetGetter, LambdaGetter<E> localKeyGetter) {
        String dtoField = LambdaUtil.getFieldName(dtoGetter);
        String targetProp = LambdaUtil.getFieldName(targetGetter);
        String localKey = localKeyGetter != null ? LambdaUtil.getFieldName(localKeyGetter) : "";
        this.manualAliasRules.put(dtoField, new AliasRule(targetEntity, targetProp, localKey, MatchType.CUSTOM));
        return this;
    }

    public <D, T> FlexSmartQuery<E, R> map(LambdaGetter<D> dtoGetter, Class<T> targetEntity, LambdaGetter<T> targetGetter) {
        return map(dtoGetter, targetEntity, targetGetter, null);
    }

    /**
     * @description 覆盖某个字段的默认匹配规则 (如强制使用 LIKE)。
     * @reason 纠正引擎对某个字段的自动推断行为。
     */
    public <D> FlexSmartQuery<E, R> override(LambdaGetter<D> getter, MatchType type) {
        this.overrides.put(LambdaUtil.getFieldName(getter), new OverrideRule(type, null)); return this;
    }

    /**
     * @description 提供高度自定义的 SQL 条件拼接函数。
     * @reason 面对极端复杂的业务逻辑（如范围查询、位运算），允许开发者接管底层 QueryColumn 的拼接过程。
     */
    public <D> FlexSmartQuery<E, R> override(LambdaGetter<D> getter, BiFunction<QueryColumn, Object, QueryCondition> customFn) {
        this.overrides.put(LambdaUtil.getFieldName(getter), new OverrideRule(MatchType.CUSTOM, customFn)); return this;
    }

    /**
     * @description 核心启动枢纽：根据前端 DTO 全自动构建查询。
     * @reason 这是一键生成的魔法来源。它会读取编译好的 SmartQueryPlan 并触发协议处理器。
     */
    public <D> FlexSmartQuery<E, R> autoBuild(D queryDto) {
        if (queryDto == null) return this;
        SmartQueryPlan plan = SmartQueryPlan.get(queryDto.getClass());
        plan.execute(this, queryDto);
        handleProtocol(queryDto);
        return this;
    }

    // ==========================================
    // 3. Plan 回调与 AST 构建
    // ==========================================

    /**
     * @description 根据 DTO 的值和元数据生成真实的 MyBatis-Flex QueryCondition。
     * @reason 被 SmartQueryPlan 内部高频回调，是连接 Java 字段与数据库 SQL 语法树的关键纽带。处理了映射优先级。
     */
    public void applyFieldCondition(String field, Object val, SmartQueryContext.DtoFieldMeta meta) {
        QueryWrapper wrapper = this.currentActiveWrapper != null ? this.currentActiveWrapper : this.queryWrapper;
        MatchType connectType = this.useOrLogic ? MatchType.OR : MatchType.AND;

        // 1. 优先处理用户通过 override() 手动指定的 SQL 规则
        if (overrides.containsKey(field)) {
            OverrideRule rule = overrides.get(field);
            if (rule.customFn() != null) {
                TargetColumn target = findColumnRecursively(field);
                QueryColumn col = target != null ? target.column() : new QueryColumn("t0", field);
                addConditionToWrapper(wrapper, rule.customFn().apply(col, val), connectType);
                return;
            }
        }

        // 2. 处理跨表映射规则
        AliasRule aliasRule = manualAliasRules.get(field);
        if (aliasRule == null && meta != null && meta.mapping() != null) {
            QueryMapping m = meta.mapping();
            aliasRule = new AliasRule(m.targetEntity(), m.targetProperty(), m.localKey(), m.matchType());
        }
        if (aliasRule != null) {
            applyExplicitMappingToWrapper(wrapper, aliasRule, field, val, meta, connectType);
            return;
        }

        // 3. 处理普通的同名属性过滤
        if (structure.filterRegistry.containsKey(field)) {
            SmartQueryStructure.FilterMapping mapping = structure.filterRegistry.get(field);
            MatchType type = resolveMatchType(field, meta, val, mapping.type());
            addConditionToWrapper(wrapper, buildBaseCondition(mapping.column(), val, type), connectType);
            return;
        }

        // 4. 兜底逻辑：去主表中查找是否有这个字段
        QueryColumn rootCol = SmartQueryContext.getTableInfo(entityClass).getQueryColumnByProperty(field);
        if (rootCol != null) {
            MatchType type = resolveMatchType(field, meta, val, MatchType.CUSTOM);
            addConditionToWrapper(wrapper, buildBaseCondition(new QueryColumn("t0", rootCol.getName()), val, type), connectType);
        }
    }

    // ==========================================
    // 4. 原生 Wrapper 代理与执行
    // ==========================================

    private void addConditionToWrapper(QueryWrapper wrapper, QueryCondition cond, MatchType connectType) {
        if (cond == null) return;
        if (connectType == MatchType.OR) wrapper.or(cond); else wrapper.and(cond);
    }
    private void addCondition(QueryCondition cond) {
        addConditionToWrapper(this.queryWrapper, cond, this.useOrLogic ? MatchType.OR : MatchType.AND);
    }

    /**
     * @description 原生添加 AND 条件。
     * @reason 在 autoBuild() 无法覆盖的特殊场景下，提供原生的手写支持。
     */
    public FlexSmartQuery<E, R> where(QueryCondition condition) { addCondition(condition); return this; }

    /**
     * @description 打印当前构建出的 SQL 语句。
     * @reason 极大地帮助开发者进行调试和慢查询排查。
     */
    public FlexSmartQuery<E, R> log() { log.info("\nSQL: {}", queryWrapper.toSQL()); return this; }

    /**
     * @description 切换后续条件的连接符为 OR。
     * @reason 支持 A OR B 这种逻辑拼接。
     */
    public FlexSmartQuery<E, R> useOr() { this.useOrLogic = true; return this; }
    public FlexSmartQuery<E, R> and(Consumer<QueryWrapper> consumer) { this.queryWrapper.and(consumer); return this; }
    public FlexSmartQuery<E, R> or(Consumer<QueryWrapper> consumer) { this.queryWrapper.or(consumer); return this; }
    public QueryWrapper getRawWrapper() { return this.queryWrapper; }

    /**
     * @description 带有多数据源切换保护机制的执行块。
     * @reason 确保不同微服务或多租户的表去对应的数据库中执行。
     */
    private <T> T execWithDS(Supplier<T> supplier) {
        String dsKey = structure.getRootNode().tableInfo.getDataSource();
        boolean switchDs = StringUtils.isNotBlank(dsKey);
        try {
            if (switchDs) DataSourceKey.use(dsKey);
            return supplier.get();
        } finally {
            if (switchDs) DataSourceKey.clear();
        }
    }

    // ==========================================
    // 5. 结果触发器 (List, Page, Seek)
    // ==========================================

    /**
     * @description 执行全量查询并进行对象组装。
     * @reason 最终执行 SQL 并将平铺的 Row 转换为多维树形 VO 的核心出口。
     */
    @SuppressWarnings("unchecked")
    public List<R> list() {
        // 如果没有 join 子表且类型一致，直接走原生超高速查询
        if (structure.getRootNode().children.isEmpty() && entityClass == resultClass) {
            return (List<R>) Mappers.ofEntityClass(entityClass).selectListByQuery(queryWrapper);
        }

        List<Row> rows = execWithDS(() -> Db.selectListBySql(queryWrapper.toSQL()));
        if (rows.isEmpty()) return Collections.emptyList();

        List<Map<String, Object>> mapList = (List<Map<String, Object>>) (List<?>) rows;
        return new SmartQueryAssembler<>(resultClass, structure.getRootNode(), entityFactories)
                .reconstruct(mapList);
    }

    /**
     * @description 传统分页 API (通过 DTO 对象一键分页)。
     * @reason 让业务层完全不关心 pageNum 和 pageSize 的获取，高度封装。
     */
    public Page<R> page(Object queryDto) {
        long current = extractProtocol(queryDto, PageNo.class, Number.class).map(Number::longValue).orElse(1L);
        long size = extractProtocol(queryDto, PageSize.class, Number.class).map(Number::longValue).orElse(10L);
        return page(current, size);
    }

    /**
     * @description 传统分页底层执行。
     * @reason 会自动判断是否存在 1:N 级联关系，如果有，则自动开启“防膨胀保护”。
     */
    @SuppressWarnings("unchecked")
    public Page<R> page(long num, long size) {
        if (structure.getRootNode().children.isEmpty() && entityClass == resultClass) {
            return (Page<R>) Mappers.ofEntityClass(entityClass).paginate(Page.of(num, size), queryWrapper);
        }
        boolean hasCollectionJoin = checkHasCollectionJoin(structure.getRootNode());
        if (hasCollectionJoin) {
            return pageWithIdGroup(num, size); // 1:N 膨胀保护分页
        }
        String tableName = structure.getRootNode().tableInfo.getTableName();
        Page<Row> rowPage = execWithDS(() -> Db.paginate(tableName, Page.of(num, size), queryWrapper));
        return buildPageResult(rowPage);
    }

    /**
     * @description ID 分组分页保护机制。
     * @reason 如果包含 Left Join List，底层条数会膨胀。必须先查出主表的 ID，再去查关联明细，保证分页总数及每页条数准确无误。
     * 【修复】：使用内存重排，保证 IN(ids) 查出的数据顺序与翻页预期的顺序严格一致。
     */
    @SuppressWarnings("unchecked")
    private Page<R> pageWithIdGroup(long num, long size) {
        TableInfo rootTable = structure.getRootNode().tableInfo;
        String pkCol = rootTable.getPrimaryKeyList().getFirst().getColumn();

        // 1. 仅提取不重复的主键 ID 翻页，保留所有的查询和排序条件
        QueryWrapper idWrapper = queryWrapper.clone();
        idWrapper.select(QueryMethods.distinct(new QueryColumn("t0", pkCol)).as("id"));

        Page<Row> idPage = execWithDS(() -> Db.paginate(rootTable.getTableName(), Page.of(num, size), idWrapper));
        if (idPage.getTotalRow() == 0) return new Page<>(Collections.emptyList(), num, size, 0);

        List<Object> ids = idPage.getRecords().stream().map(r -> r.get("id")).collect(Collectors.toList());

        // 2. 根据 ID 带着全关联结构重新查询 (注意：这里不要把 orderBy 带进去，因为 IN 查询本来就是无序的)
        QueryWrapper dataWrapper = QueryWrapper.create().from(rootTable.getTableName()).as("t0");
        structure.applyToWrapper(dataWrapper, structure.getRootNode());
        dataWrapper.where(new QueryColumn("t0", pkCol).in(ids));

        List<Row> rows = execWithDS(() -> Db.selectListBySql(dataWrapper.toSQL()));
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) (List<?>) rows;

        List<R> results = new SmartQueryAssembler<>(resultClass, structure.getRootNode(), entityFactories)
                .reconstruct(mapList);

        // 3. 【核心修复】：基于查出的正确主键顺序(ids)，对组装好的 results 进行内存重排
        Map<Object, R> resultMap = new HashMap<>();
        FastBeanMeta rootMeta = FastBeanMeta.of(resultClass);
        String voPkName = structure.getRootNode().voPkPropName != null ? structure.getRootNode().voPkPropName : "id";

        for (R result : results) {
            Object pkValue = rootMeta.getAccessor(voPkName).getter().apply(result);
            resultMap.put(pkValue, result);
        }

        List<R> sortedResults = new ArrayList<>(results.size());
        for (Object id : ids) {
            R item = resultMap.get(id);
            if (item != null) {
                sortedResults.add(item);
            }
        }

        return new Page<>(sortedResults, idPage.getPageNumber(), idPage.getPageSize(), idPage.getTotalRow());
    }
    /**
     * @description 判断是否包含 1:N 关联。
     * @reason 决定是否需要触发消耗性能的 pageWithIdGroup 分页保护机制。
     */
    private boolean checkHasCollectionJoin(SmartQueryStructure.JoinNode node) {
        for (SmartQueryStructure.JoinNode child : node.children.values()) {
            if (child.isCollection && !child.isDeferred) return true;
            if (checkHasCollectionJoin(child)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Page<R> buildPageResult(Page<Row> rowPage) {
        List<R> results;
        if (rowPage.getTotalRow() == 0) {
            results = Collections.emptyList();
        } else {
            List<Map<String, Object>> records = (List<Map<String, Object>>) (List<?>) rowPage.getRecords();
            results = new SmartQueryAssembler<>(resultClass, structure.getRootNode(), entityFactories)
                    .reconstruct(records);
        }
        return new Page<>(results, rowPage.getPageNumber(), rowPage.getPageSize(), rowPage.getTotalRow());
    }
    // ==========================================
    // Seek 瀑布流专属配置
    // ==========================================
    private String seekSortProp = null;
    private boolean seekSortAsc = true; // 默认升序

    /**
     * @description 手动注入瀑布流的排序字段与方向。如果不注入，默认按主键升序。
     * @example query.seekBy("createTime", false).seek(cursorValue, 10);
     */
    public FlexSmartQuery<E, R> seekBy(String property, boolean isAsc) {
        this.seekSortProp = property;
        this.seekSortAsc = isAsc;
        return this;
    }

    public <D> FlexSmartQuery<E, R> seekBy(LambdaGetter<D> getter, boolean isAsc) {
        this.seekSortProp = LambdaUtil.getFieldName(getter);
        this.seekSortAsc = isAsc;
        return this;
    }
    /**
     * @description 纯净版游标瀑布流分页 API (编程式直接调用)
     * @param cursorValue 当前游标的值 (由前端传入)
     * @param size        拉取条数
     */
    public CursorResult<R> seek(Object cursorValue, long size) {
        // 1. 确定最终的排序属性 (有注入用注入，无注入用主键)
        String finalProp = this.seekSortProp;
        if (finalProp == null) {
            TableInfo tableInfo = SmartQueryContext.getTableInfo(entityClass);
            finalProp = structure.getRootNode().voPkPropName != null ? structure.getRootNode().voPkPropName :
                    (tableInfo.getPrimaryKeyList() != null && !tableInfo.getPrimaryKeyList().isEmpty() ? tableInfo.getPrimaryKeyList().getFirst().getProperty() : "id");
        }

        // 2. 核心魔法：偷偷多查 1 条用来探测
        List<R> list = executeSeekList(cursorValue, size + 1, finalProp, this.seekSortAsc);

        boolean hasNext = list.size() > size;
        Serializable nextCursor = null;

        if (hasNext) {
            // 剔除最后一条探测数据
            list = list.subList(0, (int) size);
        }

        // 3. 极速提取最后一条数据的游标标识 (根据 finalProp 提取)
        if (!list.isEmpty()) {
            R lastRecord = list.get(list.size() - 1);
            try {
                FastBeanMeta.FieldAccessor accessor = FastBeanMeta.of(lastRecord.getClass()).getAccessor(finalProp);
                if (accessor != null && accessor.getter() != null) {
                    nextCursor = (Serializable) accessor.getter().apply(lastRecord);
                }
            } catch (Exception e) {
                log.warn("SmartQuery 提取 nextCursor 失败，对象可能缺失属性: {}", finalProp, e);
            }
        }

        return new CursorResult<>(list, nextCursor, hasNext);
    }

    /**
     * @description DTO 兼容版游标 API。
     * @reason 自动从 DTO 提取 pageSize 和名为 cursor 的字段。
     */
    public CursorResult<R> seek(Object queryDto) {
        if (queryDto == null) return seek((Object) null, 10L);

        long size = extractProtocol(queryDto, PageSize.class, Number.class).map(Number::longValue).orElse(10L);
        Object cursorValue = null;

        // 尝试从 DTO 中极速获取名为 "cursor" 的通用字段值
        try {
            FastBeanMeta.FieldAccessor acc = FastBeanMeta.of(queryDto.getClass()).getAccessor("cursor");
            if (acc != null && acc.getter() != null) {
                cursorValue = acc.getter().apply(queryDto);
            }
        } catch (Exception ignored) {}

        return seek(cursorValue, size);
    }
    /**
     * @description 底层的原生游标限定查询。
     */
    private List<R> executeSeekList(Object lastCursorValue, long size, String propName, boolean isAsc) {
        // 1. 【安全核心】：强制清空 DTO 解析可能带来的所有 OrderBy，保证游标分页的绝对单一排序！

        // 2. 找到该属性对应的数据库真实列名
        TargetColumn target = findColumnRecursively(propName);
        QueryColumn sortCol;
        if (target != null) {
            sortCol = new QueryColumn(target.alias(), target.column().getName());
        } else {
            QueryColumn rootCol = SmartQueryContext.getTableInfo(entityClass).getQueryColumnByProperty(propName);
            if (rootCol != null) {
                sortCol = new QueryColumn("t0", rootCol.getName());
            } else {
                sortCol = new QueryColumn("t0", structure.getRootNode().pkColName);
            }
        }

        // 3. 应用动态唯一排序与游标过滤条件 (> 或 <)
        if (isAsc) {
            queryWrapper.orderBy(sortCol.asc());
            if (lastCursorValue != null) queryWrapper.where(sortCol.gt(lastCursorValue));
        } else {
            queryWrapper.orderBy(sortCol.desc());
            if (lastCursorValue != null) queryWrapper.where(sortCol.lt(lastCursorValue));
        }

        queryWrapper.limit(size);
        return list();
    }


    // ==========================================
    // 6. Internal Helpers (底层细节处理引擎)
    // ==========================================

    /**
     * @description 处理显式映射 (@QueryMapping)。
     * @reason 支持通过注解指明某个字段其实属于关联表，并自动生成正确的 WHERE 条件；同时支持微服务跨库连表降级。
     */
    private void applyExplicitMappingToWrapper(QueryWrapper wrapper, AliasRule rule, String fieldName, Object val, SmartQueryContext.DtoFieldMeta meta, MatchType connectType) {
        TargetColumn target = findColumnInJoinTreeByEntity(structure.getRootNode(), rule.targetEntity(), rule.targetProperty());
        MatchType type = resolveMatchType(fieldName, meta, val, rule.matchType());
        if (target != null) {
            addConditionToWrapper(wrapper, buildBaseCondition(new QueryColumn(target.alias(), target.column().getName()), val, type), connectType);
        } else if (StringUtils.isNotBlank(rule.localKey())) {
            addCrossDbCondition(rule, val, type);
        } else {
            log.warn("Mapping Invalid: No node found for [{}]", rule.targetEntity().getSimpleName());
        }
    }

    /**
     * @description 跨数据库查询条件转换。
     * @reason 面对没法真物理 JOIN 的情况，自动发次子查询拿到 ids。
     * 【修复】：加入 Limit 保护与日志预警，防止 SQL IN 语句超长导致数据库崩溃。
     */
    private void addCrossDbCondition(AliasRule rule, Object val, MatchType type) {
        TableInfo rInfo = SmartQueryContext.getTableInfo(rule.targetEntity());
        if (rInfo == null) return;

        QueryWrapper subQuery = QueryWrapper.create()
                .select(rInfo.getPrimaryKeyList().getFirst().getColumn()).from(rInfo.getTableName())
                .where(buildBaseCondition(new QueryColumn(rInfo.getTableName(), rInfo.getColumnByProperty(rule.targetProperty())), val, type))
                .limit(500); // 【核心修复 1】：强制阻断超过 500 个元素的返回，防止撑爆 IN(...)

        List<Object> ids = Mappers.ofEntityClass(rule.targetEntity()).selectObjectListByQuery(subQuery);

        // 【核心修复 2】：超限预警日志
        if (ids.size() >= 500) {
            log.warn("⚠️ [SmartQuery 跨库告警] 触发跨库级联过滤，查出 ID 数量触及安全上限(500)，请优化过滤条件！目标表: {}", rInfo.getTableName());
        }

        QueryColumn localCol = SmartQueryContext.getTableInfo(entityClass).getQueryColumnByProperty(rule.localKey());
        if (localCol != null) {
            if (CollectionUtils.isEmpty(ids)) {
                addCondition(QueryCondition.create(QueryMethods.raw("1").getColumn(), "=", 2)); // 防穿透
            } else {
                addCondition(new QueryColumn("t0", localCol.getName()).in(ids));
            }
        }
    }
    /**
     * @description 根据 Entity 和属性名，在 Join 树里寻找别名。
     * @reason 处理 ExplicitMapping 时，必须知道目标字段到底位于哪一张子表（如 t1 或 t2）。
     */
    private TargetColumn findColumnInJoinTreeByEntity(SmartQueryStructure.JoinNode node, Class<?> targetEntity, String propertyName) {
        if (node.entityClass != null && node.entityClass.equals(targetEntity)) {
            QueryColumn col = node.tableInfo.getQueryColumnByProperty(propertyName);
            if (col != null) return new TargetColumn(node.tableAlias, col);
        }
        for (SmartQueryStructure.JoinNode child : node.children.values()) {
            TargetColumn found = findColumnInJoinTreeByEntity(child, targetEntity, propertyName);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * @description 自动在主表或暴露的关联字段里搜寻指定属性。
     * @reason 实现动态排序或无注解模糊过滤的核心，找到属性对应的真实数据库列名。
     */
    private TargetColumn findColumnRecursively(String propertyName) {
        if (structure.filterRegistry.containsKey(propertyName)) {
            SmartQueryStructure.FilterMapping mapping = structure.filterRegistry.get(propertyName);
            return new TargetColumn(mapping.column().getTable().getName(), mapping.column());
        }
        QueryColumn rootCol = SmartQueryContext.getTableInfo(entityClass).getQueryColumnByProperty(propertyName);
        if (rootCol != null) return new TargetColumn("t0", rootCol);
        return null;
    }

    /**
     * @description 根据字段特征推断查询方式（比如后缀带有 Like 的自动生成 LIKE 条件）。
     * @reason “智能推断”的核心，避免开发者写海量的匹配规则注解，极大提高开发效率。
     */
    private MatchType resolveMatchType(String fieldName, SmartQueryContext.DtoFieldMeta meta, Object value, MatchType preferred) {
        if (overrides.containsKey(fieldName)) return overrides.get(fieldName).type();
        if (preferred != MatchType.CUSTOM) return preferred;
        if (fieldName.endsWith("Like")) return MatchType.LIKE;
        if (fieldName.endsWith("Begin") || fieldName.endsWith("Start")) return MatchType.GE;
        if (fieldName.endsWith("End") || fieldName.endsWith("Finish")) return MatchType.LE;
        if (fieldName.endsWith("List") || fieldName.endsWith("Ids")) return MatchType.IN;
        Class<?> type = meta != null ? meta.type() : (value != null ? value.getClass() : null);
        if (type != null) {
            if (Collection.class.isAssignableFrom(type)) return MatchType.IN;
            if (String.class.isAssignableFrom(type)) return MatchType.LIKE;
        }
        return MatchType.EQ;
    }

    /**
     * @description 将推断好的 MatchType 转化为实际的 Flex QueryCondition。
     * @reason 生成最终发送给数据库的 SQL WHERE 语句块。
     */
    private QueryCondition buildBaseCondition(QueryColumn col, Object val, MatchType type) {
        Object finalVal = val;
        if ((type == MatchType.LIKE || type == MatchType.LEFT_LIKE || type == MatchType.RIGHT_LIKE) && val instanceof String s) {
            if (StringUtils.isBlank(s)) return null;
            finalVal = s.trim();
        }
        return switch (type) {
            case EQ -> col.eq(finalVal);
            case NE -> col.ne(finalVal);
            case LIKE -> col.like(finalVal);
            case LEFT_LIKE -> col.likeLeft(finalVal);
            case RIGHT_LIKE -> col.likeRight(finalVal);
            case GE -> col.ge(finalVal);
            case GT -> col.gt(finalVal);
            case LE -> col.le(finalVal);
            case LT -> col.lt(finalVal);
            case IN -> (finalVal instanceof Collection<?> c) ? col.in(c) : col.eq(finalVal);
            case NOT_IN -> (finalVal instanceof Collection<?> c) ? col.notIn(c) : col.ne(finalVal);
            default -> null;
        };
    }

    /**
     * @description 协议解析处理器。
     * @reason 集中处理前端传来的排序规则，如果在实体树中找不到映射字段则实施快速失败 (Fail-fast)。
     */
    private void handleProtocol(Object dto) {
        extractProtocol(dto, SortList.class, List.class).ifPresent(sorters -> {
            for (Object obj : sorters) {
                try {
                    String field = (String) SystemMetaObject.forObject(obj).getValue("field");
                    Object orderEnum = SystemMetaObject.forObject(obj).getValue("order");

                    if (StringUtils.isBlank(field)) continue;

                    TargetColumn target = findColumnRecursively(field);

                    if (target != null) {
                        QueryColumn col = new QueryColumn(target.alias(), target.column().getName());
                        boolean isAsc = orderEnum == null || orderEnum.toString().toUpperCase().contains("ASC");
                        if (isAsc) queryWrapper.orderBy(col.asc()); else queryWrapper.orderBy(col.desc());
                    } else {
                        // 🚨 没找到！打印 error 日志并阻断执行，防止掩盖前后端联调错误
                        log.error("SmartQuery 集合排序失败：未在关联树中找到名为 [{}] 的字段", field);
                        throw new IllegalArgumentException("SmartQuery 排序失败：未找到名为 [" + field + "] 的字段，请检查前端传参或 VO 映射！");
                    }
                } catch (Exception e) {
                    if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
                    log.error("SmartQuery 解析 SortList 时发生未知错误", e);
                    throw new RuntimeException("SmartQuery 解析排序协议时发生系统异常", e);
                }
            }
        });
    }

    /**
     * @description 扫描提取对象中被打上特殊注解的属性值。
     * @reason 不仅扫描 Field 字段，还扫描 Method(Getter)。由于核心包剥离了，这样允许业务包通过继承 + 覆盖 Getter 方法来生效引擎配置。
     */
    private <T> Optional<T> extractProtocol(Object o, Class<? extends Annotation> a, Class<T> t) {
        Class<?> clazz = o.getClass();
        while (clazz != null && clazz != Object.class) {
            // 1. 扫描字段 (Field)
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(a)) {
                    try {
                        Object v = SystemMetaObject.forObject(o).getValue(f.getName());
                        if (v != null && t.isAssignableFrom(v.getClass())) return Optional.of((T) v);
                    } catch (Exception ignored) {}
                }
            }
            // 2. 扫描方法 (Method)，完美支持子类继承增强！
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(a) && m.getParameterCount() == 0) {
                    try {
                        m.setAccessible(true);
                        Object v = m.invoke(o);
                        if (v != null && t.isAssignableFrom(v.getClass())) return Optional.of((T) v);
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
        return Optional.empty();
    }
}