# dreamtof-query 模块设计模式分析

## 模块概述

dreamtof-query 提供智能查询构建能力，支持自动推断查询条件、关联查询、结果组装等功能。

---

## 模块架构流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              dreamtof-query 架构总览                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          业务代码层 (门面)                               │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │   FlexSmartQuery.of(User.class)                                  │   │   │
│  │  │       .bind(UserVO.class)                                        │   │   │
│  │  │       .autoBuild(queryDto)                                       │   │   │
│  │  │       .page(1, 10);                                              │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          查询构建层                                     │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐      │   │
│  │  │ FlexSmartQuery   │  │ SmartQueryPlan   │  │SmartQueryStructure│      │   │
│  │  │   (建造者+代理)   │  │   (命令模式)      │  │  (组合+解释器)    │      │   │
│  │  │                  │  │                  │  │                  │      │   │
│  │  │ 链式配置         │  │ 预编译执行计划    │  │ JoinNode树结构   │      │   │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘      │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          策略选择层                                     │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │                    MatchType (策略模式)                           │   │   │
│  │  │                                                                   │   │   │
│  │  │   EQ / NE / GT / GE / LT / LE / LIKE / IN / IS_NULL ...          │   │   │
│  │  │                                                                   │   │   │
│  │  │   自动推断: 字段名后缀 → 匹配策略                                  │   │   │
│  │  │   nameLike → LIKE, idList → IN, createTimeBegin → GE             │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          结果组装层                                     │   │
│  │  ┌──────────────────┐  ┌──────────────────┐                            │   │
│  │  │SmartQueryAssembler│  │  EntityFactory   │                            │   │
│  │  │   (迭代器模式)    │  │  (工厂方法模式)   │                            │   │
│  │  │                  │  │                  │                            │   │
│  │  │ 扁平数据→树形结构 │  │ 自定义实体创建    │                            │   │
│  │  └──────────────────┘  └──────────────────┘                            │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 1. 建造者模式（Builder Pattern）

### 什么是建造者模式？

建造者模式是一种创建型设计模式，**将复杂对象的构建与其表示分离，使得同样的构建过程可以创建不同的表示**。它通过分步构建的方式，让客户端无需知道对象内部的构建细节。

### 核心特征

1. **分步构建**：将复杂对象的构建分解为多个步骤
2. **链式调用**：支持流式 API，代码更清晰
3. **配置灵活**：可以选择性地设置不同参数
4. **封装细节**：客户端无需了解对象内部结构

### 为什么这是建造者模式而不是工厂模式？

| 对比维度 | 建造者模式 | 工厂模式 |
|---------|-----------|---------|
| **关注点** | 如何构建复杂对象 | 如何创建对象 |
| **创建过程** | 分步构建，可控制细节 | 一步创建，隐藏细节 |
| **返回时机** | 构建完成后才返回产品 | 立即返回完整产品 |
| **适用场景** | 复杂对象、多参数配置 | 简单对象、类型选择 |

