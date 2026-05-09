# dreamtof-query 模块设计模式分析

## 模块概述

dreamtof-query 提供智能查询构建能力，支持自动推断查询条件、关联查询、结果组装等功能。

---

## 1. 建造者模式（Builder Pattern）

### 实现位置
`cn.dreamtof.query.core.FlexSmartQuery`

### 示例代码

```java
public class FlexSmartQuery<E, R> {
    private final Class<E> entityClass;
    private final Class<R> resultClass;
    private final QueryWrapper queryWrapper;
    private final SmartQueryStructure structure;
    private boolean useOrLogic = false;

    private FlexSmartQuery(Class<E> entityClass, Class<R> resultClass) {
        this.entityClass = entityClass;
        this.resultClass = resultClass;
        this.queryWrapper = QueryWrapper.create();
        this.structure = new SmartQueryStructure(entityClass);
    }

    public static <E> FlexSmartQuery<E, E> of(Class<E> entityClass) {
        return new FlexSmartQuery<>(entityClass, entityClass);
    }

    public <V> FlexSmartQuery<E, V> bind(Class<V> voClass) {
        FlexSmartQuery<E, V> next = new FlexSmartQuery<>(this.entityClass, voClass);
        next.structure.parseVoTree(voClass, next.structure.getRootNode(), 0);
        return next;
    }

    public <D> FlexSmartQuery<E, R> autoBuild(D queryDto) {
        SmartQueryPlan plan = SmartQueryPlan.getOrCreate(queryDto.getClass());
        plan.execute(this, queryDto);
        return this;
    }

    public <D> FlexSmartQuery<E, R> override(LambdaGetter<D> getter, MatchType type) {
        this.overrides.put(LambdaUtil.getFieldName(getter), new OverrideRule(type, null));
        return this;
    }

    public FlexSmartQuery<E, R> useOr() {
        this.useOrLogic = true;
        return this;
    }

    public FlexSmartQuery<E, R> where(QueryCondition condition) {
        addCondition(condition);
        return this;
    }

    public FlexSmartQuery<E, R> log() {
        System.out.println(queryWrapper.toSQL());
        return this;
    }
}
```

### 典型调用示例

```java
List<UserVO> users = FlexSmartQuery.of(User.class)
    .bind(UserVO.class)
    .autoBuild(queryDto)
    .useOr()
    .log()
    .list();

Page<UserVO> page = FlexSmartQuery.of(User.class)
    .bind(UserVO.class)
    .autoBuild(queryDto)
    .page(1, 10);
```

### 解决的问题
- 提供流畅的 DSL 风格 API
- 支持复杂查询的渐进式构建
- 私有构造器强制使用静态工厂方法

---

## 2. 组合模式（Composite Pattern）

### 实现位置
`cn.dreamtof.query.structure.SmartQueryStructure`

### 示例代码

```java
public class SmartQueryStructure {
    public static class JoinNode {
        public String path;
        public String tableAlias;
        public TableInfo tableInfo;
        public String pkColName;
        public String voPkPropName;
        
        public Map<String, String> selectFields = new LinkedHashMap<>();
        public Map<String, JoinNode> children = new LinkedHashMap<>();
        public List<JoinNode> deferredChildren = new ArrayList<>();
        
        public boolean isCollection = false;
        public String linkCol;
        public String hostCol;
    }

    private final JoinNode rootNode = new JoinNode();

    public void parseVoTree(Class<?> voClass, JoinNode node, int collectionDepth) {
        node.voPkPropName = findPkPropInVo(voClass, node.pkColName, node.tableInfo);

        for (VoFieldMeta meta : SmartQueryContext.getVoFields(voClass)) {
            if (meta.relation() != null) {
                processRelation(meta, node);
                continue;
            }
            if (meta.smartFetch() != null) {
                processSmartFetch(meta, node, collectionDepth);
                continue;
            }
            // 处理普通字段
            node.selectFields.put(meta.name(), meta.columnName());
        }
    }

    private void processSmartFetch(VoFieldMeta meta, JoinNode parentNode, int collectionDepth) {
        JoinNode child = new JoinNode();
        child.path = parentNode.path.isEmpty() ? meta.name() : parentNode.path + "$" + meta.name();
        child.isCollection = Collection.class.isAssignableFrom(meta.type());
        
        parentNode.children.put(meta.name(), child);
        
        if (child.isCollection && collectionDepth >= MAX_COLLECTION_DEPTH) {
            parentNode.deferredChildren.add(child);
        } else {
            parseVoTree(meta.fieldType(), child, nextDepth);
        }
    }
}
```

### 解决的问题
- 将 VO 的嵌套结构映射为树形 Join 节点
- 支持无限层级的对象关联（如 User → Orders → Items）
- 统一处理单个对象和集合类型

