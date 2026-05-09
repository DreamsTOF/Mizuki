# 上下文管理规范

## 核心原则

所有异步任务统一使用 VirtualTaskManager，自动携带上下文。

## VirtualTaskManager 使用

```java
// 提交异步任务
VirtualTaskManager.execute(() -> {
    // 上下文自动传播
    UUID traceId = OperationContext.traceId();
    Operator operator = OperationContext.get().getOperator();
});

// 异步获取结果
public CompletableFuture<Result> asyncProcess(Data data) {
    return VirtualTaskManager.supply(() -> doProcess(data));
}
```

## 获取上下文信息

```java
// 获取当前操作人
Operator operator = OperationContext.get().getOperator();

// 获取追踪 ID
UUID traceId = OperationContext.traceId();

// 判断请求类型
if (OperationContext.isReal()) {
    // 真实 HTTP 请求
}
```

## 禁止事项

| 禁止 | 替代方案 |
|------|----------|
| new Thread() | VirtualTaskManager.execute() |
| 直接调用 executor.execute() | 配置 ContextPropagator |
| 在构造函数中获取 OperationContext | 在方法执行时获取 |

---

## ❌ 绝对禁止 (Critical Constraints)

**禁止使用 `new Thread()` 或直接调用 `executor.execute()`** → **必须**使用 `VirtualTaskManager.execute()` 以保证上下文自动传播。
