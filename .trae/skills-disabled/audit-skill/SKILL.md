---
name: audit-helper
description: 自动审计注解助手。当用户提及"审计"、"添加审计注解"、"@AuditLog"等关键词时触发。扫描 application/service 层方法，识别 Repository 写操作，自动添加 @AuditLog 注解；扫描 DTO 参数，为 ID 字段添加 @AuditEntityId 和 @AuditReference 注解。支持包名到中文模块名的自动映射，生成变更报告。
---

# Audit Helper Skill

## 一、技能目标

扫描 application/service 层方法，识别 Repository 写操作，自动添加审计注解：

1. **方法级注解**: 在 app 方法上添加 `@AuditLog` 注解
2. **DTO 字段注解**: 在 DTO 的 ID 字段上添加 `@AuditEntityId` 和 `@AuditReference` 注解
3. **变更报告**: 生成 Markdown 格式的变更报告

**核心原则**:
- 只添加注解，不修改任何其他代码
- 保留已有注解的原有属性值
- 无法确定时添加注解但留空不确定的属性

---

## 二、扫描范围与识别规则

### 2.1 扫描范围

用户指定的目录，通常为：
- DDD 架构: `application` 包下的类
- MVC 架构: `service` 包下的类

### 2.2 识别 App 方法

| 架构 | 识别方式 |
|------|---------|
| DDD | `application` 包下的类中的方法 |
| MVC | `service` 包下的类中的方法 |

### 2.3 识别 PO 类

- 使用 `@Table` 注解标记的类
- 类名通常以 `PO` 结尾

### 2.4 识别 Repository

- 类名以 `Repository` 结尾
- 通过泛型参数识别关联的实体类: `extends JpaRepository<UserPO, Long>` → `UserPO`

### 2.5 识别写操作方法

| Repository 方法 | 操作类型 | action 值 |
|----------------|---------|----------|
| `save()` | INSERT | 新增 |
| `insert()` | INSERT | 新增 |
| `update()` | UPDATE | 更新 |
| `delete()` | DELETE | 删除 |
| `saveAll()` | 批量 INSERT | 批量新增 |
| `deleteAll()` | 批量 DELETE | 批量删除 |
| `deleteBatch()` | 批量 DELETE | 批量删除 |

**注意**:
- `save()` 方法统一识别为 INSERT 操作
- 查询方法（select/find/get/list 等）跳过，不添加注解
- 识别所有 Repository 调用，将所有实体类都加入 entities

---

## 三、@AuditLog 注解处理

### 3.1 注解位置

打在 app 方法上

### 3.2 属性生成规则

#### module 属性

根据包名自动翻译为中文：

1. 读取配置文件: `.trae/rules/module-mapping.json`
2. 内置映射:
   ```json
   {
     "user": "用户管理",
     "order": "订单管理",
     "product": "商品管理",
     "payment": "支付管理",
     "inventory": "库存管理"
   }
   ```
3. 如果包名没有在映射中找到，提示用户输入

#### action 属性

根据方法体操作类型判定：

| 操作类型 | action 值 |
|---------|----------|
| INSERT | 新增 |
| UPDATE | 更新 |
| DELETE | 删除 |
| 批量 INSERT | 批量新增 |
| 批量 UPDATE | 批量更新 |
| 批量 DELETE | 批量删除 |

如果方法中有多种操作类型，选择第一个写操作的类型。

#### entity/entities 属性

- 单实体: 使用 `entity = XXXPO.class`
- 多实体: 使用 `entities = {A.class, B.class}`

实体类识别方式：
1. 识别 Repository 调用参数类型: `userRepository.save(user)` → `user` 的类型
2. 根据 Repository 泛型参数识别: `UserRepository extends JpaRepository<UserPO, Long>` → `UserPO`

#### 其他属性

`partial`, `skipNull`, `systemFields`, `refresh` 使用默认值（跟随全局配置）

### 3.3 已有注解处理

如果方法已有 `@AuditLog` 注解：
- 保留原有属性值
- 只补充缺失的实体类到 `entity` 或 `entities` 属性

### 3.4 导入语句

自动添加导入语句：
```java
import cn.dreamtof.audit.annotation.AuditLog;
```

---

## 四、DTO 字段注解处理

### 4.1 DTO 识别

- 位于 `request` 包中的类
- 通常是 application 层方法的入参

### 4.2 @AuditEntityId 注解

**触发条件**: 字段名以 `Id` 结尾（如 `userId`, `orderId`）

**属性设置**:
- `target`: 设置为对应的实体类（根据字段名前缀匹配）
  - `userId` → `UserPO` 或 `User`
  - `orderId` → `OrderPO` 或 `Order`

**实体类匹配规则**:
1. 去掉 `Id` 后缀，首字母大写
2. 尝试匹配 `XXXPO` 或 `XXX`
3. 在项目中搜索对应的实体类

### 4.3 @AuditReference 注解

**触发条件**: 字段名以 `Id` 结尾且能找到对应的实体类

**属性设置**:
- `target`: 设置为对应的实体类
- `label`: 根据实体类字段猜测，优先级: `name` > `username` > `title` > `label`
- `idCol`: 使用默认值 `"id"`

### 4.4 已有注解处理

如果字段已有 `@AuditEntityId` 或 `@AuditReference` 注解：
- 补充缺失的属性

### 4.5 不确定时处理

如果无法确定实体类或 label 字段：
- 添加注解但留空不确定的属性
- 添加行内注释说明原因: `// TODO: 无法确定实体类`

