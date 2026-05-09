---
alwaysApply: false
description: 当涉及基础编码风格、参数校验、事务管理、异步上下文、对象转换、审计字段、异常处理与响应时触发。
---

# 编码约定

## 方法 & 参数

| 禁止 | 替代 |
| -- | -- |
| 方法体 > 120 行 | 拆分为私有方法 |
| 方法参数 > 4 个 | 封装为 Request DTO |
| `Optional<T>` 返回值 | 返回 T 或 null |
| 过长链式调用 | 分步检查，使用 Asserts |

## 枚举
状态、类型、类别等固定值字段 **必须** 使用枚举，禁止字符串/数字魔法值。
| 使用范围 | 枚举位置 |
| -- | -- |
| 单模块  | `domain/model/enums/` |
| 跨模块  | `common/enums/`|
| 单类使用 | 内部枚举 |

## 参数校验

使用 `@Check` 注解标记字段，属性：`msg` `required` `notNull` `min` `max` `positive` `noNulls` `regex` `type`(mobile/email/idcard/numeric) `valid`(递归校验)。

在 Service 层调用 `SmartValidator.validate(request)` 自动执行。

| 禁止 | 替代 |
| -- | -- |
| Controller 层做业务校验 | Service 层用 SmartValidator |

## 事务

| 禁止 | 替代|
| -- | -- |
| `@Transactional` 注解 | `TransactionTemplate` 编程式事务 |
| 单表操作开事务 | 仅多表/多次写操作才开事务 |

## 异步 & 上下文

| 禁止 | 替代 |
| -- | -- |
| `new Thread()` / `executor.execute()` | `VirtualTaskManager`（自动携带上下文） |
| 构造函数中获取 OperationContext | 方法执行时获取 |

上下文获取：`OperationContext.get().getOperator()` / `OperationContext.traceId()`

## 对象转换

| 禁止 | 替代 |
| -- | -- |
| Builder/Setter 链式构造对象（十几行） | MapStruct 转换（DTO↔Entity↔PO 一律 MapStruct） |

## 审计字段

| 禁止 | 替代 |
| -- | -- |
| 手动设置 createdBy/updatedBy/createdTime / updatedTime / version / isDeleted | 框架自动填充（MyBatisFlexConfig InsertListener/UpdateListener + 全局乐观锁） |

## 异常与响应

### Asserts（统一断言）

- `Asserts.notNull(obj, msg)` / `Asserts.notBlank(str, msg)` / `Asserts.notEmpty(list, msg)`
- `Asserts.isTrue(condition, msg)` / `Asserts.fail(errorCode, args...)`

### 响应

- 成功：`ResultUtils.success(data)` → `BaseResponse<T>`
- 失败：**必须** 通过 `Asserts` 抛异常，**禁止** 手动构造 `BaseResponse`

  <br />