---

## 3. 命令模式（Command Pattern）

### 实现位置
`cn.dreamtof.query.core.SmartQueryPlan`

### 示例代码

```java
public class SmartQueryPlan {
    private final List<BiConsumer<FlexSmartQuery<?, ?>, Object>> steps = new ArrayList<>();

    public void execute(FlexSmartQuery<?, ?> queryEngine, Object dto) {
        for (var step : steps) {
            step.accept(queryEngine, dto);
        }
    }

    private static SmartQueryPlan build(Class<?> dtoClass) {
        SmartQueryPlan plan = new SmartQueryPlan();
        FastBeanMeta meta = FastBeanMeta.of(dtoClass);

        for (var accessor : meta.getReadableAccessors()) {
            Field field = accessor.field();
            String fieldName = field.getName();
            DtoFieldMeta fieldMeta = SmartQueryContext.getDtoFieldMeta(dtoClass, fieldName);

            plan.steps.add((engine, dto) -> {
                Object val = accessor.getter().apply(dto);
                if (val == null || (val instanceof String s && s.isBlank())) {
                    return;
                }
                engine.applyFieldCondition(fieldName, val, fieldMeta);
            });
        }

        return plan;
    }

    private static final Map<Class<?>, SmartQueryPlan> PLAN_CACHE = new ConcurrentHashMap<>();

    public static SmartQueryPlan getOrCreate(Class<?> dtoClass) {
        return PLAN_CACHE.computeIfAbsent(dtoClass, SmartQueryPlan::build);
    }
}
```

### 解决的问题
- 将 DTO 字段的处理逻辑封装为命令对象
- 支持预编译：首次使用时构建命令链，后续直接执行
- 将"解释执行"优化为"预编译执行"，大幅提升性能

---

## 4. 策略模式（Strategy Pattern）

### 实现位置
`cn.dreamtof.query.core.FlexSmartQuery` + `cn.dreamtof.query.enums.MatchType`

### 示例代码

```java
public enum MatchType {
    EQ, NE, GT, GE, LT, LE, LIKE, LIKE_LEFT, LIKE_RIGHT, IN, NOT_IN, IS_NULL, NOT_NULL
}

public class FlexSmartQuery<E, R> {
    
    private MatchType resolveMatchType(String fieldName, DtoFieldMeta meta, Object value, MatchType preferred) {
        if (preferred != null) return preferred;
        
        if (fieldName.endsWith("Like")) return MatchType.LIKE;
        if (fieldName.endsWith("Begin") || fieldName.endsWith("Start")) return MatchType.GE;
        if (fieldName.endsWith("End") || fieldName.endsWith("Finish")) return MatchType.LE;
        if (fieldName.endsWith("List") || fieldName.endsWith("Ids")) return MatchType.IN;
        if (fieldName.endsWith("Not")) return MatchType.NE;
        
        Class<?> type = meta.type();
        if (Collection.class.isAssignableFrom(type)) return MatchType.IN;
        if (String.class.isAssignableFrom(type)) return MatchType.LIKE;
        
        return MatchType.EQ;
    }

    private QueryCondition buildBaseCondition(QueryColumn col, Object val, MatchType type) {
        Object finalVal = val instanceof Collection<?> c && c.size() == 1 
            ? c.iterator().next() 
            : val;

        return switch (type) {
            case EQ -> col.eq(finalVal);
            case NE -> col.ne(finalVal);
            case GT -> col.gt(finalVal);
            case GE -> col.ge(finalVal);
            case LT -> col.lt(finalVal);
            case LE -> col.le(finalVal);
            case LIKE -> col.like(finalVal);
            case LIKE_LEFT -> col.likeLeft(finalVal);
            case LIKE_RIGHT -> col.likeRight(finalVal);
            case IN -> (finalVal instanceof Collection<?> c) ? col.in(c) : col.eq(finalVal);
            case NOT_IN -> (finalVal instanceof Collection<?> c) ? col.notIn(c) : col.ne(finalVal);
            case IS_NULL -> col.isNull();
            case NOT_NULL -> col.isNotNull();
        };
    }
}
```

### 解决的问题
- 根据字段命名约定自动推断查询策略
- 支持 10+ 种匹配策略
- 可通过 override() 方法手动指定策略

---

## 5. 工厂方法模式（Factory Method Pattern）

### 实现位置
`cn.dreamtof.query.core.SmartQueryAssembler`

### 示例代码