**判断依据**：`FlexSmartQuery` 支持链式配置（bind、autoBuild、useOr、where、orderBy），每个方法返回 `this` 以支持继续配置，最后调用 `list()` 或 `page()` 才真正执行查询。这种**分步配置、延迟执行**的方式是建造者模式的典型特征。

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

    public FlexSmartQuery<E, R> useOr() {
        this.useOrLogic = true;
        return this;
    }

    public FlexSmartQuery<E, R> where(QueryCondition condition) {
        addCondition(condition);
        return this;
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              建造者模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    业务代码构建查询                                                              │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  FlexSmartQuery 建造过程                              │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              步骤1: 创建建造者                              │    │     │
│    │   │                                                           │    │     │
│    │   │   FlexSmartQuery.of(User.class)                           │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► 私有构造器创建实例                              │    │     │
│    │   │               new FlexSmartQuery<>(User.class, User.class)│    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              步骤2: 链式配置参数                            │    │     │
│    │   │                                                           │    │     │
│    │   │   .bind(UserVO.class)                                     │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► 解析 VO 树形结构，创建新实例                    │    │     │
│    │   │                                                           │    │     │
│    │   │   .autoBuild(queryDto)                                    │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► 根据 DTO 自动构建查询条件                       │    │     │
│    │   │                                                           │    │     │
│    │   │   .useOr()                                                │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► this.useOrLogic = true; return this;           │    │     │
│    │   │                                                           │    │     │
│    │   │   .where(condition)                                       │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► addCondition(condition); return this;          │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              步骤3: 执行构建                               │    │     │
│    │   │                                                           │    │     │
│    │   │   .page(1, 10)                                            │    │     │
│    │   │       │                                                   │    │     │
│    │   │       ▼                                                   │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ 构建流程:                                        │    │    │     │
│    │   │   │                                                   │    │    │     │
│    │   │   │ 1. buildSql() - 生成 SQL                         │    │    │     │
│    │   │   │ 2. executeQuery() - 执行查询                     │    │    │     │
│    │   │   │ 3. reconstruct() - 组装结果                      │    │    │     │
│    │   │   │ 4. return Page<UserVO>                           │    │    │     │
│    │   │   │                                                   │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    完整调用示例：                                                                │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                                                                     │     │
│    │   Page<UserVO> page = FlexSmartQuery.of(User.class)                 │     │
│    │       .bind(UserVO.class)                                           │     │
│    │       .autoBuild(queryDto)                                          │     │
│    │       .useOr()                                                      │     │
│    │       .where(User::getStatus, Status.ACTIVE)                        │     │
│    │       .orderBy(User::getCreateTime, false)                          │     │
│    │       .page(1, 10);                                                 │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 提供流畅的 DSL 风格 API
- 支持复杂查询的渐进式构建
- 私有构造器强制使用静态工厂方法

---

## 2. 组合模式（Composite Pattern）

### 什么是组合模式？

组合模式是一种结构型设计模式，**将对象组合成树形结构以表示"部分-整体"的层次结构**。它让客户端可以统一对待单个对象和组合对象，使得用户不必关心处理的是单个对象还是组合对象。

### 核心特征

1. **树形结构**：对象形成树形层次结构
2. **统一接口**：单个对象和组合对象实现相同接口
3. **递归处理**：操作可以递归应用到整个树
4. **动态组合**：可以动态添加或删除子节点

### 为什么这是组合模式而不是装饰器模式？

| 对比维度 | 组合模式 | 装饰器模式 |
|---------|---------|-----------|
| **结构形态** | 树形结构（部分-整体） | 线性包装链 |
| **目的** | 统一处理单个和组合对象 | 动态添加职责 |
| **子节点数量** | 可以有多个子节点 | 通常只有一个被装饰者 |
| **典型场景** | 文件系统、UI组件树、组织结构 | I/O流、UI组件增强 |

**判断依据**：`JoinNode` 形成**树形结构**，每个节点可以有多个子节点（children Map），支持递归遍历整个树。它表示"部分-整体"关系（User → Orders → Items），客户端可以统一处理单个节点和整个子树，这符合组合模式的定义。

### 实现位置
`cn.dreamtof.query.structure.SmartQueryStructure`

### 示例代码

```java
public class SmartQueryStructure {
    public static class JoinNode {
        public String path;
        public String tableAlias;
        public TableInfo tableInfo;
        
        public Map<String, String> selectFields = new LinkedHashMap<>();
        public Map<String, JoinNode> children = new LinkedHashMap<>();
        public List<JoinNode> deferredChildren = new ArrayList<>();
        
        public boolean isCollection = false;
    }

    public void parseVoTree(Class<?> voClass, JoinNode node, int collectionDepth) {
        for (VoFieldMeta meta : SmartQueryContext.getVoFields(voClass)) {
            if (meta.smartFetch() != null) {
                processSmartFetch(meta, node, collectionDepth);
            }
        }
    }

    private void processSmartFetch(VoFieldMeta meta, JoinNode parentNode, int collectionDepth) {
        JoinNode child = new JoinNode();
        child.path = parentNode.path.isEmpty() ? meta.name() : parentNode.path + "$" + meta.name();
        child.isCollection = Collection.class.isAssignableFrom(meta.type());
        
        parentNode.children.put(meta.name(), child);
        parseVoTree(meta.fieldType(), child, nextDepth);
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              组合模式流程                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    VO 类定义                                                                     │
│    class UserVO { List<OrderVO> orders; }                                      │
│    class OrderVO { List<OrderItemVO> items; }                                  │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  JoinNode 树形结构构建                                │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              parseVoTree() 递归解析                        │    │     │
│    │   │                                                           │    │     │
│    │   │   解析 UserVO                                             │    │     │
│    │   │       │                                                   │    │     │
│    │   │       ├──► 普通字段 → selectFields                        │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► @SmartFetch 字段 → 创建子 JoinNode             │    │     │
│    │   │               │                                           │    │     │
│    │   │               ▼                                           │    │     │
│    │   │           解析 OrderVO (递归)                              │    │     │
│    │   │               │                                           │    │     │
│    │   │               └──► 解析 OrderItemVO (递归)                 │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              生成的 JoinNode 树                            │    │     │
│    │   │                                                           │    │     │
│    │   │   JoinNode (User)                                         │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ path: ""                                          │    │    │     │
│    │   │   │ tableAlias: "t0"                                  │    │    │     │
│    │   │   │ selectFields: {id, name, email, ...}             │    │    │     │
│    │   │   │ children: {                                       │    │    │     │
│    │   │   │   orders: JoinNode (Order) ─────────────────┐    │    │    │     │
│    │   │   │ }                                            │    │    │    │     │
│    │   │   └──────────────────────────────────────────────┼────┘    │    │     │
│    │   │                                                   │         │    │     │
│    │   │                                                   ▼         │    │     │
│    │   │   JoinNode (Order)                               ┌────────┐ │    │     │
│    │   │   ┌─────────────────────────────────────────┐    │        │ │    │     │
│    │   │   │ path: "orders"                           │    │        │ │    │     │
│    │   │   │ tableAlias: "t1"                         │    │        │ │    │     │
│    │   │   │ isCollection: true                       │    │        │ │    │     │
│    │   │   │ selectFields: {id, orderNo, ...}         │    │        │ │    │     │
│    │   │   │ children: {                              │    │        │ │    │     │
│    │   │   │   items: JoinNode (OrderItem) ──────┐    │    │        │ │    │     │
│    │   │   │ }                                   │    │    │        │ │    │     │
│    │   │   └─────────────────────────────────────┼────┘    │        │ │    │     │
│    │   │                                           │         │        │ │    │     │
│    │   │                                           ▼         │        │ │    │     │
│    │   │   JoinNode (OrderItem)                    ┌────────┐│        │ │    │     │
│    │   │   ┌─────────────────────────────────┐    │        ││        │ │    │     │
│    │   │   │ path: "orders$items"             │    │        ││        │ │    │     │
│    │   │   │ tableAlias: "t2"                 │    │        ││        │ │    │     │
│    │   │   │ isCollection: true               │    │        ││        │ │    │     │
│    │   │   │ selectFields: {id, productId, ...}│   │        ││        │ │    │     │
│    │   │   │ children: {}                     │    │        ││        │ │    │     │
│    │   │   └─────────────────────────────────┘    │        ││        │ │    │     │
│    │   │                                           └────────┘│        │ │    │     │
│    │   │                                                     └────────┘ │    │     │
│    │   │                                                               │    │     │
│    │   └───────────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    组合模式优势：                                                                │
│    1. 统一处理单个对象和集合类型                                                 │
│    2. 支持无限层级的对象关联                                                     │
│    3. 递归遍历树形结构                                                          │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 将 VO 的嵌套结构映射为树形 Join 节点
- 支持无限层级的对象关联（如 User → Orders → Items）
- 统一处理单个对象和集合类型

---

## 3. 命令模式（Command Pattern）

### 什么是命令模式？

命令模式是一种行为型设计模式，**将请求封装为对象，从而让你可以用不同的请求对客户进行参数化、对请求排队或记录请求日志，以及支持可撤销的操作**。它将"请求"本身变成一个对象。

### 核心特征

1. **请求封装**：将请求封装为独立的命令对象
2. **调用解耦**：调用者不需要知道接收者的具体实现
3. **可组合**：多个命令可以组合成复合命令
4. **可撤销**：支持撤销和重做操作

### 为什么这是命令模式而不是策略模式？

| 对比维度 | 命令模式 | 策略模式 |
|---------|---------|---------|
| **关注点** | 请求的封装和执行 | 算法的封装和替换 |
| **执行时机** | 可以延迟执行、排队执行 | 立即执行选定的策略 |
| **可撤销性** | 支持撤销/重做 | 不支持撤销 |
| **典型场景** | 菜单操作、事务操作、宏命令 | 排序算法、支付方式 |

**判断依据**：`SmartQueryPlan` 将每个 DTO 字段的处理逻辑**封装为独立的命令**（BiConsumer），这些命令可以被缓存、复用，并按顺序执行。它实现了"预编译"——首次使用时构建命令链，后续直接执行缓存的命令，这符合命令模式的定义。

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
            String fieldName = accessor.field().getName();
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

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              命令模式流程                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    首次使用 DTO 查询                                                             │
│    SmartQueryPlan.getOrCreate(UserQueryDto.class)                              │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      SmartQueryPlan 构建                             │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              build() 预编译过程                             │    │     │
│    │   │                                                           │    │     │
│    │   │   1. 解析 DTO 类结构                                       │    │     │
│    │   │      FastBeanMeta meta = FastBeanMeta.of(dtoClass);       │    │     │
│    │   │                                                           │    │     │
│    │   │   2. 遍历每个字段，生成命令                                 │    │     │
│    │   │      for (var accessor : meta.getReadableAccessors()) {   │    │     │
│    │   │          // 为每个字段创建一个命令                          │    │     │
│    │   │          plan.steps.add((engine, dto) -> {                │    │     │
│    │   │              Object val = accessor.getter().apply(dto);   │    │     │
│    │   │              if (val == null) return;                     │    │     │
│    │   │              engine.applyFieldCondition(...);             │    │     │
│    │   │          });                                              │    │     │
│    │   │      }                                                    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              生成的命令列表                                 │    │     │
│    │   │                                                           │    │     │
│    │   │   List<BiConsumer<FlexSmartQuery, Object>> steps:         │    │     │
│    │   │                                                           │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ steps[0]: (engine, dto) -> {                    │    │    │     │
│    │   │   │     val = dto.getName();                        │    │    │     │
│    │   │   │     engine.applyFieldCondition("name", val);    │    │    │     │
│    │   │   │ }                                              │    │    │     │
│    │   │   ├─────────────────────────────────────────────────┤    │    │     │
│    │   │   │ steps[1]: (engine, dto) -> {                    │    │    │     │
│    │   │   │     val = dto.getStatus();                     │    │    │     │
│    │   │   │     engine.applyFieldCondition("status", val); │    │    │     │
│    │   │   │ }                                              │    │    │     │
│    │   │   ├─────────────────────────────────────────────────┤    │    │     │
│    │   │   │ steps[2]: (engine, dto) -> {                    │    │    │     │
│    │   │   │     val = dto.getCreateTimeBegin();            │    │    │     │
│    │   │   │     engine.applyFieldCondition("createTime", val);│   │    │     │
│    │   │   │ }                                              │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              缓存到 PLAN_CACHE                             │    │     │
│    │   │                                                           │    │     │
│    │   │   PLAN_CACHE.put(UserQueryDto.class, plan);               │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    后续调用（直接使用缓存的计划）：                                              │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                                                                     │     │
│    │   SmartQueryPlan plan = PLAN_CACHE.get(UserQueryDto.class);         │     │
│    │   plan.execute(this, queryDto);                                     │     │
│    │                                                                     │     │
│    │   // 遍历执行预编译的命令                                            │     │
│    │   for (var step : steps) {                                          │     │
│    │       step.accept(queryEngine, dto);                                │     │
│    │   }                                                                 │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    性能优势：                                                                    │
│    1. 预编译：首次解析后缓存执行计划                                             │
│    2. 避免：后续调用无需反射解析                                                 │
│    3. 高效：直接执行预编译的命令                                                 │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 将 DTO 字段的处理逻辑封装为命令对象
- 支持预编译：首次使用时构建命令链，后续直接执行
- 将"解释执行"优化为"预编译执行"，大幅提升性能

---

## 4. 策略模式（Strategy Pattern）

### 什么是策略模式？

策略模式是一种行为型设计模式，**定义一系列算法，将每个算法封装起来，并使它们可以互相替换**。策略模式让算法独立于使用它的客户端而变化，客户端可以根据需要选择不同的策略。

### 核心特征

1. **算法封装**：每个策略都是一个独立的算法实现
2. **可互换性**：策略之间可以互相替换
3. **客户端解耦**：客户端不需要知道具体策略的实现细节
4. **运行时选择**：可以在运行时动态切换策略

### 为什么这是策略模式而不是状态模式？

| 对比维度 | 策略模式 | 状态模式 |
|---------|---------|---------|
| **目的** | 算法/行为的封装和替换 | 状态驱动的行为变化 |
| **触发方式** | 客户端主动选择策略 | 状态变化自动触发行为 |
| **相互关系** | 策略之间相互独立 | 状态之间有转换关系 |
| **典型场景** | 排序算法、支付方式、匹配类型 | 订单状态、工作流状态 |

**判断依据**：`MatchType` 定义了一系列**独立的匹配算法**（EQ、LIKE、IN、GE等），这些策略之间没有状态转换关系，客户端根据字段命名约定或类型**主动选择**对应的策略。每种策略独立封装，可以自由替换，这符合策略模式的定义。

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
        
        Class<?> type = meta.type();
        if (Collection.class.isAssignableFrom(type)) return MatchType.IN;
        if (String.class.isAssignableFrom(type)) return MatchType.LIKE;
        
        return MatchType.EQ;
    }

    private QueryCondition buildBaseCondition(QueryColumn col, Object val, MatchType type) {
        return switch (type) {
            case EQ -> col.eq(val);
            case NE -> col.ne(val);
            case LIKE -> col.like(val);
            case GE -> col.ge(val);
            case LE -> col.le(val);
            case IN -> col.in((Collection<?>) val);
            default -> null;
        };
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              策略模式流程                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    DTO 字段处理                                                                  │
│    applyFieldCondition(fieldName, value, meta)                                 │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      策略推断                                        │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              resolveMatchType() 策略选择                    │    │     │
│    │   │                                                           │    │     │
│    │   │   字段名推断:                                              │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ nameLike        → LIKE                          │    │    │     │
│    │   │   │ createTimeBegin → GE                             │    │    │     │
│    │   │   │ createTimeEnd   → LE                             │    │    │     │
│    │   │   │ idList          → IN                             │    │    │     │
│    │   │   │ statusNot       → NE                             │    │    │     │
│    │   │   │ userIds         → IN                             │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   │   类型推断:                                                │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ Collection 类型 → IN                             │    │    │     │
│    │   │   │ String 类型     → LIKE                           │    │    │     │
│    │   │   │ 其他类型       → EQ                              │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              策略执行                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   buildBaseCondition(column, value, matchType)            │    │     │
│    │   │                                                           │    │     │
│    │   │   switch (matchType) {                                    │    │     │
│    │   │       case EQ    → column.eq(val)                         │    │     │
│    │   │       case NE    → column.ne(val)                         │    │     │
│    │   │       case GT    → column.gt(val)                         │    │     │
│    │   │       case GE    → column.ge(val)                         │    │     │
│    │   │       case LT    → column.lt(val)                         │    │     │
│    │   │       case LE    → column.le(val)                         │    │     │
│    │   │       case LIKE  → column.like(val)                       │    │     │
│    │   │       case IN    → column.in((Collection<?>) val)         │    │     │
│    │   │   }                                                      │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              生成的 SQL 条件                                │    │     │
│    │   │                                                           │    │     │
│    │   │   nameLike = "张"        → name LIKE '%张%'               │    │     │
│    │   │   createTimeBegin = "2024-01-01" → create_time >= '2024-01-01'│
│    │   │   idList = [1,2,3]       → id IN (1, 2, 3)                │    │     │
│    │   │   status = 1             → status = 1                     │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    策略模式优势：                                                                │
│    1. 根据字段命名约定自动推断查询策略                                           │
│    2. 支持 10+ 种匹配策略                                                       │
│    3. 可通过 override() 方法手动指定策略                                        │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 根据字段命名约定自动推断查询策略
- 支持 10+ 种匹配策略
- 可通过 override() 方法手动指定策略

---

## 5. 工厂方法模式（Factory Method Pattern）

### 什么是工厂方法模式？

工厂方法模式是一种创建型设计模式，**定义一个创建对象的接口，但由子类决定要实例化的类是哪一个**。工厂方法让类把实例化推迟到子类，客户端无需知道具体创建的类。

### 核心特征

1. **抽象创建**：定义抽象的工厂接口
2. **延迟实例化**：由子类决定具体实例化哪个类
3. **解耦客户端**：客户端只依赖抽象接口
4. **扩展方便**：新增产品只需新增工厂实现

### 为什么这是工厂方法模式而不是简单工厂？

| 对比维度 | 工厂方法模式 | 简单工厂 |
|---------|------------|---------|
| **工厂结构** | 抽象工厂 + 具体工厂 | 单一工厂类 |
| **扩展方式** | 新增工厂实现 | 修改工厂方法 |
| **开闭原则** | 符合开闭原则 | 需修改已有代码 |
| **典型场景** | 框架级扩展点 | 简单对象创建 |

**判断依据**：`EntityFactory` 是一个**函数式接口**，定义了创建实体的抽象方法。业务代码可以注册自定义工厂实现，`SmartQueryAssembler` 根据实体类型选择对应的工厂创建实例。这种"定义抽象工厂接口，由具体实现决定实例化"的方式符合工厂方法模式的定义。

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

    private <T> T buildNodeObject(Map<String, Object> row, JoinNode node) {
        EntityFactory<?> factory = entityFactoryRegistry.get(node.entityClass);
        if (factory != null) {
            return (T) factory.create(row, node.path.replace("$", ".") + ".");
        }

        // 默认工厂实现
        T instance = (T) node.entityClass.getDeclaredConstructor().newInstance();
        // ... 设置属性
        return instance;
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              工厂方法模式流程                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    结果组装需要创建实体                                                          │
│    buildNodeObject(row, node)                                                  │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  EntityFactory 注册表                                │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              注册自定义工厂                                  │    │     │
│    │   │                                                           │    │     │
│    │   │   FlexSmartQuery.of(User.class)                           │    │     │
│    │   │       .registerFactory(Address.class, (row, prefix) -> {   │    │     │
│    │   │           String json = (String) row.get(prefix + "json");│    │     │
│    │   │           return JsonUtils.fromJson(json, Address.class); │    │     │
│    │   │       })                                                  │    │     │
│    │   │       .registerFactory(Order.class, (row, prefix) -> {     │    │     │
│    │   │           // 自定义 Order 创建逻辑                          │    │     │
│    │   │       });                                                 │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              工厂选择                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   EntityFactory<?> factory = entityFactoryRegistry        │    │     │
│    │   │       .get(node.entityClass);                             │    │     │
│    │   │                                                           │    │     │
│    │   │   if (factory != null) {                                  │    │     │
│    │   │       // 使用自定义工厂                                    │    │     │
│    │   │       return factory.create(row, aliasPrefix);            │    │     │
│    │   │   } else {                                                │    │     │
│    │   │       // 使用默认工厂                                      │    │     │
│    │   │       return defaultFactory(row, node);                   │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ├─────────────────────────┬───────────────────────┐      │     │
│    │           ▼                         ▼                       ▼      │     │
│    │   ┌─────────────┐           ┌─────────────┐         ┌─────────────┐│     │
│    │   │自定义工厂   │           │自定义工厂   │         │默认工厂     ││     │
│    │   │(Address)    │           │(Order)      │         │(其他实体)   ││     │
│    │   │             │           │             │         │             ││     │
│    │   │JSON反序列化 │           │复杂逻辑创建 │         │反射创建实例 ││     │
│    │   │             │           │             │         │设置属性     ││     │
│    │   └─────────────┘           └─────────────┘         └─────────────┘│     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    工厂方法优势：                                                                │
│    1. 允许业务代码自定义实体创建逻辑                                            │
│    2. 支持处理复杂的 JSON 字段反序列化场景                                       │
│    3. 通过函数式接口简化工厂定义                                                │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 允许开发者注册自定义实体创建工厂
- 支持处理复杂的 JSON 字段反序列化场景
- 通过函数式接口简化工厂定义

---

## 6. 门面模式（Facade Pattern）

### 什么是门面模式？

门面模式是一种结构型设计模式，**为子系统中的一组接口提供一个统一的高层接口，使得子系统更容易使用**。它隐藏了子系统的复杂性，为客户端提供一个简化的入口。

### 核心特征

1. **简化接口**：提供简化的高层接口
2. **隐藏复杂性**：封装子系统的复杂交互
3. **解耦客户端**：客户端与子系统解耦
4. **分层设计**：有助于建立分层架构

### 为什么这是门面模式而不是中介者模式？

| 对比维度 | 门面模式 | 中介者模式 |
|---------|---------|-----------|
| **交互方式** | 单向调用（客户端→门面→子系统） | 双向通信（同事对象↔中介者） |
| **目的** | 简化接口，隐藏复杂性 | 协调多个对象间的交互 |
| **依赖关系** | 客户端依赖门面 | 同事对象依赖中介者 |
| **典型场景** | API网关、服务入口 | 聊天室、GUI组件协调 |

**判断依据**：`FlexSmartQuery` 提供简化的 API（list、page、one、count），内部协调 Structure、Plan、Assembler 等多个子系统完成复杂查询。客户端只需调用门面方法，无需了解内部的 SQL 构建、结果组装等复杂逻辑。这种**单向简化入口**的设计符合门面模式的定义。

### 实现位置
`cn.dreamtof.query.core.FlexSmartQuery`

### 示例代码

```java
public class FlexSmartQuery<E, R> {
    
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
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              门面模式流程                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    业务代码调用（简洁的门面 API）                                                │
│    FlexSmartQuery.of(User.class).bind(UserVO.class).page(1, 10)                │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  FlexSmartQuery (门面)                               │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              简化的门面 API                                  │    │     │
│    │   │                                                           │    │     │
│    │   │   list()      → List<R>                                   │    │     │
│    │   │   page(n, size) → Page<R>                                 │    │     │
│    │   │   seek(lastId, size) → CursorResult<R>                    │    │     │
│    │   │   one()      → R                                          │    │     │
│    │   │   count()    → long                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              隐藏的复杂逻辑                                 │    │     │
│    │   │                                                           │    │     │
│    │   │   buildSql() 内部流程:                                     │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ 1. 构建 SELECT 子句                              │    │    │     │
│    │   │   │    applySelectColumns(queryWrapper, rootNode)   │    │    │     │
│    │   │   │                                                     │    │    │     │
│    │   │   │ 2. 构建 FROM 子句                                  │    │    │     │
│    │   │   │    queryWrapper.from(tableName).as(alias)        │    │    │     │
│    │   │   │                                                     │    │    │     │
│    │   │   │ 3. 构建 LEFT JOIN 子句                             │    │    │     │
│    │   │   │    applyJoins(queryWrapper, rootNode)            │    │    │     │
│    │   │   │                                                     │    │    │     │
│    │   │   │ 4. 构建 WHERE 子句                                  │    │    │     │
│    │   │   │    applyConditions(queryWrapper)                  │    │    │     │
│    │   │   │                                                     │    │    │     │
│    │   │   │ 5. 构建 ORDER BY 子句                               │    │    │     │
│    │   │   │    applyOrderBy(queryWrapper)                     │    │    │     │
│    │   │   │                                                     │    │    │     │
│    │   │   │ 6. 防笛卡尔积保护                                    │    │    │     │
│    │   │   │    checkCartesianProduct()                        │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   │   reconstruct() 内部流程:                                 │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ 1. 遍历扁平数据行                                 │    │    │     │
│    │   │   │ 2. 根据 JoinNode 树构建对象                       │    │    │     │
│    │   │   │ 3. 处理集合类型字段                               │    │    │     │
│    │   │   │ 4. 去重合并相同主键的对象                         │    │    │     │
│    │   │   │ 5. 返回树形结构结果                               │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    门面模式优势：                                                                │
│    1. 提供简洁的链式 API 隐藏底层复杂性                                         │
│    2. 内部协调 Structure、Plan、Assembler 等多个子系统                          │
│    3. 用户只需关注业务语义，无需关心 SQL 细节                                    │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 提供简洁的链式 API 隐藏底层复杂性
- 内部协调 Structure、Plan、Assembler 等多个子系统
- 用户只需关注业务语义，无需关心 SQL 细节

---

## 7. 解释器模式（Interpreter Pattern）

### 什么是解释器模式？

解释器模式是一种行为型设计模式，**给定一个语言，定义它的文法的一种表示，并定义一个解释器，该解释器使用该表示来解释语言中的句子**。它将语法规则表示为类层次结构，通过解释器执行语法树。

### 核心特征

1. **语法表示**：将语法规则表示为类或对象
2. **抽象语法树**：构建 AST 表示语言结构
3. **递归解释**：递归遍历 AST 执行解释
4. **可扩展语法**：易于添加新的语法规则

### 为什么这是解释器模式而不是访问者模式？

| 对比维度 | 解释器模式 | 访问者模式 |
|---------|-----------|-----------|
| **目的** | 解释执行语言/语法 | 分离数据结构与操作 |
| **结构** | AST + 解释器 | 元素 + 访问者 |
| **操作定义** | 操作在节点类中定义 | 操作在访问者类中定义 |
| **典型场景** | SQL解析、正则表达式、配置解析 | 编译器优化、文档处理 |

**判断依据**：`JoinNode` 树作为**抽象语法树（AST）**，`applyToWrapper()` 方法作为解释器，递归遍历 AST 并将其**翻译**为 MyBatis-Flex 的 QueryWrapper（SQL）。注解（@Relation、@SmartFetch）作为语法规则，这符合解释器模式的定义。

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
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              解释器模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    JoinNode 树 (抽象语法树 AST)                                                  │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  applyToWrapper() 解释执行                            │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              AST 结构 (JoinNode 树)                         │    │     │
│    │   │                                                           │    │     │
│    │   │   JoinNode (User)                                         │    │     │
│    │   │   ├── selectFields: {id, name, email}                     │    │     │
│    │   │   └── children:                                           │    │     │
│    │   │       └── orders: JoinNode (Order)                        │    │     │
│    │   │           ├── selectFields: {id, orderNo}                 │    │     │
│    │   │           └── children:                                   │    │     │
│    │   │               └── items: JoinNode (OrderItem)             │    │     │
│    │   │                   └── selectFields: {id, productId}       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              解释 SELECT 子句                               │    │     │
│    │   │                                                           │    │     │
│    │   │   for (entry : node.selectFields) {                       │    │     │
│    │   │       String alias = path + "$" + key;                    │    │     │
│    │   │       queryWrapper.select(                                │    │     │
│    │   │           new QueryColumn(tableAlias, value).as(alias)    │    │     │
│    │   │       );                                                  │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   生成:                                                   │    │     │
│    │   │   SELECT t0.id, t0.name, t0.email,                        │    │     │
│    │   │          t1.id AS orders$id, t1.order_no AS orders$order_no,│   │     │
│    │   │          t2.id AS orders$items$id, ...                    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              解释 LEFT JOIN 子句 (递归)                     │    │     │
│    │   │                                                           │    │     │
│    │   │   for (entry : node.children) {                           │    │     │
│    │   │       JoinNode child = entry.getValue();                  │    │     │
│    │   │       queryWrapper.leftJoin(child.tableName)              │    │     │
│    │   │           .as(child.tableAlias)                           │    │     │
│    │   │           .on(parentCol.eq(childCol));                    │    │     │
│    │   │                                                           │    │     │
│    │   │       // 递归解释子节点                                    │    │     │
│    │   │       applyToWrapper(queryWrapper, child);                │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   生成:                                                   │    │     │
│    │   │   FROM user t0                                            │    │     │
│    │   │   LEFT JOIN orders t1 ON t0.id = t1.user_id               │    │     │
│    │   │   LEFT JOIN order_item t2 ON t1.id = t2.order_id          │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              最终生成的 SQL                                 │    │     │
│    │   │                                                           │    │     │
│    │   │   SELECT t0.id, t0.name, t0.email,                        │    │     │
│    │   │          t1.id AS orders$id, t1.order_no AS orders$order_no,│   │     │
│    │   │          t2.id AS orders$items$id, t2.product_id          │    │     │
│    │   │   FROM user t0                                            │    │     │
│    │   │   LEFT JOIN orders t1 ON t0.id = t1.user_id               │    │     │
│    │   │   LEFT JOIN order_item t2 ON t1.id = t2.order_id          │    │     │
│    │   │   WHERE ...                                               │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    解释器模式优势：                                                              │
│    1. JoinNode 树作为抽象语法树（AST）                                          │
│    2. 递归遍历 AST 并翻译为 SQL                                                 │
│    3. 支持 @Relation、@SmartFetch 等注解作为语法规则                            │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- JoinNode 树作为抽象语法树（AST）
- 递归遍历 AST 并翻译为 MyBatis-Flex 的 QueryWrapper
- 支持 @Relation、@SmartFetch 等注解作为语法规则

---

## 8. 迭代器模式（Iterator Pattern）

### 什么是迭代器模式？

迭代器模式是一种行为型设计模式，**提供一种方法顺序访问一个聚合对象中的各个元素，而又不暴露该对象的内部表示**。它将遍历逻辑从聚合对象中分离出来，使得可以用统一的接口遍历不同类型的聚合结构。

### 核心特征

1. **顺序访问**：按顺序访问聚合对象的元素
2. **隐藏内部**：不暴露聚合对象的内部结构
3. **统一接口**：不同聚合结构使用相同的遍历接口
4. **支持多种遍历**：可以定义不同的迭代器实现不同遍历方式

### 为什么这是迭代器模式而不是访问者模式？

| 对比维度 | 迭代器模式 | 访问者模式 |
|---------|-----------|-----------|
| **目的** | 遍历聚合对象 | 对元素执行操作 |
| **控制权** | 客户端控制遍历 | 访问者控制操作 |
| **元素操作** | 迭代器不操作元素 | 访问者操作元素 |
| **典型场景** | 集合遍历、树形结构遍历 | 编译器、文档处理 |

**判断依据**：`SmartQueryAssembler` 的 `reconstruct()` 方法遍历扁平数据行（rows），通过 `fillRecursive()` 递归遍历 JoinNode 树的子节点。它**顺序访问**聚合数据并构建树形结构，隐藏了内部的数据映射逻辑，这符合迭代器模式的定义。

### 实现位置
`cn.dreamtof.query.core.SmartQueryAssembler`

### 示例代码

```java
public class SmartQueryAssembler<R> {
    
    public List<R> reconstruct(List<Map<String, Object>> rows) {
        Map<Object, R> rootMap = new LinkedHashMap<>(rows.size());
        JoinNode rootNode = structure.getRootNode();

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
            
            Object childObj = buildOrGetChild(currentVo, propName, childNode, row);
            fillRecursive(childObj, row, childNode, context);
        }
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              迭代器模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    扁平数据行 (SQL 查询结果)                                                     │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  reconstruct() 结果组装                              │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              扁平数据行示例                                  │    │     │
│    │   │                                                           │    │     │
│    │   │   row1: {id:1, name:"张三", orders$id:101, orders$order_no:"O001"}│
│    │   │   row2: {id:1, name:"张三", orders$id:102, orders$order_no:"O002"}│
│    │   │   row3: {id:2, name:"李四", orders$id:201, orders$order_no:"O003"}│
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              第一层迭代: 遍历所有行                         │    │     │
│    │   │                                                           │    │     │
│    │   │   for (Map<String, Object> row : rows) {                  │    │     │
│    │   │       Object pk = row.get(rootPkAlias);  // 获取主键      │    │     │
│    │   │                                                           │    │     │
│    │   │       R rootVo = rootMap.computeIfAbsent(pk, k -> {       │    │     │
│    │   │           return buildNodeObject(row, rootNode);          │    │     │
│    │   │       });                                                 │    │     │
│    │   │                                                           │    │     │
│    │   │       fillRecursive(rootVo, row, rootNode, context);      │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   处理过程:                                                │    │     │
│    │   │   row1: pk=1 → 创建 User(id=1, name="张三")               │    │     │
│    │   │   row2: pk=1 → 复用 User(id=1)                            │    │     │
│    │   │   row3: pk=2 → 创建 User(id=2, name="李四")               │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              第二层迭代: fillRecursive() 递归填充          │    │     │
│    │   │                                                           │    │     │
│    │   │   for (entry : currentNode.children.entrySet()) {         │    │     │
│    │   │       JoinNode childNode = entry.getValue();              │    │     │
│    │   │                                                           │    │     │
│    │   │       // 获取子对象主键                                    │    │     │
│    │   │       Object childPk = row.get(childPkAlias);             │    │     │
│    │   │       if (childPk == null) continue;                      │    │     │
│    │   │                                                           │    │     │
│    │   │       // 构建或获取子对象                                  │    │     │
│    │   │       Object childObj = buildOrGetChild(...);             │    │     │
│    │   │                                                           │    │     │
│    │   │       // 递归处理子节点                                    │    │     │
│    │   │       fillRecursive(childObj, row, childNode, context);   │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   处理过程:                                                │    │     │
│    │   │   row1: User(1) → 添加 Order(101)                         │    │     │
│    │   │   row2: User(1) → 添加 Order(102)                         │    │     │
│    │   │   row3: User(2) → 添加 Order(201)                         │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              最终结果: 树形结构                             │    │     │
│    │   │                                                           │    │     │
│    │   │   [                                                       │    │     │
│    │   │     User(id=1, name="张三", orders=[                      │    │     │
│    │   │       Order(id=101, orderNo="O001"),                      │    │     │
│    │   │       Order(id=102, orderNo="O002")                       │    │     │
│    │   │     ]),                                                   │    │     │
│    │   │     User(id=2, name="李四", orders=[                      │    │     │
│    │   │       Order(id=201, orderNo="O003")                       │    │     │
│    │   │     ])                                                    │    │     │
│    │   │   ]                                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    迭代器模式优势：                                                              │
│    1. 统一遍历扁平数据并构建树形结构                                             │
│    2. 支持深层嵌套的递归迭代                                                    │
│    3. 使用 IdentityHashMap 处理循环引用                                        │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 统一遍历扁平数据并构建树形结构
- 支持深层嵌套的递归迭代
- 使用 IdentityHashMap 处理循环引用

---

## 9. 代理模式（Proxy Pattern）

### 什么是代理模式？

代理模式是一种结构型设计模式，**为其他对象提供代理以控制对这个对象的访问**。代理对象在客户端和目标对象之间起到中介作用，可以在不修改目标对象代码的情况下，增加额外的功能或控制逻辑。

### 核心特征

1. **控制访问**：代理对象控制对真实对象的访问
2. **接口一致**：代理和真实对象实现相同接口，客户端无感知
3. **增强功能**：在不修改原对象的前提下添加额外行为
4. **延迟加载**：可实现按需创建真实对象

### 为什么这是代理模式而不是装饰器模式？

| 对比维度 | 代理模式 | 装饰器模式 |
|---------|---------|-----------|
| **主要目的** | 控制对象访问 | 动态添加职责 |
| **创建时机** | 代理通常在编译时就确定 | 装饰器可运行时动态组合 |
| **对象关系** | 代理和真实对象生命周期独立 | 装饰器和被装饰者生命周期绑定 |
| **典型场景** | 远程代理、虚拟代理、保护代理 | 功能增强、职责叠加 |

**判断依据**：`FlexSmartQuery` 持有 `QueryWrapper` 实例，代理其原生方法（where、and、orderBy），在调用时增加链式返回支持，并提供 `getRawWrapper()` 访问原生对象。它**控制对 QueryWrapper 的访问**并增强其能力，这符合代理模式的定义。

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

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              代理模式流程                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    业务代码调用                                                                  │
│    FlexSmartQuery.of(User.class).where(condition)                              │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  FlexSmartQuery (代理)                               │     │
│    │                                                                     │     │
│    │   持有被代理对象:                                                    │     │
│    │   private final QueryWrapper queryWrapper;                          │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              代理方法                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   // 代理 where 方法，增加链式返回                          │    │     │
│    │   │   public FlexSmartQuery<E, R> where(QueryCondition cond) {│    │     │
│    │   │       addCondition(cond);  // 可能增加额外逻辑              │    │     │
│    │   │       return this;         // 返回代理对象，支持链式        │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   // 代理 and 方法                                         │    │     │
│    │   │   public FlexSmartQuery<E, R> and(Consumer<QueryWrapper> c){│   │     │
│    │   │       this.queryWrapper.and(c);  // 委托给被代理对象       │    │     │
│    │   │       return this;                                        │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   // 代理 orderBy 方法                                     │    │     │
│    │   │   public FlexSmartQuery<E, R> orderBy(QueryColumn col,    │    │     │
│    │   │                                         boolean isAsc) { │    │     │
│    │   │       this.queryWrapper.orderBy(col, isAsc);              │    │     │
│    │   │       return this;                                        │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              QueryWrapper (被代理对象)                      │    │     │
│    │   │                                                           │    │     │
│    │   │   MyBatis-Flex 原生 QueryWrapper                          │    │     │
│    │   │                                                           │    │     │
│    │   │   where(condition)   → void                               │    │     │
│    │   │   and(consumer)      → void                               │    │     │
│    │   │   orderBy(column, asc) → void                             │    │     │
│    │   │   toSQL()            → String                             │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    代理模式优势：                                                                │
│    1. 在 QueryWrapper 基础上增加智能推断能力                                    │
│    2. 保持对原生 API 的兼容性                                                   │
│    3. 增强功能而不修改原有实现                                                  │
│    4. 提供链式调用支持                                                          │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 在 QueryWrapper 基础上增加智能推断能力
- 保持对原生 API 的兼容性
- 增强功能而不修改原有实现

---

## 设计模式总结

| 设计模式 | 核心应用场景 | 关键类 | 判断依据 |
|---------|-------------|--------|---------|
| 建造者模式 | 流式查询 API 构建 | FlexSmartQuery | 分步配置、延迟执行，非一步创建 |
| 组合模式 | 树形 Join 节点结构 | SmartQueryStructure.JoinNode | 树形结构、部分-整体关系，非线性包装 |
| 命令模式 | 预编译执行计划 | SmartQueryPlan | 请求封装、可缓存复用，非算法替换 |
| 策略模式 | 自动匹配策略推断 | MatchType | 算法独立可互换，非状态驱动 |
| 工厂方法模式 | 自定义实体创建 | EntityFactory | 抽象工厂接口、子类决定实例化 |
| 门面模式 | 简化查询入口 | FlexSmartQuery | 单向简化入口，非双向协调 |
| 解释器模式 | AST 解析生成 SQL | SmartQueryStructure | AST + 解释器翻译，非操作分离 |
| 迭代器模式 | 扁平数据构建树形结构 | SmartQueryAssembler | 顺序遍历聚合数据，非元素操作 |
| 代理模式 | QueryWrapper 增强 | FlexSmartQuery | 控制访问并增强，非职责叠加 |
