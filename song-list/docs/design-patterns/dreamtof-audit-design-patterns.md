# dreamtof-audit 模块设计模式分析

## 模块概述

dreamtof-audit 提供审计日志自动采集能力，通过 AOP 和数据库事件监听实现无侵入式审计。

---

## 模块架构流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              dreamtof-audit 架构总览                            │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          AOP 拦截层                                      │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │                    AutoAuditAspect (代理模式)                     │   │   │
│  │  │                                                                   │   │   │
│  │  │   @Around("@annotation(auditLog)")                               │   │   │
│  │  │   ┌─────────────────────────────────────────────────────────┐    │   │   │
│  │  │   │  1. 识别审计目标 (identify)                              │    │   │   │
│  │  │   │  2. 预抓取旧状态 (fetchBatch)                            │    │   │   │
│  │  │   │  3. 执行业务方法 (joinPoint.proceed)                     │    │   │   │
│  │  │   │  4. 提交审计任务 (submitAuditTask)                       │    │   │   │
│  │  │   └─────────────────────────────────────────────────────────┘    │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          上下文管理层                                    │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │              AuditTransactionContext (上下文模式)                 │   │   │
│  │  │                                                                   │   │   │
│  │  │   ScopedValue<ContextPayload> CONTEXT                            │   │   │
│  │  │   ┌─────────────────────────────────────────────────────────┐    │   │   │
│  │  │   │  ContextPayload:                                        │    │   │   │
│  │  │   │  - module / action                                      │    │   │   │
│  │  │   │  - allowedEntities                                      │    │   │   │
│  │  │   │  - records (审计记录列表)                                │    │   │   │
│  │  │   │  - prefetchedOldStates (预抓取的旧状态)                  │    │   │   │
│  │  │   │  - savepoint (保存点机制)                               │    │   │   │
│  │  │   └─────────────────────────────────────────────────────────┘    │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          数据库事件监听层                                │   │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │   │
│  │  │              GlobalAuditListener (观察者模式)                     │   │   │
│  │  │                                                                   │   │   │
│  │  │   implements InsertListener, UpdateListener                      │   │   │
│  │  │   ┌─────────────────────────────────────────────────────────┐    │   │   │
│  │  │   │  onInsert(entity)  ──► 记录 INSERT 操作                  │    │   │   │
│  │  │   │  onUpdate(entity)  ──► 记录 UPDATE 操作                  │    │   │   │
│  │  │   └─────────────────────────────────────────────────────────┘    │   │   │
│  │  └──────────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│                                      ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                          审计数据处理层                                  │   │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐      │   │
│  │  │ SmartAuditUpdater│  │  DialectSupport  │  │  AuditBeanMeta   │      │   │
│  │  │   (建造者模式)    │  │   (策略模式)      │  │   (适配器模式)    │      │   │
│  │  │                  │  │                  │  │                  │      │   │
│  │  │ 差异比较          │  │ 数据库方言支持    │  │ 元数据适配        │      │   │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘      │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 1. AOP 代理模式（Proxy Pattern）

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

**判断依据**：`AutoAuditAspect` 通过 Spring AOP 在运行时拦截方法调用，**控制对业务方法的访问**（前置抓取旧状态、后置提交审计任务），而不是简单地叠加功能。业务代码本身不感知代理的存在，这符合代理模式"控制访问"的本质。

### 实现位置
`cn.dreamtof.audit.aspect.AutoAuditAspect`

### 示例代码