```java
public class SmartQueryAssembler<R> {
    
    @FunctionalInterface
    public interface EntityFactory<E> {
        E create(Map<String, Object> row, String aliasPrefix);
    }

    private final Map<Class<?>, EntityFactory<?>> entityFactoryRegistry = new ConcurrentHashMap<>();

    public <T> void registerFactory(Class<T> entityClass, EntityFactory<T> factory) {
        entityFactoryRegistry.put(entityClass, factory);
    }

    @SuppressWarnings("unchecked")
    private <T> T buildNodeObject(Map<String, Object> row, JoinNode node) {
        EntityFactory<?> factory = entityFactoryRegistry.get(node.entityClass);
        if (factory != null) {
            return (T) factory.create(row, node.path.replace("$", ".") + ".");
        }

        // 默认工厂实现
        try {
            T instance = (T) node.entityClass.getDeclaredConstructor().newInstance();
            for (var entry : node.selectFields.entrySet()) {
                String alias = (node.path.isEmpty() ? "" : node.path + "$") + entry.getKey();
                Object val = row.get(alias);
                if (val != null) {
                    accessor.setter().accept(instance, val);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 典型使用示例

```java
FlexSmartQuery.of(User.class)
    .bind(UserVO.class)
    .registerFactory(Address.class, (row, prefix) -> {
        String json = (String) row.get(prefix + "addressJson");
        return JsonUtils.fromJson(json, Address.class);
    })
    .list();
```

### 解决的问题
- 允许开发者注册自定义实体创建工厂
- 支持处理复杂的 JSON 字段反序列化场景
- 通过函数式接口简化工厂定义

---

## 6. 门面模式（Facade Pattern）

### 实现位置
`cn.dreamtof.query.core.FlexSmartQuery`

### 示例代码

```java
public class FlexSmartQuery<E, R> {
    
    // 简化的门面 API
    public List<R> list() {
        String sql = buildSql();
        List<Map<String, Object>> rows = executeQuery(sql);
        return new SmartQueryAssembler<>(structure).reconstruct(rows);
    }

    public Page<R> page(int pageNum, int pageSize) {
        long total = count();
        List<R> records = offset((pageNum - 1) * pageSize).limit(pageSize).list();
        return new Page<>(records, pageNum, pageSize, total);
    }

    public CursorResult<R> seek(Object lastId, int pageSize) {
        // 游标分页实现
    }

    public R one() {
        return limit(1).list().stream().findFirst().orElse(null);
    }

    public long count() {
        // 计数查询
    }
}
```

### 隐藏的复杂逻辑

```java
private String buildSql() {
    // 1. 构建 SELECT 子句
    applySelectColumns(queryWrapper, structure.getRootNode());
    
    // 2. 构建 FROM 子句
    queryWrapper.from(structure.getRootNode().tableInfo.getTableName())
                .as(structure.getRootNode().tableAlias);
    
    // 3. 构建 LEFT JOIN 子句
    applyJoins(queryWrapper, structure.getRootNode());
    
    // 4. 构建 WHERE 子句
    applyConditions(queryWrapper);
    
    // 5. 构建 ORDER BY 子句
    applyOrderBy(queryWrapper);
    
    return queryWrapper.toSQL();
}
```

### 解决的问题
- 提供简洁的链式 API 隐藏底层复杂性
- 内部协调 Structure、Plan、Assembler 等多个子系统
- 用户只需关注业务语义，无需关心 SQL 细节

---

## 7. 解释器模式（Interpreter Pattern）

### 实现位置
`cn.dreamtof.query.structure.SmartQueryStructure`

### 示例代码

```java
public class SmartQueryStructure {
    
    public void applyToWrapper(QueryWrapper queryWrapper, JoinNode node) {
        // 解释 SELECT 子句
        for (var entry : node.selectFields.entrySet()) {
            String alias = (node.path.isEmpty() ? "" : node.path + "$") + entry.getKey();
            queryWrapper.select(new QueryColumn(node.tableAlias, entry.getValue()).as(alias));
        }

        // 解释 LEFT JOIN 子句
        for (var entry : node.children.entrySet()) {
            JoinNode child = entry.getValue();
            queryWrapper.leftJoin(child.tableInfo.getTableName())
                       .as(child.tableAlias)
                       .on(new QueryColumn(node.tableAlias, node.pkColName)
                           .eq(new QueryColumn(child.tableAlias, child.linkCol)));
            
            // 递归解释子节点
            applyToWrapper(queryWrapper, child);
        }

        // 处理延迟加载节点
        for (JoinNode deferred : node.deferredChildren) {
            // 标记为延迟加载
        }
    }
}
```

### 注解语法规则

```java
@SmartFetch(type = FetchType.EAGER)
private List<Order> orders;

@Relation(oneToOne = true, joinColumn = "address_id")
private Address address;

