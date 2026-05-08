---
name: database-table-spec
description: 数据库建表规范。当用户需要创建数据库表、设计数据库结构、审查建表语句、定义表字段、CREATE TABLE、表设计、字段定义、添加字段、优化表结构、设计表关系时，必须使用此 skill。确保所有建表操作符合统一的规范标准，包括命名规范、字段类型、必备字段、索引设计等。
---

# 数据库建表规范

本规范定义了数据库表设计的统一标准，确保所有表结构遵循一致的命名、字段类型、索引和约束规范。

## 核心原则

1. **命名语义化**：表名和字段名必须能够清晰表达业务含义
2. **统一命名风格**：使用 snake_case（小写字母+下划线）
3. **必备字段完整**：每张表必须包含规定的必备字段
4. **注释完整清晰**：表和字段必须有中文注释

## 命名规范

### 表名规范
- 使用 snake_case 命名风格
- 不需要模块或项目前缀
- 使用名词或名词短语，表达清晰的业务含义
- 禁止使用数据库保留字
- 示例：`user`, `order`, `order_item`, `product_category`

### 字段名规范
- 使用 snake_case 命名风格
- 字段名必须语义化，能够清晰表达业务含义
- 避免使用缩写，除非是通用缩写（如 id, url）
- 禁止使用数据库保留字
- 示例：`user_name`, `email_address`, `order_status`, `created_at`

## 必备字段规范

每张表必须包含以下必备字段，按照指定顺序排列：

1. **id** - 主键字段，排在第一位
   - 类型：UUID
   - 默认值：`uuidv7()`
   - 注释：'主键ID'
   - 每张表只能有一个主键

2. **业务字段** - 中间位置
   - 根据业务需要定义

3. **create_time** - 创建时间
   - 类型：TIMESTAMP 或 DATETIME
   - 默认值：CURRENT_TIMESTAMP
   - 注释：'创建时间'

4. **update_time** - 更新时间
   - 类型：TIMESTAMP 或 DATETIME
   - 默认值：CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
   - 注释：'更新时间'

5. **created_by** - 创建人
   - 类型：UUID 或 VARCHAR(36)
   - 默认值：NULL
   - 注释：'创建人ID'

6. **updated_by** - 更新人
   - 类型：UUID 或 VARCHAR(36)
   - 默认值：NULL
   - 注释：'更新人ID'

7. **deleted** - 软删除标记
   - 类型：INT 或 TINYINT
   - 默认值：0
   - 注释：'删除标记：0-未删除，1-已删除'

8. **version** - 乐观锁版本号
   - 类型：INT
   - 默认值：0
   - 注释：'版本号，用于乐观锁'

## 字段类型规范

### 字符串类型
- **VARCHAR**：用于短字符串，根据实际需要设定长度
  - 姓名：VARCHAR(50)
  - 邮箱：VARCHAR(100)
  - 手机号：VARCHAR(20)
  - URL：VARCHAR(255)
- **TEXT**：用于长文本内容
- **LONGTEXT**：用于超大文本内容
- 默认值：NULL

### 数值类型
- **TINYINT(4)**：用于状态、类型等枚举字段
- **INT**：用于整数
- **BIGINT**：用于大整数
- **DECIMAL**：用于金额等需要精确计算的字段
  - 金额：DECIMAL(18,2) 或根据业务需要设定精度
- 默认值：0

### 时间类型
- **TIMESTAMP**：推荐用于时间字段
- **DATETIME**：也可用于时间字段
- 默认值：CURRENT_TIMESTAMP

### JSON 类型
- **JSON**：用于存储复杂的结构化数据
- 默认值：NULL

### UUID 类型
- **UUID**：用于主键 ID
- 默认值：uuidv7()

## 索引规范

### 索引命名
- 普通索引：`idx_字段名` 或 `idx_字段名1_字段名2`（组合索引）
- 唯一索引：`uk_字段名` 或 `uk_字段名1_字段名2`（组合唯一索引）

### 索引创建原则
1. 外键字段必须创建索引
2. 常用查询条件字段必须创建索引
3. 组合索引遵循最左前缀原则
4. 区分度高的字段放在组合索引前面
5. 单表索引数量建议不超过 5 个

### 索引示例
```sql
-- 普通索引
CREATE INDEX idx_user_id ON order(user_id);

-- 组合索引
CREATE INDEX idx_status_create_time ON order(status, create_time);

-- 唯一索引
CREATE UNIQUE INDEX uk_email ON user(email);
```

