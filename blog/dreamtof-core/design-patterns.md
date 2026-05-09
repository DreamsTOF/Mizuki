# dreamtof-core 模块设计模式分析

## 模块概述

dreamtof-core 是基础核心层，提供上下文管理、异常处理、校验、反射工具等基础设施能力。

---

## 1. Spring Bean 自动注入 + 注册表模式（双模式架构）

### 实现位置
`cn.dreamtof.core.context.ThreadContextCopier` + `cn.dreamtof.core.context.ContextRegistry`

### 模式一：Spring Bean 自动注入（默认，零配置）

```java
// 只需两步：1. 实现 ThreadContextCopier  2. 加 @Component
@Component
public class DataSourceCopier implements ThreadContextCopier {
    @Override
    public Object capture() { return DataSourceKey.get(); }
    
    @Override
    public void restore(Object snapshot) { 
        if (snapshot != null) DataSourceKey.use((String) snapshot); 
    }
    
    @Override
    public void clear() { DataSourceKey.clear(); }
}

// 无需任何配置文件，Spring 自动注入所有 ThreadContextCopier Bean
```

### 模式二：注册表模式（显式命名注册）

```java
// 1. 配置启用注册表模式
// application.yml: dreamtof.context.registry.enabled=true

// 2. 定义 Bean（自动使用 Class 作为 key）
@Bean
public ThreadContextCopier dataSourceCopier() {
    return new DataSourceCopier();
}

// 3. 通过 Class 类型卸载（编译期安全）
ContextRegistry.unregisterCopier(DataSourceCopier.class);

// 4. 通过 Class 类型获取
DataSourceCopier copier = ContextRegistry.getCopier(DataSourceCopier.class);
```

### 配置切换

```yaml
# application.yml
dreamtof:
  context:
    registry:
      enabled: true  # 启用注册表模式，Spring Bean 自动注入将被忽略
```

### 优先级规则
1. **注册表模式**（配置 enabled=true）：使用 ContextRegistry 显式注册，Spring Bean 自动注入被忽略
2. **Spring Bean 自动注入**（默认）：自动注入所有 ThreadContextCopier Bean

### 解决的问题
- Spring Bean 自动注入实现零配置注册，只需加 @Component 即可
- 注册表模式提供显式注册，便于管理、调试和动态卸载
- 两种模式互斥，配置了注册表则 Spring Bean 自动注入不再工作

---

## 2. 注册表模式（Registry Pattern）

### 实现位置
`cn.dreamtof.core.exception.ErrorCode`

### 示例代码

```java
@Getter
public abstract class ErrorCode implements IErrorCode {
    private static final Map<Integer, ErrorCode> REGISTRY = new ConcurrentHashMap<>();

    protected ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
        // 自动注册到全局表
        if (REGISTRY.containsKey(code)) {
            throw new IllegalStateException("禁止注册相同错误码: " + code);
        }
        REGISTRY.put(code, this);
    }

    public static ErrorCode getByCode(int code) {
        return REGISTRY.get(code);
    }
}
```

### 解决的问题
- 全局统一管理错误码，防止重复定义
- 支持通过错误码反向查找错误对象

---

## 3. 工厂模式 + ClassValue 缓存

### 实现位置
`cn.dreamtof.core.utils.FastBeanMeta`

### 示例代码

```java
public class FastBeanMeta {
    private static final ClassValue<FastBeanMeta> CACHE = new ClassValue<>() {
        @Override
        protected FastBeanMeta computeValue(Class<?> type) {
            return new FastBeanMeta(type);
        }
    };

    public static FastBeanMeta of(Class<?> clazz) {
        return CACHE.get(clazz);
    }
}
```

### 解决的问题
- 使用 ClassValue 替代 ConcurrentHashMap，在类卸载时自动清理缓存
- 避免类加载器导致的 Metaspace OOM
- 比 ConcurrentHashMap 更快，因为它是 JVM 内置的类关联元数据机制

---

## 4. 模板方法模式（Template Method）

### 实现位置
`cn.dreamtof.core.exception.BaseExceptionRegistry`

### 示例代码

```java
public abstract class BaseExceptionRegistry {
    protected final Map<Class<? extends Throwable>, IErrorCode> mappings = new HashMap<>();
    
    public IErrorCode getErrorCode(Throwable throwable) {
        if (throwable == null) return CommonErrorCode.SYSTEM_ERROR;
        Throwable root = unwrap(throwable);  // 步骤1：拆包
        Class<? extends Throwable> clazz = root.getClass();
        IErrorCode cached = cache.get(clazz);
        if (cached != null) return cached;
        IErrorCode match = findMatch(clazz);  // 步骤2：查找
        if (match == null) match = CommonErrorCode.SYSTEM_ERROR;
        cache.put(clazz, match);
        return match;
    }
    
    protected Throwable unwrap(Throwable e) { 
        // 钩子方法：子类可重写
    }
}
```

### 解决的问题
- 定义异常处理的统一骨架流程
- 子类只需注册特定异常映射，无需重复实现查找逻辑

---

## 5. 策略模式（Strategy Pattern）

### 实现位置
`cn.dreamtof.core.utils.SmartValidator`

### 示例代码

