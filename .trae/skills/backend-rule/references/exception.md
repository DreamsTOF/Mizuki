# 异常处理规范

## Asserts 工具类

统一使用 Asserts 进行业务校验，抛出 BusinessException。

### 常用方法

| 方法 | 说明 |
|------|------|
| `Asserts.notNull(obj, "消息")` | 非空校验 |
| `Asserts.notBlank(str, "消息")` | 非空白校验 |
| `Asserts.notEmpty(list, "消息")` | 集合非空校验 |
| `Asserts.isTrue(condition, "消息")` | 条件校验 |
| `Asserts.fail(errorCode, args...)` | 直接抛异常 |

### 使用示例

```java
// 非空校验
Asserts.notNull(user, "用户不存在");

// 条件校验
Asserts.isTrue(order.getAmount().compareTo(BigDecimal.ZERO) > 0, "订单金额必须大于0");

// 直接抛异常
Asserts.fail(OrderErrorCode.INSUFFICIENT_STOCK, productId, quantity);
```

## 错误码范围

| 范围 | 分类 |
|------|------|
| 0 | 成功 |
| 10000-19999 | 基础错误 |
| 20000-29999 | 认证错误 |
| 30000-39999 | 业务错误 |
| 40000-49999 | 资源错误 |

## 返回值规范

统一使用 ResultUtils.success() 返回成功响应：

```java
return ResultUtils.success(appService.getDetail(id));
```

## 失败响应

失败响应统一抛出异常，不要手动构造：

```java
// 正确
Asserts.notNull(user, "用户不存在");

// 错误
return new BaseResponse<>(10000, null, "用户不存在", false);
```

---

## ❌ 绝对禁止 (Critical Constraints)

 **禁止手动构造失败的 `BaseResponse`** → **必须**使用 `Asserts` 工具类抛出业务异常阻断流程。