```java
@Aspect
@Component
@Order(2)
public class AutoAuditAspect {
    @Around("@annotation(auditLog)")
    public Object doAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        AuditTransactionContext.ContextPayload payload = new AuditTransactionContext.ContextPayload(
            auditLog.module(),
            auditLog.action(),
            Arrays.asList(auditLog.entities())
        );

        List<AuditTarget> targets = identify(payload.getAllowedEntities(), joinPoint.getArgs());
        Map<Class<?>, Map<Serializable, Object>> oldSnapshots = fetchBatch(targets);

        return ScopedValue.where(AuditTransactionContext.CONTEXT, payload).call(() -> {
            try {
                Object result = joinPoint.proceed();
                submitAuditTask(payload, oldSnapshots);
                return result;
            } catch (Throwable e) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
        });
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              AOP 代理模式流程                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    业务代码调用                                                                  │
│    userService.updateUser(user)                                                │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      Spring AOP 代理                                 │     │
│    │                                                                     │     │
│    │   检测到 @AuditLog 注解                                              │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              AutoAuditAspect.doAudit()                     │    │     │
│    │   │                                                           │    │     │
│    │   │   ┌─────────────────────────────────────────────────────┐ │    │     │
│    │   │   │ 阶段1: 前置处理                                      │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   │  1. 解析 @AuditLog 注解                              │ │    │     │
│    │   │   │     - module: "用户管理"                             │ │    │     │
│    │   │   │     - action: "更新用户"                             │ │    │     │
│    │   │   │     - entities: [User.class]                         │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   │  2. 识别审计目标 (从方法参数中提取)                   │ │    │     │
│    │   │   │     identify(allowedEntities, args)                  │ │    │     │
│    │   │   │     └──► [AuditTarget(User.class, userId: 1001)]     │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   │  3. 预抓取旧状态快照                                 │ │    │     │
│    │   │   │     fetchBatch(targets)                             │ │    │     │
│    │   │   │     └──► SELECT JSON_OBJECT(...) FROM user WHERE id=?│ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   └─────────────────────────────────────────────────────┘ │    │     │
│    │   │                           │                               │ │    │     │
│    │   │                           ▼                               │ │    │     │
│    │   │   ┌─────────────────────────────────────────────────────┐ │    │     │
│    │   │   │ 阶段2: 执行业务                                      │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   │  ScopedValue.where(CONTEXT, payload).call(() -> {   │ │    │     │
│    │   │   │      return joinPoint.proceed();  // 执行原方法      │ │    │     │
│    │   │   │  })                                                  │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   │  内部触发:                                           │ │    │     │
│    │   │   │  ┌─────────────────────────────────────────────┐    │ │    │     │
│    │   │   │  │ GlobalAuditListener.onUpdate(entity)        │    │ │    │     │
│    │   │   │  │   └──► 记录审计到 ContextPayload            │    │ │    │     │
│    │   │   │  └─────────────────────────────────────────────┘    │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   └─────────────────────────────────────────────────────┘ │    │     │
│    │   │                           │                               │ │    │     │
│    │   │                           ▼                               │ │    │     │
│    │   │   ┌─────────────────────────────────────────────────────┐ │    │     │
│    │   │   │ 阶段3: 后置处理                                      │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   │  1. 收集审计记录                                     │ │    │     │
│    │   │   │     payload.getRecords()                            │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   │  2. 异步提交审计任务                                 │ │    │     │
│    │   │   │     VirtualTaskManager.executeWithLimit(auditTask)  │ │    │     │
│    │   │   │     └──► 写入审计日志表                              │ │    │     │
│    │   │   │                                                     │ │    │     │
│    │   │   └─────────────────────────────────────────────────────┘ │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    无侵入设计：业务代码无需任何修改                                              │
│    @AuditLog(module = "用户管理", action = "更新用户")                          │
│    public void updateUser(User user) { ... }                                   │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 无侵入式地拦截带有 @AuditLog 注解的方法
- 在业务执行前后自动完成审计数据的采集
- 支持嵌套调用共享上下文

---

## 2. 观察者模式（Observer Pattern）

### 什么是观察者模式？

观察者模式是一种行为型设计模式，**定义对象间的一对多依赖关系，当一个对象（被观察者）状态改变时，所有依赖于它的对象（观察者）都会收到通知并自动更新**。它实现了发布-订阅机制，使对象之间松耦合。

### 核心特征

1. **一对多关系**：一个被观察者可以有多个观察者
2. **自动通知**：被观察者状态变化时自动通知所有观察者
3. **松耦合**：被观察者不需要知道具体观察者的实现
4. **动态订阅**：观察者可以动态注册和注销

### 为什么这是观察者模式而不是发布-订阅模式？

| 对比维度 | 观察者模式 | 发布-订阅模式 |
|---------|-----------|--------------|
| **通信方式** | 被观察者直接通知观察者 | 通过消息队列/事件总线中转 |
| **耦合程度** | 观察者和被观察者有直接关联 | 发布者和订阅者完全解耦 |
| **消息传递** | 同步调用 | 通常异步传递 |
| **典型实现** | 事件监听器、GUI事件 | 消息队列、事件总线 |

**判断依据**：`GlobalAuditListener` 直接实现了 MyBatis-Flex 的 `InsertListener` 和 `UpdateListener` 接口，MyBatis-Flex（被观察者）在执行数据库操作时**直接调用**监听器的方法。观察者和被观察者之间存在直接的接口依赖关系，而非通过中间消息队列中转，这符合观察者模式的定义。

### 实现位置
`cn.dreamtof.audit.core.GlobalAuditListener`

### 示例代码

```java
@Component
public class GlobalAuditListener implements InsertListener, UpdateListener {
    @Override
    public void onInsert(Object entity) {
        AuditTransactionContext.ContextPayload payload = AuditTransactionContext.get();
        if (payload == null || !payload.getAllowedEntities().contains(entity.getClass())) {
            return;
        }
        
        AuditTransactionContext.addRecord(new AuditTransactionContext.Record(
            OperationType.INSERT,
            entity.getClass(),
            IdExtractor.extractId(entity),
            null,
            entity
        ));
    }

