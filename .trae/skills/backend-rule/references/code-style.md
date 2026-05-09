# 代码风格规范

---

## 代码长度限制

### 方法长度

| 规则 | 说明 |
|------|------|
| **强制上限** | 方法体不超过 120 行 |
| **处理方式** | 超过 120 行必须拆分为多个私有方法 |
| **拆分原则** | 每个方法保持单一职责 |

### 参数数量

| 规则 | 说明 |
|------|------|
| **强制上限** | 方法参数不超过 4 个 |
| **处理方式** | 超过 4 个参数必须封装为对象 |
| **封装位置** | 使用 Request DTO 或专门的参数对象 |

---

## 注释规范

### 必须注释的场景

| 场景 | 注释类型 | 说明 |
|------|----------|------|
| 公共 API 方法 | Javadoc | 说明方法用途、参数、返回值、异常 |
| 复杂业务逻辑 | 行内注释 | 解释业务规则和决策原因 |
| 关键算法 | 块注释 | 说明算法思路和关键步骤 |
| 边界条件 | 行内注释 | 说明特殊处理的原因 |

### Javadoc 格式

```java
/**
 * 根据用户名查询用户
 *
 * @param username 用户名，不能为空
 * @return 用户实体
 * @throws BusinessException 用户不存在时抛出异常
 */
public User getByUsername(String username) {
    Asserts.notBlank(username, "用户名不能为空");
    UserPO po = repository.getByUsername(username);
    Asserts.notNull(po, "用户不存在");
    return assembler.toEntity(po);
}
```

### 复杂逻辑注释

```java
public void processOrder(Order order) {
    // 业务规则：订单金额超过1000元需要人工审核
    if (order.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
        order.setStatus(OrderStatus.PENDING_REVIEW);
    }
    
    // 边界条件：库存不足时回滚并通知用户
    if (!inventoryService.checkStock(order.getProductId(), order.getQuantity())) {
        throw new BusinessException(OrderErrorCode.INSUFFICIENT_STOCK);
    }
}
```

---

## null 值处理

### 核心原则

| 规则 | 说明 |
|------|------|
| **使用 Asserts** | 业务规则校验使用 Asserts 工具类（详细用法见 [exception.md](exception.md)） |
| **禁止 Optional** | 不使用 `Optional<T>` 包装返回值 |
| **减少链式调用** | 避免过长的链式调用，拆分为多步 |

---

## 异常消息规范

### 枚举强制使用规范

**核心原则**：对于只有少数几个固定值的字段（如状态、类型、类别等），**必须使用枚举**，禁止使用字符串魔法值。

| 场景 | 必须使用枚举 | 示例 |
|------|--------------|------|
| 用户状态 | ✅ 必须 | `UserStatus.ACTIVE` 而非 `"ACTIVE"` |
| 订单状态 | ✅ 必须 | `OrderStatus.PENDING` 而非 `"PENDING"` |
| 性别 | ✅ 必须 | `Gender.MALE` 而非 `"MALE"` |
| 角色类型 | ✅ 必须 | `RoleType.ADMIN` 而非 `"ADMIN"` |
| 支付状态 | ✅ 必须 | `PaymentStatus.SUCCESS` 而非 `"SUCCESS"` |
| 日志级别 | ✅ 必须 | `LogLevel.INFO` 而非 `"INFO"` |
| 布尔标志 | ❌ 使用 boolean | `isActive` 而非 `"1"/"0"` |
| 无限制文本 | ❌ 使用 String | 用户名、描述等 |

**禁止行为**：
- ❌ 禁止使用字符串字面量表示状态：`"ACTIVE"`、`"PENDING"`、`"DELETED"`
- ❌ 禁止使用数字常量表示状态：`0`、`1`、`2`
- ❌ 禁止在代码中进行字符串比较：`"ACTIVE".equals(status)`
- ✅ 必须使用枚举类型：`UserStatus status`

### 枚举位置

枚举类的定义位置根据使用范围灵活选择：

| 使用范围 | 建议位置 |
|----------|----------|
| 仅在单个模块使用 | 模块的 `domain/model/enums/` 目录 |
| 跨模块使用 | 公共模块的 `common/enums/` 目录 |
| 仅在单个类使用 | 作为内部枚举 |

---

## 禁止事项

| 禁止 | 原因 | 替代方案 |
|------|------|----------|
| `Optional<T>` 返回值 | 增加复杂度，不符合团队习惯 | 直接返回对象或 null |
| 过长的链式调用 | 空指针异常难以定位 | 逐步检查，使用 Asserts |
| 魔法值 | 代码可读性差 | 使用常量或枚举 |
| 方法参数超过 4 个 | 可读性差，难以维护 | 封装为 Request DTO |

---

## ❌ 绝对禁止 (Critical Constraints)

**禁止使用 XML 配置文件** → **必须**完全采用 MyBatis-Flex 的代码和注解方式。
**禁止使用 `Optional<T>` 作为返回值** → **必须**直接返回对象或 `null`。
**禁止使用字符串或数字字面量表示状态、类型等有限集字段** → **必须**定义并使用枚举（Enum）。