## 外键约束规范

- **不使用物理外键**：不在数据库层面创建外键约束
- **使用逻辑外键**：在应用层维护关联关系
- 外键字段命名：`关联表名_id`，如 `user_id`, `order_id`

## 注释规范

### 表注释
- 每张表必须有表注释
- 使用中文说明表的用途
- 示例：`COMMENT '用户信息表'`

### 字段注释
- 每个字段必须有注释
- 使用中文说明字段含义
- 状态类字段需要注释说明各个枚举值的含义
- 示例：
  ```sql
  `status` TINYINT(4) DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消'
  ```

## 建表语句格式规范

### 格式要求
1. 显式指定存储引擎和字符集
2. 每个字段单独一行，便于阅读和维护
3. 字段顺序：主键在前，业务字段在中间，必备字段在后
4. 使用统一的缩进格式

### 建表语句模板
```sql
CREATE TABLE `table_name` (
    `id` UUID DEFAULT uuidv7() COMMENT '主键ID',
    
    -- 业务字段
    `field_name` VARCHAR(100) DEFAULT NULL COMMENT '字段说明',
    
    -- 必备字段
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` UUID DEFAULT NULL COMMENT '创建人ID',
    `updated_by` UUID DEFAULT NULL COMMENT '更新人ID',
    `deleted` INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '版本号，用于乐观锁',
    
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表说明';
```

## 完整示例

### 用户表示例
```sql
CREATE TABLE `user` (
    `id` UUID DEFAULT uuidv7() COMMENT '主键ID',
    
    -- 用户基本信息
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    `password_hash` VARCHAR(255) DEFAULT NULL COMMENT '密码哈希值',
    `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT(4) DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    `gender` TINYINT(4) DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATE DEFAULT NULL COMMENT '出生日期',
    `profile` TEXT DEFAULT NULL COMMENT '个人简介',
    `settings` JSON DEFAULT NULL COMMENT '用户设置JSON',
    
    -- 必备字段
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` UUID DEFAULT NULL COMMENT '创建人ID',
    `updated_by` UUID DEFAULT NULL COMMENT '更新人ID',
    `deleted` INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '版本号，用于乐观锁',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
```

### 订单表示例
```sql
CREATE TABLE `order` (
    `id` UUID DEFAULT uuidv7() COMMENT '主键ID',
    
    -- 订单基本信息
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '订单编号',
    `user_id` UUID DEFAULT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '订单总金额',
    `discount_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '优惠金额',
    `pay_amount` DECIMAL(18,2) DEFAULT 0 COMMENT '实付金额',
    `status` TINYINT(4) DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
    `pay_time` TIMESTAMP DEFAULT NULL COMMENT '支付时间',
    `ship_time` TIMESTAMP DEFAULT NULL COMMENT '发货时间',
    `complete_time` TIMESTAMP DEFAULT NULL COMMENT '完成时间',
    `remark` TEXT DEFAULT NULL COMMENT '订单备注',
    `extra_data` JSON DEFAULT NULL COMMENT '扩展数据',
    
    -- 必备字段
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` UUID DEFAULT NULL COMMENT '创建人ID',
    `updated_by` UUID DEFAULT NULL COMMENT '更新人ID',
    `deleted` INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '版本号，用于乐观锁',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status_create_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';
```

## 工作流程

当用户需要创建数据库表时，按照以下流程执行：

1. **理解业务需求**
   - 询问用户表的业务用途
   - 了解需要存储的数据内容
   - 确认表的关联关系

2. **设计表结构**
   - 根据业务需求设计字段
   - 确定字段类型和长度
   - 添加必备字段

3. **生成建表语句**
   - 按照规范格式生成 CREATE TABLE 语句
   - 添加完整的注释
   - 创建必要的索引

4. **输出内容**
   - 完整的建表语句
   - 字段清单说明
   - 索引创建建议

## 注意事项

1. 所有表必须包含完整的必备字段
2. 字段命名必须语义化，避免使用无意义的名称
3. 状态类字段必须注释说明各个枚举值的含义
4. 金额字段必须使用 DECIMAL 类型，避免精度丢失
5. 外键字段必须创建索引
6. 不使用物理外键约束
7. 表和字段必须有中文注释