    @Override
    public void onUpdate(Object entity) {
        AuditTransactionContext.ContextPayload payload = AuditTransactionContext.get();
        if (payload == null) return;
        
        Object oldState = payload.getPrefetchedOldStates().get(key);
        AuditTransactionContext.addRecord(new Record(
            OperationType.UPDATE,
            entity.getClass(),
            IdExtractor.extractId(entity),
            oldState,
            entity
        ));
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              观察者模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    MyBatis-Flex 数据操作                                                        │
│    mapper.insert(user) / mapper.update(user)                                   │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      MyBatis-Flex 核心                               │     │
│    │                                                                     │     │
│    │   执行 SQL 操作                                                      │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              触发监听器事件                                 │    │     │
│    │   │                                                           │    │     │
│    │   │   INSERT 操作 ──► 触发 InsertListener.onInsert()          │    │     │
│    │   │   UPDATE 操作 ──► 触发 UpdateListener.onUpdate()          │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └───────────────────────────────┬─────────────────────────────────────┘     │
│                                    │                                          │
│                                    ▼                                          │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  GlobalAuditListener (观察者)                        │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              onInsert(entity)                              │    │     │
│    │   │                                                           │    │     │
│    │   │   1. 检查是否在审计上下文中                                │    │     │
│    │   │      AuditTransactionContext.get() != null?               │    │     │
│    │   │                                                           │    │     │
│    │   │   2. 检查实体是否在允许列表中                              │    │     │
│    │   │      allowedEntities.contains(entity.getClass())?         │    │     │
│    │   │                                                           │    │     │
│    │   │   3. 创建审计记录                                         │    │     │
│    │   │      Record(INSERT, User.class, id: 1001, null, entity)   │    │     │
│    │   │                                                           │    │     │
│    │   │   4. 添加到上下文                                         │    │     │
│    │   │      AuditTransactionContext.addRecord(record)            │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              onUpdate(entity)                              │    │     │
│    │   │                                                           │    │     │
│    │   │   1. 检查审计上下文                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   2. 获取预抓取的旧状态                                   │    │     │
│    │   │      oldState = prefetchedOldStates.get(key)              │    │     │
│    │   │                                                           │    │     │
│    │   │   3. 创建审计记录 (包含新旧状态)                          │    │     │
│    │   │      Record(UPDATE, User.class, id: 1001, oldState, entity)│   │     │
│    │   │                                                           │    │     │
│    │   │   4. 添加到上下文                                         │    │     │
│    │   │      AuditTransactionContext.addRecord(record)            │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    观察者模式优势：                                                              │
│    1. 业务代码无感知 - 不需要显式调用审计逻辑                                    │
│    2. 自动捕获所有数据库变更                                                    │
│    3. 与 MyBatis-Flex 深度集成                                                 │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 监听 MyBatis-Flex 的数据库操作事件
- 自动捕获 INSERT/UPDATE 操作，无需业务代码显式调用
- 作为观察者订阅数据库的变更事件

---

## 3. 建造者模式（Builder Pattern）

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

**判断依据**：`SmartAuditUpdater` 需要配置多个参数（partial、skipNullFields、includeSystemFields、operationType），通过链式调用逐步设置，最后调用 `diff()` 方法才真正执行构建逻辑。这种**分步配置、延迟构建**的方式是建造者模式的典型特征，而非工厂模式的一步创建。

### 实现位置
`cn.dreamtof.audit.core.SmartAuditUpdater`

### 示例代码

```java
public class SmartAuditUpdater<S, T> {
    private final S source;
    private final T target;
    private boolean isPartial = false;
    private boolean skipNullFields = false;
    private boolean includeSystemFields = false;
    private OperationType operationType;

    private SmartAuditUpdater(S source, T target) {
        this.source = source;
        this.target = target;
    }

    public static <S, T> SmartAuditUpdater<S, T> copy(S source, T target) {
        return new SmartAuditUpdater<>(source, target);
    }

    public SmartAuditUpdater<S, T> partial(boolean partial) {
        this.isPartial = partial;
        return this;
    }

    public SmartAuditUpdater<S, T> skipNullFields(boolean skip) {
        this.skipNullFields = skip;
        return this;
    }

    public SmartAuditUpdater<S, T> includeSystemFields(boolean include) {
        this.includeSystemFields = include;
        return this;
    }

    public SmartAuditUpdater<S, T> operationType(OperationType type) {
        this.operationType = type;
        return this;
    }

    public List<AuditResult> diff() {
        // 执行差异比较
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              建造者模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    审计差异比较需求                                                              │
│    比较新旧对象差异，生成审计日志                                                │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  SmartAuditUpdater 建造过程                          │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              步骤1: 创建建造者                              │    │     │
│    │   │                                                           │    │     │
│    │   │   SmartAuditUpdater.copy(oldState, newState)              │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► 私有构造器创建实例                              │    │     │
│    │   │               new SmartAuditUpdater<>(source, target)     │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              步骤2: 链式配置参数                            │    │     │
│    │   │                                                           │    │     │
│    │   │   .partial(true)          // 只比较部分字段                │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► this.isPartial = true; return this;            │    │     │
│    │   │                                                           │    │     │
│    │   │   .skipNullFields(true)   // 跳过空值字段                  │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► this.skipNullFields = true; return this;       │    │     │
│    │   │                                                           │    │     │
│    │   │   .operationType(UPDATE)  // 操作类型                      │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► this.operationType = UPDATE; return this;      │    │     │
│    │   │                                                           │    │     │
│    │   │   .includeSystemFields(false) // 排除系统字段              │    │     │
│    │   │       │                                                   │    │     │
│    │   │       └──► this.includeSystemFields = false; return this; │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              步骤3: 执行构建                               │    │     │
│    │   │                                                           │    │     │
│    │   │   .diff()                                                 │    │     │
│    │   │       │                                                   │    │     │
│    │   │       ▼                                                   │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ 差异比较逻辑:                                     │    │    │     │
│    │   │   │                                                   │    │    │     │
│    │   │   │ for (AuditField field : fields) {               │    │    │     │
│    │   │   │   Object oldVal = field.get(source);            │    │    │     │
│    │   │   │   Object newVal = field.get(target);            │    │    │     │
│    │   │   │                                                   │    │    │     │
│    │   │   │   if (isPartial && !partialFields.contains(field))│   │    │     │
│    │   │   │       continue;                                  │    │    │     │
│    │   │   │                                                   │    │    │     │
│    │   │   │   if (skipNullFields && newVal == null)         │    │    │     │
│    │   │   │       continue;                                  │    │    │     │
│    │   │   │                                                   │    │    │     │
│    │   │   │   if (!Objects.equals(oldVal, newVal)) {        │    │    │     │
│    │   │   │       results.add(new AuditResult(...));        │    │    │     │
│    │   │   │   }                                              │    │    │     │
│    │   │   │ }                                                │    │    │     │
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
│    │   List<AuditResult> results = SmartAuditUpdater                     │     │
│    │       .copy(oldUser, newUser)                                       │     │
│    │       .partial(true)                                                │     │
│    │       .skipNullFields(true)                                         │     │
│    │       .operationType(OperationType.UPDATE)                          │     │
│    │       .includeSystemFields(false)                                   │     │
│    │       .diff();                                                      │     │
│    │                                                                     │     │
│    │   // 结果: [                                                         │     │
│    │   //   AuditResult("用户名", "张三", "李四"),                        │     │
│    │   //   AuditResult("状态", "ACTIVE", "INACTIVE")                    │     │
│    │   // ]                                                              │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 支持链式调用配置审计参数
- 使复杂的审计配置更加直观和灵活

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
| **典型场景** | 排序算法、支付方式、数据库方言 | 订单状态、工作流状态 |

**判断依据**：`DialectSupport` 根据 `DbType` 选择不同的 SQL 生成策略，这些策略（MySQL、PostgreSQL、Oracle）之间**相互独立**，没有状态转换关系。客户端（审计模块）根据当前数据库类型**主动选择**对应的策略，这符合策略模式的定义。

### 实现位置
`cn.dreamtof.audit.utils.DialectSupport`

### 示例代码

```java
public class DialectSupport {
    public static String buildJsonSnapshotSql(TableInfo info) {
        DbType dbType = getDbType();
        List<String> columns = info.getColumns();
        String tableName = info.getTableName();
        String alias = "t";

        return switch (dbType) {
            case MYSQL, H2 -> {
                String jsonPairs = columns.stream()
                    .map(col -> "\"" + col + "\", " + alias + "." + col)
                    .collect(Collectors.joining(", "));
                yield "SELECT JSON_OBJECT(" + jsonPairs + ") as snapshot FROM " + tableName + " " + alias;
            }
            case POSTGRE_SQL -> {
                String jsonPairs = columns.stream()
                    .map(col -> "'" + col + "', " + alias + "." + col)
                    .collect(Collectors.joining(", "));
                yield "SELECT json_build_object(" + jsonPairs + ") as snapshot FROM " + tableName + " " + alias;
            }
            case ORACLE -> {
                String jsonPairs = columns.stream()
                    .map(col -> "'" + col + "' VALUE " + alias + "." + col)
                    .collect(Collectors.joining(", "));
                yield "SELECT JSON_OBJECT(" + jsonPairs + ") as snapshot FROM " + tableName + " " + alias;
            }
            default -> buildFallbackSql(info);
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
│    审计快照查询需求                                                              │
│    buildJsonSnapshotSql(TableInfo)                                             │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      DialectSupport                                  │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              获取数据库类型                                 │    │     │
│    │   │                                                           │    │     │
│    │   │   DbType dbType = getDbType();                            │    │     │
│    │   │   // 从 DataSource 或配置中获取                            │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              策略选择 (switch)                              │    │     │
│    │   │                                                           │    │     │
│    │   │   switch (dbType) {                                       │    │     │
│    │   │       case MYSQL, H2 ->      MySQL 策略                   │    │     │
│    │   │       case POSTGRE_SQL ->    PostgreSQL 策略              │    │     │
│    │   │       case ORACLE ->         Oracle 策略                  │    │     │
│    │   │       default ->             兜底策略                     │    │     │
│    │   │   }                                                      │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ├─────────────────┬─────────────────┬─────────────────┐  │     │
│    │           ▼                 ▼                 ▼                 ▼  │     │
│    │   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│    │   │MySQL 策略   │   │PostgreSQL策略│   │ Oracle 策略 │   │ 兜底策略    │
│    │   │             │   │             │   │             │   │             │
│    │   │JSON_OBJECT()│   │json_build_  │   │JSON_OBJECT()│   │SELECT所有列 │
│    │   │             │   │object()     │   │             │   │             │
│    │   └─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘
│    │           │                 │                 │                 │     │
│    │           ▼                 ▼                 ▼                 ▼     │
│    │   ┌─────────────────────────────────────────────────────────────┐   │     │
│    │   │                      生成的 SQL                              │   │     │
│    │   │                                                             │   │     │
│    │   │   MySQL:                                                    │   │     │
│    │   │   SELECT JSON_OBJECT("id", t.id, "name", t.name, ...)       │   │     │
│    │   │   FROM user t WHERE id = ?                                  │   │     │
│    │   │                                                             │   │     │
│    │   │   PostgreSQL:                                               │   │     │
│    │   │   SELECT json_build_object('id', t.id, 'name', t.name, ...) │   │     │
│    │   │   FROM user t WHERE id = ?                                  │   │     │
│    │   │                                                             │   │     │
│    │   │   Oracle:                                                   │   │     │
│    │   │   SELECT JSON_OBJECT('id' VALUE t.id, 'name' VALUE t.name)  │   │     │
│    │   │   FROM user t WHERE id = ?                                  │   │     │
│    │   │                                                             │   │     │
│    │   └─────────────────────────────────────────────────────────────┘   │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    扩展性：新增数据库支持只需添加新的 case 分支                                  │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 支持多种数据库的 JSON 函数差异
- 新增数据库支持只需添加新的 case 分支

---

## 5. 装饰器模式（Decorator Pattern）

### 什么是装饰器模式？

装饰器模式是一种结构型设计模式，**动态地给对象添加额外的职责**。它通过创建包装对象（装饰器）来包裹真实对象，在不改变原有对象结构的情况下扩展其功能，比继承更加灵活。

### 核心特征

1. **动态扩展**：运行时动态添加功能，而非编译时
2. **继承替代**：使用组合而非继承来扩展功能
3. **多层装饰**：可以叠加多个装饰器
4. **接口一致**：装饰器与被装饰对象实现相同接口

### 为什么这是装饰器模式而不是代理模式？

| 对比维度 | 装饰器模式 | 代理模式 |
|---------|-----------|---------|
| **主要目的** | 动态添加职责 | 控制对象访问 |
| **功能侧重** | 增强功能（做更多事） | 控制访问（管谁能做事） |
| **对象创建** | 装饰器通常由客户端创建 | 代理可能隐藏真实对象的创建 |
| **典型场景** | I/O流、UI组件增强 | 远程代理、保护代理 |

**判断依据**：`SmartTransactionTemplate` 继承 `TransactionTemplate` 并**增强**其 `execute()` 方法，添加了审计保存点机制。它不是为了控制访问，而是为了**在原有事务功能基础上叠加审计相关的能力**。这种"增强功能"的意图符合装饰器模式的定义。

### 实现位置
`cn.dreamtof.audit.utils.SmartTransactionTemplate`

### 示例代码

```java
@Component
public class SmartTransactionTemplate extends TransactionTemplate {

    @Override
    public <T> T execute(@NonNull TransactionCallback<T> action) {
        AuditTransactionContext.ContextPayload payload = AuditTransactionContext.get();
        int savepoint = (payload != null) ? payload.createSavepoint() : 0;

        try {
            return super.execute(status -> {
                T result = action.doInTransaction(status);
                if (status != null && status.isRollbackOnly() && payload != null) {
                    payload.rollbackToSavepoint(savepoint);
                }
                return result;
            });
        } catch (Throwable e) {
            if (payload != null) {
                payload.rollbackToSavepoint(savepoint);
            }
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        }
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              装饰器模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    业务代码调用                                                                  │
│    smartTransactionTemplate.execute(status -> {...})                           │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                SmartTransactionTemplate (装饰器)                     │     │
│    │                                                                     │     │
│    │   继承 TransactionTemplate，增强事务处理能力                         │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              execute() 增强流程                             │    │     │
│    │   │                                                           │    │     │
│    │   │   步骤1: 创建审计保存点                                    │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ AuditTransactionContext.ContextPayload payload  │    │    │     │
│    │   │   │     = AuditTransactionContext.get();            │    │    │     │
│    │   │   │ int savepoint = payload.createSavepoint();      │    │    │     │
│    │   │   │     // savepoint = 当前审计记录数量              │    │    │     │
│    │   │   │     // 例如: savepoint = 3                       │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                           │                               │    │     │
│    │   │                           ▼                               │    │     │
│    │   │   步骤2: 调用父类事务模板                                  │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ return super.execute(status -> {                │    │    │     │
│    │   │   │     T result = action.doInTransaction(status);  │    │    │     │
│    │   │   │     // 执行业务逻辑                              │    │    │     │
│    │   │   │     // 可能产生多条审计记录                      │    │    │     │
│    │   │   │     return result;                              │    │    │     │
│    │   │   │ });                                             │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                           │                               │    │     │
│    │   │           ┌───────────────┴───────────────┐              │    │     │
│    │   │           ▼                               ▼              │    │     │
│    │   │   ┌─────────────────┐           ┌─────────────────┐     │    │     │
│    │   │   │   正常提交       │           │   异常回滚       │     │    │     │
│    │   │   │                 │           │                 │     │    │     │
│    │   │   │ 审计记录保留     │           │ 回滚到保存点     │     │    │     │
│    │   │   │ records: [1,2,3,4,5]│       │ payload.rollbackToSavepoint(3)│
│    │   │   │                 │           │ records: [1,2,3] │     │    │     │
│    │   │   └─────────────────┘           └─────────────────┘     │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    对比：原始 TransactionTemplate vs SmartTransactionTemplate                   │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                                                                     │     │
│    │   TransactionTemplate              SmartTransactionTemplate          │     │
│    │   ┌──────────────────┐            ┌──────────────────┐              │     │
│    │   │ 开启事务          │            │ 开启事务          │              │     │
│    │   │ 执行业务          │            │ 创建保存点        │              │     │
│    │   │ 提交/回滚         │            │ 执行业务          │              │     │
│    │   │                  │            │ 回滚时清理审计    │              │     │
│    │   │ ❌ 审计记录可能脏 │            │ 提交/回滚         │              │     │
│    │   │                  │            │                  │              │     │
│    │   │                  │            │ ✅ 审计记录一致   │              │     │
│    │   └──────────────────┘            └──────────────────┘              │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 在原生 TransactionTemplate 基础上增加审计保存点机制
- 事务回滚时自动清理对应的审计记录，防止"脏审计数据"

---

## 6. 上下文模式（Context Pattern）

### 什么是上下文模式？

上下文模式是一种用于**在特定作用域内共享状态和数据**的设计模式。它提供了一种机制，使得在调用链中的多个组件可以访问共享的上下文信息，而无需显式传递参数。在 Java 中通常通过 ThreadLocal 或 ScopedValue 实现。

### 核心特征

1. **作用域绑定**：上下文数据绑定到特定执行范围
2. **隐式传递**：无需通过方法参数显式传递
3. **线程隔离**：不同线程的上下文相互独立
4. **自动清理**：作用域结束后自动清理上下文

### 为什么这是上下文模式而不是单例模式？

| 对比维度 | 上下文模式 | 单例模式 |
|---------|-----------|---------|
| **数据范围** | 每个线程/作用域独立 | 全局共享唯一实例 |
| **生命周期** | 随作用域创建和销毁 | 应用生命周期 |
| **并发访问** | 天然线程安全 | 需要同步控制 |
| **典型场景** | 事务上下文、请求上下文 | 配置管理、连接池 |

**判断依据**：`AuditTransactionContext` 使用 `ScopedValue` 实现上下文绑定，每个审计操作都有独立的 `ContextPayload`，不同线程/调用的上下文数据**相互隔离**。它不是全局共享的单例，而是**随作用域创建和销毁**的上下文容器，这符合上下文模式的定义。

### 实现位置
`cn.dreamtof.audit.core.AuditTransactionContext`

### 示例代码

```java
public class AuditTransactionContext {
    public static final ScopedValue<ContextPayload> CONTEXT = ScopedValue.newInstance();

    @Data
    public static class ContextPayload {
        private final String module;
        private final String action;
        private final Set<Class<?>> allowedEntities;
        private final List<Record> records = new ArrayList<>();
        private final Map<String, Object> prefetchedOldStates = new ConcurrentHashMap<>();

        public int createSavepoint() {
            return records.size();
        }

        public void rollbackToSavepoint(int savepoint) {
            while (records.size() > savepoint) {
                records.remove(records.size() - 1);
            }
        }
    }

    public static ContextPayload get() {
        return CONTEXT.get();
    }

    public static void addRecord(Record record) {
        CONTEXT.get().getRecords().add(record);
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              上下文模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    AOP 拦截入口                                                                  │
│    AutoAuditAspect.doAudit()                                                   │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │              AuditTransactionContext 上下文管理                      │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              ScopedValue 绑定                              │    │     │
│    │   │                                                           │    │     │
│    │   │   ScopedValue.where(CONTEXT, payload).call(() -> {        │    │     │
│    │   │       // 在此范围内，CONTEXT 绑定到 payload               │    │     │
│    │   │       // 所有子调用都可以访问同一个 payload               │    │     │
│    │   │       return joinPoint.proceed();                         │    │     │
│    │   │   });                                                     │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              ContextPayload 结构                           │    │     │
│    │   │                                                           │    │     │
│    │   │   ┌─────────────────────────────────────────────────┐    │    │     │
│    │   │   │ ContextPayload {                                 │    │    │     │
│    │   │   │   module: "用户管理"                             │    │    │     │
│    │   │   │   action: "更新用户"                             │    │    │     │
│    │   │   │   allowedEntities: [User.class, Order.class]    │    │    │     │
│    │   │   │                                                     │    │    │     │
│    │   │   │   records: [                    // 审计记录列表   │    │    │     │
│    │   │   │     Record(UPDATE, User, 1001, oldState, newState),│   │    │     │
│    │   │   │     Record(INSERT, Order, 2001, null, order),   │    │    │     │
│    │   │   │   ]                                                │    │    │     │
│    │   │   │                                                     │    │    │     │
│    │   │   │   prefetchedOldStates: {         // 预抓取旧状态  │    │    │     │
│    │   │   │     "User:1001": {id:1001, name:"张三", ...},    │    │    │     │
│    │   │   │     "Order:2001": {...},                         │    │    │     │
│    │   │   │   }                                                │    │    │     │
│    │   │   │ }                                                  │    │    │     │
│    │   │   └─────────────────────────────────────────────────┘    │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              保存点机制                                    │    │     │
│    │   │                                                           │    │     │
│    │   │   createSavepoint()                                       │    │     │
│    │   │       └──► return records.size();  // 返回当前记录数      │    │     │
│    │   │                                                           │    │     │
│    │   │   rollbackToSavepoint(savepoint)                          │    │     │
│    │   │       └──► while (records.size() > savepoint) {           │    │     │
│    │   │               records.remove(records.size() - 1);         │    │     │
│    │   │           }                                               │    │     │
│    │   │                                                           │    │     │
│    │   │   示例:                                                    │    │     │
│    │   │   savepoint = 3  // 创建保存点时 records.size() = 3       │    │     │
│    │   │   ... 执行业务，新增 2 条记录 ...                          │    │     │
│    │   │   records.size() = 5                                       │    │     │
│    │   │   rollbackToSavepoint(3)  // 回滚到保存点                  │    │     │
│    │   │   records.size() = 3  // 清理了后 2 条记录                 │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    ScopedValue vs ThreadLocal:                                                  │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                                                                     │     │
│    │   ThreadLocal                      ScopedValue (Java 21+)           │     │
│    │   ┌──────────────────┐            ┌──────────────────┐              │     │
│    │   │ 可变             │            │ 不可变            │              │     │
│    │   │ 需要手动清理      │            │ 自动清理          │              │     │
│    │   │ 虚拟线程不友好    │            │ 虚拟线程友好      │              │     │
│    │   │ 可能内存泄漏      │            │ 无内存泄漏        │              │     │
│    │   └──────────────────┘            └──────────────────┘              │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 在事务生命周期内传递审计上下文
- 支持嵌套事务的保存点和回滚机制
- 使用 ScopedValue 替代 ThreadLocal，适配虚拟线程

---

## 7. 适配器模式（Adapter Pattern）

### 什么是适配器模式？

适配器模式是一种结构型设计模式，**将一个类的接口转换成客户端期望的另一个接口**。它使原本因接口不兼容而不能一起工作的类可以协同工作，解决了接口不匹配的问题。

### 核心特征

1. **接口转换**：将现有接口转换为目标接口
2. **复用现有类**：无需修改原有代码即可复用
3. **解耦客户端**：客户端通过统一接口与不同实现交互
4. **双向适配**：可以实现双向适配（可选）

### 为什么这是适配器模式而不是装饰器模式？

| 对比维度 | 适配器模式 | 装饰器模式 |
|---------|-----------|-----------|
| **主要目的** | 接口转换 | 功能增强 |
| **接口关系** | 改变接口以匹配目标 | 保持接口不变 |
| **使用场景** | 接口不兼容时 | 需要扩展功能时 |
| **典型例子** | 电源适配器、日志框架适配 | I/O流包装、UI组件增强 |

**判断依据**：`AuditBeanMeta` 将通用的 `FastBeanMeta` 接口**转换**为审计模块所需的 `AuditBeanMeta` 接口，添加了审计特有的显示名称映射。它不是为了增强功能，而是为了**让通用元数据适配审计场景的需求**，这符合适配器模式的定义。

### 实现位置
`cn.dreamtof.audit.cache.AuditBeanMeta`

### 示例代码

```java
public class AuditBeanMeta {
    private final Class<?> targetClass;
    private final String classLabel;
    private final List<AuditField> fields;

    private static final ClassValue<AuditBeanMeta> CACHE = new ClassValue<>() {
        @Override
        protected AuditBeanMeta computeValue(Class<?> type) {
            return new AuditBeanMeta(type);
        }
    };

    public static AuditBeanMeta of(Class<?> clazz) {
        return CACHE.get(clazz);
    }

    private AuditBeanMeta(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.classLabel = AuditMetaCache.getDisplayName(targetClass);

        FastBeanMeta fastMeta = FastBeanMeta.of(targetClass);
        this.fields = fastMeta.getWriteableAccessors().stream()
            .map(acc -> new AuditField(
                AuditMetaCache.getDisplayName(acc.field()),
                acc
            ))
            .collect(Collectors.toList());
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              适配器模式流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    审计模块需要元数据                                                            │
│    AuditBeanMeta.of(User.class)                                                │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                      AuditBeanMeta (适配器)                          │     │
│    │                                                                     │     │
│    │   适配目标：将通用的 FastBeanMeta 转换为审计专用的元数据              │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              适配过程                                      │    │     │
│    │   │                                                           │    │     │
│    │   │   步骤1: 获取通用元数据                                    │    │     │
│    │   │   FastBeanMeta fastMeta = FastBeanMeta.of(User.class);    │    │     │
│    │   │                                                           │    │     │
│    │   │   步骤2: 转换字段信息                                      │    │     │
│    │   │   this.fields = fastMeta.getWriteableAccessors()          │    │     │
│    │   │       .stream()                                           │    │     │
│    │   │       .map(acc -> new AuditField(                         │    │     │
│    │   │           AuditMetaCache.getDisplayName(acc.field()),     │    │     │
│    │   │           acc                                             │    │     │
│    │   │       ))                                                  │    │     │
│    │   │       .collect(Collectors.toList());                      │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              数据结构对比                                  │    │     │
│    │   │                                                           │    │     │
│    │   │   FastBeanMeta (通用)           AuditBeanMeta (审计专用)  │    │     │
│    │   │   ┌──────────────────┐         ┌──────────────────┐      │    │     │
│    │   │   │ User.class       │         │ User.class       │      │    │     │
│    │   │   │                  │  适配   │ classLabel: "用户"│      │    │     │
│    │   │   │ accessors: [     │ ────►  │                  │      │    │     │
│    │   │   │   {field, getter,│         │ fields: [        │      │    │     │
│    │   │   │    setter}       │         │   AuditField(    │      │    │     │
│    │   │   │ ]                │         │     "用户名",     │      │    │     │
│    │   │   └──────────────────┘         │     accessor     │      │    │     │
│    │   │                                │   ),             │      │    │     │
│    │   │                                │   AuditField(    │      │    │     │
│    │   │                                │     "创建时间",   │      │    │     │
│    │   │                                │     accessor     │      │    │     │
│    │   │                                │   )              │      │    │     │
│    │   │                                │ ]                │      │    │     │
│    │   │                                └──────────────────┘      │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
│    适配器优势：                                                                  │
│    1. 解耦"如何运行"(FastBeanMeta) 和"怎么展示"(AuditMetaCache)                │
│    2. 复用 core 模块的反射能力                                                  │
│    3. 添加审计特有的显示名称映射                                                 │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 将通用的 FastBeanMeta 适配为审计专用的元数据结构
- 解耦"如何运行"（FastBeanMeta）和"怎么展示"（AuditMetaCache）

---

## 8. 单例模式（Singleton Pattern）

### 什么是单例模式？

单例模式是一种创建型设计模式，**确保一个类只有一个实例，并提供一个全局访问点**。它限制了类的实例化，确保在整个应用程序中只存在一个实例，常用于管理共享资源或全局状态。

### 核心特征

1. **唯一实例**：整个应用中只存在一个实例
2. **全局访问**：提供全局访问点获取实例
3. **私有构造**：防止外部通过 new 创建实例
4. **延迟初始化**：可以延迟到首次使用时创建（可选）

### 为什么这是单例模式而不是静态工具类？

| 对比维度 | 单例模式 | 静态工具类 |
|---------|---------|-----------|
| **实例化** | 有实例对象 | 无实例，全是静态方法 |
| **状态管理** | 可以维护实例状态 | 通常无状态 |
| **继承多态** | 可以继承、实现接口 | 不能继承、无多态 |
| **生命周期** | 可以延迟加载、可销毁 | 类加载时初始化 |

**判断依据**：`AuditMetaCache` 使用 `static final` 缓存实例，所有方法都是 `static`，但内部维护了 `Caffeine Cache` 的**状态**（缓存的显示名称映射）。它结合了单例和缓存的特性，提供了**全局唯一**的缓存访问点，这符合单例模式的定义。

### 实现位置
`cn.dreamtof.audit.cache.AuditMetaCache`

### 示例代码

```java
public class AuditMetaCache {
    private static final Cache<AnnotatedElement, String> DISPLAY_NAME_CACHE = Caffeine.newBuilder()
        .maximumSize(5000)
        .weakKeys()
        .expireAfterAccess(24, TimeUnit.HOURS)
        .build();

    public static String getDisplayName(AnnotatedElement element) {
        return DISPLAY_NAME_CACHE.get(element, AuditMetaCache::computeDisplayName);
    }

    private static String computeDisplayName(AnnotatedElement element) {
        // 计算显示名称
    }
}
```

### 流程图

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              单例模式流程                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│    多处调用                                                                      │
│    AuditMetaCache.getDisplayName(User.class)                                   │
│    AuditMetaCache.getDisplayName(User.class.getDeclaredField("name"))          │
│           │                                                                     │
│           ▼                                                                     │
│    ┌─────────────────────────────────────────────────────────────────────┐     │
│    │                  AuditMetaCache (单例 + 缓存)                        │     │
│    │                                                                     │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              static final 单例缓存                         │    │     │
│    │   │                                                           │    │     │
│    │   │   private static final Cache<AnnotatedElement, String>    │    │     │
│    │   │       DISPLAY_NAME_CACHE = Caffeine.newBuilder()          │    │     │
│    │   │           .maximumSize(5000)                              │    │     │
│    │   │           .weakKeys()                                     │    │     │
│    │   │           .expireAfterAccess(24, TimeUnit.HOURS)          │    │     │
│    │   │           .build();                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   特性:                                                    │    │     │
│    │   │   - static final: 全局唯一实例                             │    │     │
│    │   │   - Caffeine: 高性能缓存库                                 │    │     │
│    │   │   - maximumSize: 最大 5000 条记录                          │    │     │
│    │   │   - weakKeys: 弱引用键，GC 可回收                          │    │     │
│    │   │   - expireAfterAccess: 24 小时未访问则过期                 │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              getDisplayName() 流程                         │    │     │
│    │   │                                                           │    │     │
│    │   │   public static String getDisplayName(element) {          │    │     │
│    │   │       return DISPLAY_NAME_CACHE.get(element, key -> {     │    │     │
│    │   │           return computeDisplayName(key);  // 缓存未命中时 │    │     │
│    │   │       });                                                 │    │     │
│    │   │   }                                                       │    │     │
│    │   │                                                           │    │     │
│    │   │   缓存命中 ──► 直接返回缓存值                              │    │     │
│    │   │   缓存未命中 ──► 调用 computeDisplayName() 计算并缓存     │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │           │                                                         │     │
│    │           ▼                                                         │     │
│    │   ┌───────────────────────────────────────────────────────────┐    │     │
│    │   │              computeDisplayName() 计算逻辑                 │    │     │
│    │   │                                                           │    │     │
│    │   │   1. 检查 @AuditEntityId 注解                             │    │     │
│    │   │      @AuditEntityId("用户")                               │    │     │
│    │   │      public class User { ... }                            │    │     │
│    │   │      └──► 返回 "用户"                                     │    │     │
│    │   │                                                           │    │     │
│    │   │   2. 检查 @AuditReference 注解                            │    │     │
│    │   │      @AuditReference("用户名")                            │    │     │
│    │   │      private String name;                                 │    │     │
│    │   │      └──► 返回 "用户名"                                   │    │     │
│    │   │                                                           │    │     │
│    │   │   3. 默认: 字段名驼峰转中文                                │    │     │
│    │   │      createTime ──► "创建时间"                            │    │     │
│    │   │      updateTime ──► "更新时间"                            │    │     │
│    │   │                                                           │    │     │
│    │   └───────────────────────────────────────────────────────────┘    │     │
│    │                                                                     │     │
│    └─────────────────────────────────────────────────────────────────────┘     │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 解决的问题
- 使用 static final 确保全局唯一实例
- 结合 Caffeine 缓存库实现带过期策略的单例缓存
- 所有方法均为 static，通过类名直接访问

---

## 设计模式总结

| 设计模式 | 核心应用场景 | 关键类 | 判断依据 |
|---------|-------------|--------|---------|
| AOP 代理模式 | 无侵入审计拦截 | AutoAuditAspect | 控制对业务方法的访问，而非简单叠加功能 |
| 观察者模式 | 数据库事件监听 | GlobalAuditListener | 直接接口依赖，非消息队列中转 |
| 建造者模式 | 审计配置链式构建 | SmartAuditUpdater | 分步配置、延迟构建，非一步创建 |
| 策略模式 | 数据库方言支持 | DialectSupport | 算法独立可互换，非状态驱动 |
| 装饰器模式 | 事务模板增强 | SmartTransactionTemplate | 增强功能叠加，非控制访问 |
| 上下文模式 | 事务生命周期上下文传递 | AuditTransactionContext | 作用域隔离，非全局共享 |
| 适配器模式 | Bean元数据适配 | AuditBeanMeta | 接口转换适配，非功能增强 |
| 单例模式 | 审计元数据缓存 | AuditMetaCache | 全局唯一实例，维护缓存状态 |