---

## 五、执行流程

### Step 1: 加载配置

1. 读取 `.trae/rules/module-mapping.json` 配置文件
2. 合并内置映射和用户自定义映射

### Step 2: 扫描目标目录

1. 识别 application/service 层的类
2. 遍历每个类的每个方法

### Step 3: 分析方法

对于每个方法：

1. **检查是否已有 @AuditLog 注解**
   - 如果有，记录已有属性

2. **分析方法体**
   - 查找 Repository 方法调用: `save`, `insert`, `update`, `delete`, `saveAll`, `deleteAll`, `deleteBatch`
   - 识别调用参数类型
   - 识别 Repository 泛型参数

3. **分析方法参数**
   - 识别参数类型是否为 `@Table` 注解的类
   - 识别 `List<XXXPO>` 泛型参数类型

4. **收集实体类**
   - 去重
   - 确定操作类型

5. **跳过查询方法**
   - 如果只有查询操作，跳过该方法

### Step 4: 生成注解

1. **确定 module**
   - 根据包名查找映射
   - 未找到则提示用户输入

2. **确定 action**
   - 根据操作类型生成

3. **确定 entity/entities**
   - 单实体用 entity
   - 多实体用 entities

### Step 5: 修改源文件

1. 添加 `@AuditLog` 注解
2. 添加导入语句
3. 保留原有属性值

### Step 6: 处理 DTO

对于方法参数中的 DTO 类：

1. 扫描字段
2. 识别 ID 字段（以 `Id` 结尾）
3. 添加 `@AuditEntityId` 和 `@AuditReference` 注解
4. 添加导入语句

### Step 7: 生成报告

生成 Markdown 格式的变更报告，包含：
- 修改的方法名
- 添加的注解内容
- 跳过的方法及原因

---

## 六、注解模板

### 6.1 @AuditLog 模板

```java
// 单实体
@AuditLog(
    module = "用户管理",
    action = "新增",
    entity = UserPO.class
)
public void createUser(UserCreateReq req) { ... }

// 多实体
@AuditLog(
    module = "订单管理",
    action = "更新",
    entities = {OrderPO.class, OrderItemPO.class}
)
public void updateOrder(OrderUpdateReq req) { ... }

// 批量操作
@AuditLog(
    module = "商品管理",
    action = "批量新增",
    entity = ProductPO.class
)
public void batchImport(List<ProductPO> products) { ... }
```

### 6.2 @AuditEntityId 模板

```java
public class UserUpdateReq {
    @AuditEntityId(target = UserPO.class)
    private Long userId;
}
```

### 6.3 @AuditReference 模板

```java
public class OrderCreateReq {
    @AuditReference(
        target = UserPO.class,
        label = "username"
    )
    private Long userId;

    @AuditReference(
        target = ProductPO.class,
        label = "name"
    )
    private Long productId;
}
```

---

## 七、变更报告格式

### 7.1 文件名

`audit-report-YYYY-MM-DD-HH-mm.md`

### 7.2 文件位置

项目根目录

### 7.3 报告内容

```markdown
# 审计注解变更报告

生成时间: 2024-01-15 14:30:00

## 修改的方法

### UserService.createUser

**文件**: `src/main/java/com/example/application/UserService.java`

**添加注解**:
```java
@AuditLog(
    module = "用户管理",
    action = "新增",
    entity = UserPO.class
)
```

**识别到的实体类**: `UserPO`

---

### OrderService.updateOrder

**文件**: `src/main/java/com/example/application/OrderService.java`

**添加注解**:
```java
@AuditLog(
    module = "订单管理",
    action = "更新",
    entities = {OrderPO.class, OrderItemPO.class}
)
```

**识别到的实体类**: `OrderPO`, `OrderItemPO`

---

## 跳过的方法

### UserService.getUserById

**原因**: 只有查询操作，无需审计

---

## DTO 字段注解变更

### UserCreateReq

**文件**: `src/main/java/com/example/dto/request/UserCreateReq.java`

**添加注解**:
- `departmentId`: `@AuditReference(target = DepartmentPO.class, label = "name")`

---

## 统计

- 扫描方法数: 20
- 添加注解方法数: 15
- 跳过方法数: 5
- 处理 DTO 数: 10
```

---

## 八、配置文件格式

### 8.1 module-mapping.json

位置: `.trae/rules/module-mapping.json`

```json
{
  "user": "用户管理",
  "order": "订单管理",
  "product": "商品管理",
  "payment": "支付管理",
  "inventory": "库存管理",
  "song": "歌曲管理",
  "label": "标签管理",
  "category": "分类管理"
}
```

---

## 九、错误处理

### 9.1 无法识别实体类

- 跳过该方法
- 在报告中记录原因

### 9.2 无法确定 module

- 提示用户输入
- 用户输入后继续处理

### 9.3 无法确定 label 字段

- 添加注解但留空 label 属性
- 添加行内注释: `// TODO: 无法确定 label 字段`

### 9.4 文件读取失败

- 跳过该文件
- 在报告中记录原因

---

## 十、注意事项

1. **只添加注解，不修改其他代码**
2. **保留已有注解的原有属性值**
3. **自动添加导入语句**
4. **跳过查询方法**
5. **识别批量操作方法**
6. **处理 List 泛型参数类型**
7. **生成变更报告**

---

**版本**: 2.0.0
**更新日期**: 2026-04-19