@QueryMapping(column = "user_name")
private String userName;
```

### 解决的问题
- JoinNode 树作为抽象语法树（AST）
- 递归遍历 AST 并翻译为 MyBatis-Flex 的 QueryWrapper
- 支持 @Relation、@SmartFetch 等注解作为语法规则

---

## 8. 迭代器模式（Iterator Pattern）

### 实现位置
`cn.dreamtof.query.core.SmartQueryAssembler`

### 示例代码

```java
public class SmartQueryAssembler<R> {
    
    public List<R> reconstruct(List<Map<String, Object>> rows) {
        Map<Object, R> rootMap = new LinkedHashMap<>(rows.size());
        JoinNode rootNode = structure.getRootNode();
        String rootPkAlias = rootNode.path.isEmpty() 
            ? rootNode.pkColName 
            : rootNode.path + "$" + rootNode.pkColName;

        for (Map<String, Object> row : rows) {
            Object pk = row.get(rootPkAlias);
            R rootVo = rootMap.computeIfAbsent(pk, k -> buildNodeObject(row, rootNode));
            fillRecursive(rootVo, row, rootNode, contextCache);
        }

        return new ArrayList<>(rootMap.values());
    }

    private void fillRecursive(Object currentVo, Map<String, Object> row, 
                               JoinNode currentNode, Map<String, Object> context) {
        for (Map.Entry<String, JoinNode> entry : currentNode.children.entrySet()) {
            String propName = entry.getKey();
            JoinNode childNode = entry.getValue();
            
            Object childPk = row.get(childPkAlias);
            if (childPk == null) continue;

            Object childObj = buildOrGetChild(currentVo, propName, childNode, childPk, row);
            fillRecursive(childObj, row, childNode, context);
        }
    }
}
```

### 解决的问题
- 统一遍历扁平数据并构建树形结构
- 支持深层嵌套的递归迭代
- 使用 IdentityHashMap 处理循环引用

---

## 9. 代理模式（Proxy Pattern）

### 实现位置
`cn.dreamtof.query.core.FlexSmartQuery`

### 示例代码

```java
public class FlexSmartQuery<E, R> {
    private final QueryWrapper queryWrapper;

    // 代理 QueryWrapper 的原生方法
    public FlexSmartQuery<E, R> where(QueryCondition condition) {
        addCondition(condition);
        return this;
    }

    public FlexSmartQuery<E, R> and(Consumer<QueryWrapper> consumer) {
        this.queryWrapper.and(consumer);
        return this;
    }

    public FlexSmartQuery<E, R> or(Consumer<QueryWrapper> consumer) {
        this.queryWrapper.or(consumer);
        return this;
    }

    public FlexSmartQuery<E, R> orderBy(QueryColumn column, boolean isAsc) {
        this.queryWrapper.orderBy(column, isAsc);
        return this;
    }

    // 提供对原生对象的访问
    public QueryWrapper getRawWrapper() {
        return this.queryWrapper;
    }
}
```

### 解决的问题
- 在 QueryWrapper 基础上增加智能推断能力
- 保持对原生 API 的兼容性
- 增强功能而不修改原有实现

---

## 设计模式总结

| 设计模式 | 核心应用场景 |
|---------|-------------|
| 建造者模式 | 流式查询 API 构建 |
| 组合模式 | 树形 Join 节点结构 |
| 命令模式 | 预编译执行计划 |
| 策略模式 | 自动匹配策略推断 |
| 工厂方法模式 | 自定义实体创建 |
| 门面模式 | 简化查询入口 |
| 解释器模式 | AST 解析生成 SQL |
| 迭代器模式 | 扁平数据构建树形结构 |
| 代理模式 | QueryWrapper 增强 |

---

## 架构流程图

```
┌─────────────────────────────────────────────────────────────────┐
│                        业务代码                                   │
│   FlexSmartQuery.of(User.class).bind(UserVO.class).list()      │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                 FlexSmartQuery (门面 + 建造者)                   │
│              协调各子系统完成查询构建                             │
└───────────────────────────┬─────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│SmartQueryPlan │   │SmartQueryStructure│SmartQueryAssembler│
│  (命令模式)    │   │  (组合+解释器)  │   │  (迭代器)      │
│               │   │               │   │               │
│ 预编译执行步骤 │   │ JoinNode树    │   │ 结果树形组装   │
└───────┬───────┘   └───────┬───────┘   └───────┬───────┘
        │                   │                   │
        │           ┌───────┴───────┐           │
        │           ▼               ▼           │
        │   ┌─────────────┐ ┌─────────────┐     │
        │   │MatchType    │ │EntityFactory│     │
        │   │ (策略模式)   │ │(工厂方法)   │     │
        │   └─────────────┘ └─────────────┘     │
        │                                       │
        └───────────────────┬───────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    QueryWrapper (代理模式)                       │
│                    MyBatis-Flex 查询封装                        │
└─────────────────────────────────────────────────────────────────┘
```