```java
private static void performCheck(Object value, Check check) {
    switch (value) {
        case String s -> {
            if (check.notBlank() && s.isBlank()) {
                throw new BusinessException(check.message());
            }
            if (check.regex().length() > 0 && !s.matches(check.regex())) {
                throw new BusinessException(check.message());
            }
        }
        case Number n -> {
            if (check.min() != Long.MIN_VALUE && n.longValue() < check.min()) {
                throw new BusinessException(check.message());
            }
            if (check.max() != Long.MAX_VALUE && n.longValue() > check.max()) {
                throw new BusinessException(check.message());
            }
        }
        case Collection<?> coll -> {
            if (check.notEmpty() && coll.isEmpty()) {
                throw new BusinessException(check.message());
            }
        }
        default -> {}
    }
}
```

### 解决的问题
- 根据不同的数据类型动态选择校验算法
- 避免大量的 if-else 类型判断

---

## 6. 装饰器模式（Decorator Pattern）

### 实现位置
`cn.dreamtof.core.context.ContextPropagator`

### 示例代码

```java
public class ContextPropagator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        List<Object> snapshots = new ArrayList<>();
        for (ThreadContextCopier copier : COPIERS) {
            snapshots.add(copier.capture());
        }
        return () -> {
            for (int i = 0; i < COPIERS.size(); i++) {
                COPIERS.get(i).restore(snapshots.get(i));
            }
            try {
                ScopedValue.where(OperationContext.getScopedValue(), childInfo).run(runnable);
            } finally {
                for (ThreadContextCopier copier : COPIERS) {
                    copier.clear();
                }
            }
        };
    }
}
```

### 解决的问题
- 在不修改原始 Runnable 的情况下，为其添加上下文传播能力
- 保证异步任务执行时能正确传递 TraceId、用户身份等信息

---

## 7. 门面模式（Facade Pattern）

### 实现位置
`cn.dreamtof.core.utils.FastReflectUtils`

### 示例代码

```java
public class FastReflectUtils {
    public static <T> T newInstance(Class<T> clazz) { ... }
    public static Object getPropertyValue(Object target, String fieldName) { ... }
    public static void setPropertyValue(Object target, String fieldName, Object value) { ... }
    public static <T> T shallowCopy(T source) { ... }
    public static <T> T deepCopy(T source) { ... }
}
```

### 解决的问题
- 隐藏复杂的 LambdaMetafactory 调用细节
- 为上层提供简洁统一的反射 API

---

## 8. 空对象模式（Null Object Pattern）

### 实现位置
`cn.dreamtof.core.context.OperationContext`

### 示例代码

```java
public static OperationInfo get() {
    if (CONTEXT.isBound()) {
        return CONTEXT.get();
    }
    return OperationInfo.builder()
            .isDefault(true)
            .traceId(UuidCreator.getTimeOrderedEpoch())
            .deviceId("UNKNOWN-DEVICE")
            .startTime(System.currentTimeMillis())
            .businessModule("System")
            .businessAction("Background-Task")
            .clientIp("127.0.0.1")
            .build();
}
```

### 解决的问题
- 避免业务代码频繁判空
- 在后台任务、定时任务等非 HTTP 请求场景下提供默认上下文
- 通过 `isDefault` 字段区分"真实请求"和"系统降级"

---

## 9. 原型模式（Prototype Pattern）

### 实现位置
`cn.dreamtof.core.utils.FastReflectUtils`

### 示例代码

```java
public static <T> T deepCopy(T source) {
    return deepCopy(source, new IdentityHashMap<>());
}

private static <T> T deepCopy(T source, Map<Object, Object> visited) {
    if (source == null) return null;
    if (isSimpleType(clazz)) return source;
    if (visited.containsKey(source)) return (T) visited.get(source);
    
    if (source instanceof Collection<?> coll) {
        Collection<Object> target = (Collection<Object>) newInstance(clazz);
        visited.put(source, target);
        for (Object item : coll) {
            target.add(deepCopy(item, visited));
        }
        return (T) target;
    }
    
    if (source instanceof Map<?, ?> map) {
        Map<Object, Object> target = (Map<Object, Object>) newInstance(clazz);
        visited.put(source, target);
        for (var entry : map.entrySet()) {
            target.put(deepCopy(entry.getKey(), visited), deepCopy(entry.getValue(), visited));
        }
        return (T) target;
    }
    
    T target = (T) newInstance(clazz);
    visited.put(source, target);
    for (var accessor : accessors) {
        Object val = accessor.getter().apply(source);
        accessor.setter().accept(target, deepCopy(val, visited));
    }
    return target;
}
```

### 解决的问题
- 使用 IdentityHashMap 解决循环引用问题
- 支持集合、Map、POJO 等多种类型的递归深拷贝
- 结合 FastBeanMeta 的 Lambda 访问器实现高性能克隆

---

## 设计模式总结

| 设计模式 | 核心应用场景 |
|---------|-------------|
| Spring Bean 自动注入 + 注册表模式 | 上下文传播扩展（双模式互斥） |
| 注册表模式 | 错误码全局管理 |
| 工厂模式 | Bean元数据缓存创建 |
| 模板方法 | 异常处理流程 |
| 策略模式 | 类型校验算法选择 |
| 装饰器模式 | 异步任务上下文传播 |
| 门面模式 | 反射工具统一入口 |
| 空对象模式 | 默认操作上下文 |
| 原型模式 | 对象深拷贝 |
