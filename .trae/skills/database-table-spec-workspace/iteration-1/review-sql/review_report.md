-- SQL 审查报告

## 原始建表语句
```sql
CREATE TABLE user (id INT PRIMARY KEY, name VARCHAR(50), email VARCHAR(100));
```

## 审查结果：不符合规范

根据数据库建表规范，该建表语句存在以下问题：

### 1. 主键类型错误
- 当前：`id INT PRIMARY KEY`
- 规范要求：主键应使用 UUID 类型，默认值为 uuidv7()
- 改进：`id UUID DEFAULT uuidv7() COMMENT '主键ID'`

### 2. 缺少必备字段
- 当前：只有 id, name, email 三个字段
- 规范要求：必须包含完整的必备字段
- 缺少字段：create_time, update_time, created_by, updated_by, deleted, version

### 3. 缺少字段注释
- 当前：所有字段都没有注释
- 规范要求：每个字段必须有中文注释说明字段含义

### 4. 缺少表注释
- 当前：没有表注释
- 规范要求：每张表必须有中文注释说明表的用途

### 5. 缺少存储引擎和字符集声明
- 当前：没有指定 ENGINE 和 CHARSET
- 规范要求：显式指定存储引擎和字符集

### 6. 缺少索引
- 当前：没有创建任何索引
- 规范要求：外键字段、常用查询字段应创建索引
- 建议：email 字段应创建唯一索引

### 7. 字段命名不够语义化
- 当前：`name` 字段名不够明确
- 规范要求：字段名必须语义化
- 建议：改为 `user_name` 或 `username`

### 8. 缺少默认值
- 当前：字段没有默认值
- 规范要求：字段应设置合理的默认值

## 改进后的建表语句

```sql
CREATE TABLE `user` (
    `id` UUID DEFAULT uuidv7() COMMENT '主键ID',
    
    -- 用户基本信息
    `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    
    -- 必备字段
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` UUID DEFAULT NULL COMMENT '创建人ID',
    `updated_by` UUID DEFAULT NULL COMMENT '更新人ID',
    `deleted` INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '版本号，用于乐观锁',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';
```
