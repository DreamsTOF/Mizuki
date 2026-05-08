# dreamtof-audit 模块设计模式分析

## 模块概述

dreamtof-audit 提供审计日志自动采集能力，通过 AOP 和数据库事件监听实现无侵入式审计。

---

## 1. AOP 代理模式（Proxy Pattern）

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

### 解决的问题
- 无侵入式地拦截带有 @AuditLog 注解的方法
- 在业务执行前后自动完成审计数据的采集
- 支持嵌套调用共享上下文

---

## 2. 观察者模式（Observer Pattern）

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

### 解决的问题
- 监听 MyBatis-Flex 的数据库操作事件
- 自动捕获 INSERT/UPDATE 操作，无需业务代码显式调用
- 作为观察者订阅数据库的变更事件

---

## 3. 建造者模式（Builder Pattern）

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

### 典型调用示例

```java
List<AuditResult> results = SmartAuditUpdater.copy(record.getOldState(), record.getNewState())
    .partial(payload.isPartial())
    .skipNullFields(payload.isSkipNull())
    .operationType(record.getType())
    .includeSystemFields(payload.isSystemFields())
    .diff();
```

### 解决的问题
- 支持链式调用配置审计参数
- 使复杂的审计配置更加直观和灵活

---

## 4. 策略模式（Strategy Pattern）

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

### 解决的问题
- 支持多种数据库的 JSON 函数差异
- 新增数据库支持只需添加新的 case 分支

---

## 5. 装饰器模式（Decorator Pattern）

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

### 解决的问题
- 在原生 TransactionTemplate 基础上增加审计保存点机制
- 事务回滚时自动清理对应的审计记录，防止"脏审计数据"

---

## 6. 上下文模式（Context Pattern）

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

### 解决的问题
- 在事务生命周期内传递审计上下文
- 支持嵌套事务的保存点和回滚机制
- 使用 ScopedValue 替代 ThreadLocal，适配虚拟线程

---

## 7. 适配器模式（Adapter Pattern）

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

### 解决的问题
- 将通用的 FastBeanMeta 适配为审计专用的元数据结构
- 解耦"如何运行"（FastBeanMeta）和"怎么展示"（AuditMetaCache）

---

## 8. 单例模式（Singleton Pattern）

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

### 解决的问题
- 使用 static final 确保全局唯一实例
- 结合 Caffeine 缓存库实现带过期策略的单例缓存
- 所有方法均为 static，通过类名直接访问

---

## 设计模式总结

| 设计模式 | 核心应用场景 |
|---------|-------------|
| AOP 代理模式 | 无侵入审计拦截 |
| 观察者模式 | 数据库事件监听 |
| 建造者模式 | 审计配置链式构建 |
| 策略模式 | 数据库方言支持 |
| 装饰器模式 | 事务模板增强 |
| 上下文模式 | 事务生命周期上下文传递 |
| 适配器模式 | Bean元数据适配 |
| 单例模式 | 审计元数据缓存 |
