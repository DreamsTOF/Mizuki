# Timeline（时间线）模块后端功能需求文档

> **模块名称**: Timeline
> **文档类型**: 后端 API 需求文档（BRD/API Spec）
> **目标读者**: 后端开发工程师、数据库工程师、API 测试工程师
> **文档版本**: v1.0
> **最后更新**: 2026-05-04

---

## 1. 概述

本文档为 Mizuki 博客系统 Timeline（时间线）模块的后端开发提供详细需求说明，涵盖数据库设计、功能接口定义、业务逻辑规则、数据校验等方面。

### 1.1 设计原则

- **前后端契约**: 严格对齐前端 `TimelineItem` 数据结构
- **安全性**: 写操作强制管理员身份认证
- **可扩展性**: 数据库设计预留未来扩展字段

---

## 2. 数据库表设计

### 2.1 主表: `timeline_events`

存储时间线事件的核心信息。

```sql
CREATE TABLE timeline_events (
    id              VARCHAR(36) PRIMARY KEY,        -- UUID，全局唯一标识
    title           VARCHAR(255) NOT NULL,          -- 事件标题
    description     TEXT NOT NULL,                  -- 事件详细描述
    type            VARCHAR(20) NOT NULL,           -- 事件类型枚举值
    icon            VARCHAR(100) NOT NULL,          -- 图标标识符
    color           VARCHAR(7) NOT NULL,            -- HEX 颜色值，如 #2A53DD
    start_date      DATE NOT NULL,                  -- 事件开始日期
    end_date        DATE,                           -- 事件结束日期（NULL 表示进行中）
    location        VARCHAR(255),                   -- 地点
    organization    VARCHAR(255),                   -- 所属机构/组织
    position        VARCHAR(255),                   -- 职位/角色
    featured        BOOLEAN NOT NULL DEFAULT FALSE, -- 是否为重点展示事件

    -- 审计字段
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 约束
    CONSTRAINT chk_type CHECK (type IN ('education', 'work', 'project', 'achievement')),
    CONSTRAINT chk_color_format CHECK (color REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 字段说明

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | `VARCHAR(36)` | 否 | - | UUID v4，由应用层生成或使用数据库 UUID 函数 |
| `title` | `VARCHAR(255)` | 否 | - | 事件标题，最大 255 字符 |
| `description` | `TEXT` | 否 | - | 事件描述，建议限制 5000 字符 |
| `type` | `VARCHAR(20)` | 否 | - | 枚举：`education`, `work`, `project`, `achievement` |
| `icon` | `VARCHAR(100)` | 否 | - | 图标标识符，如 `material-symbols:school` |
| `color` | `VARCHAR(7)` | 否 | - | HEX 颜色值，必须匹配 `^#[0-9A-Fa-f]{6}$` |
| `start_date` | `DATE` | 否 | - | 事件开始日期，格式 `YYYY-MM-DD` |
| `end_date` | `DATE` | 是 | `NULL` | 事件结束日期，格式 `YYYY-MM-DD`。NULL 表示进行中 |
| `location` | `VARCHAR(255)` | 是 | `NULL` | 地理位置描述 |
| `organization` | `VARCHAR(255)` | 是 | `NULL` | 机构/组织名称 |
| `position` | `VARCHAR(255)` | 是 | `NULL` | 职位或角色 |
| `featured` | `BOOLEAN` | 否 | `FALSE` | 是否为精选事件 |
| `created_at` | `TIMESTAMP` | 否 | `CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | 否 | `CURRENT_TIMESTAMP` | 最后更新时间 |

### 2.2 关联表: `timeline_event_skills`

存储事件与技能的关联关系（多对多）。

```sql
CREATE TABLE timeline_event_skills (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    timeline_event_id VARCHAR(36) NOT NULL,         -- 关联的时间线事件 ID
    skill_name      VARCHAR(50) NOT NULL,           -- 技能名称

    -- 约束
    CONSTRAINT fk_timeline_event_skills_event
        FOREIGN KEY (timeline_event_id) REFERENCES timeline_events(id) ON DELETE CASCADE,
    CONSTRAINT uq_timeline_event_skill UNIQUE (timeline_event_id, skill_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 设计说明

- 采用独立关联表而非 JSON 数组，便于后续技能统计、筛选及全文检索
- `skill_name` 使用 `VARCHAR(50)`，限制单个技能名称长度
- 设置联合唯一索引 `(timeline_event_id, skill_name)` 防止重复技能标签
- 外键级联删除：删除事件时自动清理关联技能记录

### 2.3 关联表: `timeline_event_achievements`

存储事件与成就的关联关系（一对多）。

```sql
CREATE TABLE timeline_event_achievements (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    timeline_event_id VARCHAR(36) NOT NULL,         -- 关联的时间线事件 ID
    achievement     VARCHAR(500) NOT NULL,          -- 成就描述
    sort_order      INT NOT NULL DEFAULT 0,         -- 排序顺序

    -- 约束
    CONSTRAINT fk_timeline_event_achievements_event
        FOREIGN KEY (timeline_event_id) REFERENCES timeline_events(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.4 关联表: `timeline_event_links`

存储事件与外部链接的关联关系（一对多）。

```sql
CREATE TABLE timeline_event_links (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    timeline_event_id VARCHAR(36) NOT NULL,         -- 关联的时间线事件 ID
    name            VARCHAR(100) NOT NULL,          -- 链接名称
    url             VARCHAR(512) NOT NULL,          -- 链接地址
    link_type       VARCHAR(20) NOT NULL,           -- 链接类型：website, certificate, project, other

    -- 约束
    CONSTRAINT fk_timeline_event_links_event
        FOREIGN KEY (timeline_event_id) REFERENCES timeline_events(id) ON DELETE CASCADE,
    CONSTRAINT chk_link_type CHECK (link_type IN ('website', 'certificate', 'project', 'other'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.5 索引设计

```sql
-- 按日期排序查询（最常用）
CREATE INDEX idx_timeline_start_date ON timeline_events(start_date DESC);

-- 按类型筛选
CREATE INDEX idx_timeline_type ON timeline_events(type);

-- 精选事件查询
CREATE INDEX idx_timeline_featured ON timeline_events(featured);

-- 复合索引：类型 + 日期（用于筛选后排序）
CREATE INDEX idx_timeline_type_start_date ON timeline_events(type, start_date DESC);

-- 复合索引：精选 + 日期（用于精选事件优先排序）
CREATE INDEX idx_timeline_featured_start_date ON timeline_events(featured DESC, start_date DESC);

-- 技能名称索引（用于技能筛选/统计）
CREATE INDEX idx_skills_name ON timeline_event_skills(skill_name);
```

### 2.6 ER 图

```
┌─────────────────────────┐
│     timeline_events     │
├─────────────────────────┤
│ PK  id          VARCHAR │
│     title       VARCHAR │
│     description TEXT    │
│     type        VARCHAR │
│     icon        VARCHAR │
│     color       VARCHAR │
│     start_date  DATE    │
│     end_date    DATE    │
│     location    VARCHAR │
│     organization VARCHAR│
│     position    VARCHAR │
│     featured    BOOLEAN │
│     created_at  TIMESTAMP
│     updated_at  TIMESTAMP
└─────────────────────────┘
            │
            │ 1
            │
            ▼ N
┌─────────────────────────────┐    ┌─────────────────────────────┐    ┌─────────────────────────────┐
│   timeline_event_skills     │    │ timeline_event_achievements │    │    timeline_event_links     │
├─────────────────────────────┤    ├─────────────────────────────┤    ├─────────────────────────────┤
│ PK  id            BIGINT     │    │ PK  id            BIGINT     │    │ PK  id            BIGINT     │
│ FK  timeline_event_id VARCHAR│    │ FK  timeline_event_id VARCHAR│    │ FK  timeline_event_id VARCHAR│
│     skill_name    VARCHAR    │    │     achievement   VARCHAR    │    │     name          VARCHAR    │
└─────────────────────────────┘    │     sort_order    INT        │    │     url           VARCHAR    │
                                   └─────────────────────────────┘    │     link_type     VARCHAR    │
                                                                       └─────────────────────────────┘
```

---

## 3. 功能接口列表

### 3.1 接口概览

| 功能 | 公开/管理 | 说明 |
|------|----------|------|
| 获取时间线事件列表 | 公开 | 支持按类型筛选、按日期倒序、featured 优先 |
| 获取单个时间线事件详情 | 公开 | 返回完整事件数据，含技能、成就、链接 |
| 创建时间线事件 | 管理员 | 创建新事件，自动映射类型默认 icon/color |
| 更新时间线事件 | 管理员 | 更新指定事件，支持部分更新 |
| 删除时间线事件 | 管理员 | 删除事件及其关联数据 |
| 按类型筛选时间线事件 | 公开 | 获取指定类型的所有事件 |

---

## 4. 功能详细说明

### 4.1 获取时间线事件列表

#### 功能描述

获取所有公开的时间线事件列表。默认按 `featured DESC, start_date DESC` 排序（featured 事件优先，同 featured 状态下按日期倒序）。

#### 请求数据

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `type` | `string` | 否 | - | 按类型筛选：`education`, `work`, `project`, `achievement` |
| `featured` | `boolean` | 否 | - | 按是否精选筛选：`true`, `false` |
| `sort_by` | `string` | 否 | `start_date` | 排序字段：`start_date`, `created_at`, `title` |
| `sort_order` | `string` | 否 | `desc` | 排序方向：`asc`, `desc` |
| `limit` | `integer` | 否 | `50` | 每页数量，最大 `100` |
| `offset` | `integer` | 否 | `0` | 偏移量 |

#### 响应数据

返回时间线事件数组，每个事件包含以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `string` | 事件唯一标识 |
| `title` | `string` | 事件标题 |
| `description` | `string` | 事件详细描述 |
| `type` | `string` | 事件类型 |
| `icon` | `string` | 图标标识符 |
| `color` | `string` | HEX 颜色值 |
| `startDate` | `string` | 开始日期，格式 `YYYY-MM-DD` |
| `endDate` | `string` | 结束日期，格式 `YYYY-MM-DD`，可能为空 |
| `location` | `string` | 地点，可能为空 |
| `organization` | `string` | 机构名称，可能为空 |
| `position` | `string` | 职位/角色，可能为空 |
| `skills` | `string[]` | 技能标签数组 |
| `achievements` | `string[]` | 成就列表 |
| `links` | `object[]` | 相关链接数组，每项含 `name`, `url`, `type` |
| `featured` | `boolean` | 是否为精选事件 |
| `createdAt` | `string` | 创建时间（ISO 8601） |
| `updatedAt` | `string` | 最后更新时间（ISO 8601） |

列表响应额外包含分页元数据：

| 字段 | 类型 | 说明 |
|------|------|------|
| `total` | `integer` | 总记录数 |
| `limit` | `integer` | 每页数量 |
| `offset` | `integer` | 当前偏移量 |
| `sortBy` | `string` | 当前排序字段 |
| `sortOrder` | `string` | 当前排序方向 |

#### 业务规则

1. **默认排序**: 按 `featured DESC, start_date DESC`（featured 事件在前，同 featured 状态下最新的在前）
2. **日期格式**: 返回的 `startDate`/`endDate` 统一为 `YYYY-MM-DD` 格式字符串
3. **关联数据组装**: 从 `timeline_event_skills`、`timeline_event_achievements`、`timeline_event_links` 关联表查询并组装为数组
4. **空列表**: 无数据时返回空数组

---

### 4.2 获取单个时间线事件详情

#### 功能描述

根据 ID 获取单个时间线事件的完整详情。

#### 请求数据

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `string` | 是 | 时间线事件唯一标识 |

#### 响应数据

与列表接口中的单条事件数据结构完全一致，包含完整的 `skills`、`achievements`、`links` 关联数据。

#### 业务规则

1. 若指定 ID 的事件不存在，返回资源不存在的错误提示
2. 返回的关联数据按原排序顺序组装（achievements 按 `sort_order` 排序，links 按创建顺序）

---

### 4.3 创建时间线事件

#### 功能描述

创建新的时间线事件。需要管理员身份认证。

#### 请求数据

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | `string` | 是 | 事件标题，1-255 字符 |
| `description` | `string` | 是 | 事件描述，1-5000 字符 |
| `type` | `string` | 是 | 事件类型：`education`, `work`, `project`, `achievement` |
| `icon` | `string` | 否 | 图标标识符，1-100 字符。若为空，根据 `type` 自动填充默认值 |
| `color` | `string` | 否 | HEX 颜色值，必须匹配 `^#[0-9A-Fa-f]{6}$`。若为空，根据 `type` 自动填充默认值 |
| `startDate` | `string` | 是 | 开始日期，格式 `YYYY-MM-DD` |
| `endDate` | `string` | 否 | 结束日期，格式 `YYYY-MM-DD`。必须大于等于 `startDate` |
| `location` | `string` | 否 | 地点，最大 255 字符 |
| `organization` | `string` | 否 | 机构名称，最大 255 字符 |
| `position` | `string` | 否 | 职位/角色，最大 255 字符 |
| `skills` | `string[]` | 否 | 技能标签数组，每项 1-50 字符，最多 20 项 |
| `achievements` | `string[]` | 否 | 成就列表，每项 1-500 字符，最多 20 项 |
| `links` | `object[]` | 否 | 相关链接数组，每项含 `name`（1-100 字符）、`url`（合法 URL）、`type`（`website`/`certificate`/`project`/`other`），最多 10 项 |
| `featured` | `boolean` | 否 | 是否为精选事件，默认 `false` |

#### 类型映射逻辑（服务端兜底）

若请求中未提供 `icon` 或 `color`，或提供的值为空字符串，服务端根据 `type` 自动填充默认值：

| 类型 (`type`) | 默认图标 (`icon`) | 默认颜色 (`color`) |
|---------------|-------------------|-------------------|
| `education` | `material-symbols:school` | `#2A53DD` |
| `work` | `material-symbols:work` | `#51F56C` |
| `project` | `material-symbols:code-blocks` | `#FF4B63` |
| `achievement` | `material-symbols:emoji-events` | `#FAB83E` |

#### 响应数据

返回创建成功后的事件完整数据，结构与获取单个事件详情一致，额外包含 `createdAt` 和 `updatedAt`。

#### 业务规则

1. **类型默认映射**: 若 `icon` 或 `color` 为空字符串/缺失，服务端必须根据 `type` 自动填充默认值
2. **日期校验**: `endDate` 若提供，必须大于等于 `startDate`
3. **关联数据创建**: 同时创建 skills、achievements、links 的关联记录
4. **事务保证**: 主记录与关联记录应在同一事务中创建，失败时整体回滚

---

### 4.4 更新时间线事件

#### 功能描述

更新指定 ID 的时间线事件。需要管理员身份认证。

#### 请求数据

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `string` | 是 | 要更新的时间线事件唯一标识（路径参数） |
| `title` | `string` | 否 | 事件标题，1-255 字符 |
| `description` | `string` | 否 | 事件描述，1-5000 字符 |
| `type` | `string` | 否 | 事件类型 |
| `icon` | `string` | 否 | 图标标识符 |
| `color` | `string` | 否 | HEX 颜色值 |
| `startDate` | `string` | 否 | 开始日期，格式 `YYYY-MM-DD` |
| `endDate` | `string` | 否 | 结束日期，格式 `YYYY-MM-DD`。必须大于等于 `startDate` |
| `location` | `string` | 否 | 地点 |
| `organization` | `string` | 否 | 机构名称 |
| `position` | `string` | 否 | 职位/角色 |
| `skills` | `string[]` | 否 | 技能标签数组（全量替换） |
| `achievements` | `string[]` | 否 | 成就列表（全量替换） |
| `links` | `object[]` | 否 | 相关链接数组（全量替换） |
| `featured` | `boolean` | 否 | 是否为精选事件 |

#### 业务规则

1. **存在性检查**: 更新前必须确认事件存在，不存在返回资源不存在错误
2. **类型变更处理**: 若 `type` 变更且请求中未提供新的 `icon`/`color`，服务端需重新应用类型默认映射
3. **关联数据更新策略**: 全量替换
   - skills：先删除旧关联，再批量插入新记录
   - achievements：先删除旧记录，再按顺序批量插入新记录
   - links：先删除旧记录，再批量插入新记录
4. **部分更新**: 若某字段未提供，保持原值不变
5. **日期校验**: `endDate` 若提供，必须大于等于 `startDate`（若 `startDate` 也在本次更新中，使用新值比较）

#### 响应数据

返回更新成功后的事件完整数据，结构与获取单个事件详情一致，`updatedAt` 字段已刷新。

---

### 4.5 删除时间线事件

#### 功能描述

删除指定 ID 的时间线事件及其所有关联数据。需要管理员身份认证。

#### 请求数据

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `string` | 是 | 要删除的时间线事件唯一标识 |

#### 业务规则

1. **级联删除**: 由于外键设置了 `ON DELETE CASCADE`，删除事件时关联的 skills、achievements、links 记录自动清理
2. **幂等性**: 对同一 ID 重复删除应返回相同结果（成功或资源不存在），避免报错
3. **存在性检查**: 若事件不存在，返回资源不存在错误

#### 响应数据

成功删除后返回操作成功的提示信息。

---

### 4.6 按类型筛选时间线事件

#### 功能描述

获取指定类型的所有时间线事件列表。此功能可由"获取时间线事件列表"接口通过 `type` 查询参数实现，也可作为独立接口提供。

#### 请求数据

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | `string` | 是 | 事件类型：`education`, `work`, `project`, `achievement` |
| `limit` | `integer` | 否 | `50` | 每页数量，最大 `100` |
| `offset` | `integer` | 否 | `0` | 偏移量 |

#### 响应数据

与"获取时间线事件列表"接口的响应数据结构一致，仅返回匹配指定类型的事件。

#### 业务规则

1. 排序规则与列表接口一致：默认 `featured DESC, start_date DESC`
2. 若指定类型无事件，返回空数组

---

## 5. 业务逻辑规则

### 5.1 类型映射逻辑

| 类型 (`type`) | 默认图标 (`icon`) | 默认颜色 (`color`) |
|---------------|-------------------|-------------------|
| `education` | `material-symbols:school` | `#2A53DD` |
| `work` | `material-symbols:work` | `#51F56C` |
| `project` | `material-symbols:code-blocks` | `#FF4B63` |
| `achievement` | `material-symbols:emoji-events` | `#FAB83E` |

**规则说明**:
- 服务端在创建/更新时，若 `icon` 或 `color` 为空，必须自动填充对应类型的默认值
- 若请求中显式提供了 `icon`/`color`，则尊重用户选择（允许自定义）
- 类型变更时，若未提供新的 `icon`/`color`，服务端自动重新映射

### 5.2 Featured 逻辑

- `featured` 为布尔字段，用于标记重点事件
- 公开列表接口支持按 `featured` 筛选
- 前端对 featured 事件显示星标图标（`material-symbols:star-rounded`）
- 默认排序中 featured 事件优先展示
- 建议 featured 事件数量不做硬性限制，由前端/管理策略控制

### 5.3 日期处理逻辑

- 数据库层使用 `DATE` 类型存储，仅保留日期部分
- API 层使用 `YYYY-MM-DD` 字符串格式传输
- 默认排序按 `start_date` 倒序（最新的在前面）
- `endDate` 为可选字段，NULL 表示事件正在进行中
- 时区处理：日期本身无时区信息，按纯日期处理；`created_at`/`updated_at` 使用 UTC

### 5.4 Skills 处理逻辑

- 技能标签存储于独立关联表，支持多对多关系
- 创建/更新时采用**全量替换策略**
- 技能名称统一做 `trim()` 处理，去除首尾空格
- 重复技能名称在单事件中应去重（数据库层通过唯一索引保证）
- 空技能数组 `[]` 是合法值，表示该事件无关联技能
- 技能名称统一转换为**首字母大写**格式存储，保持展示一致性

### 5.5 Achievements 处理逻辑

- 成就列表存储于独立关联表，支持一对多关系
- 创建/更新时采用**全量替换策略**
- 成就描述统一做 `trim()` 处理
- 按数组顺序设置 `sort_order`，保持前端传入的排序
- 空成就数组 `[]` 是合法值，表示该事件无成就列表

### 5.6 Links 处理逻辑

- 相关链接存储于独立关联表，支持一对多关系
- 创建/更新时采用**全量替换策略**
- `url` 字段需校验为合法 URL 格式（http/https）
- `type` 字段必须为 `website`、`certificate`、`project`、`other` 之一
- 空链接数组 `[]` 是合法值，表示该事件无相关链接

---

## 6. 数据验证规则汇总

### 6.1 输入校验矩阵

| 字段 | 必填 | 类型 | 最小长度 | 最大长度 | 格式/枚举 |
|------|------|------|----------|----------|-----------|
| `id` | - | string | 36 | 36 | UUID v4 格式 |
| `title` | 是 | string | 1 | 255 | - |
| `description` | 是 | string | 1 | 5000 | - |
| `type` | 是 | string | - | 20 | `education`, `work`, `project`, `achievement` |
| `icon` | 否 | string | 1 | 100 | - |
| `color` | 否 | string | 7 | 7 | `^#[0-9A-Fa-f]{6}$` |
| `startDate` | 是 | string | 10 | 10 | `YYYY-MM-DD` |
| `endDate` | 否 | string | 10 | 10 | `YYYY-MM-DD`，>= startDate |
| `location` | 否 | string | 1 | 255 | - |
| `organization` | 否 | string | 1 | 255 | - |
| `position` | 否 | string | 1 | 255 | - |
| `skills` | 否 | array | 0 | 20 | 每项 string, 1-50 字符 |
| `achievements` | 否 | array | 0 | 20 | 每项 string, 1-500 字符 |
| `links` | 否 | array | 0 | 10 | 每项 object，含 name(1-100)、url(合法URL)、type(枚举) |
| `featured` | 否 | boolean | - | - | `true` / `false` |

### 6.2 自定义校验逻辑

```python
import re
from datetime import datetime

def validate_timeline_payload(data: dict, is_update: bool = False) -> list:
    """
    校验时间线事件请求数据，返回错误详情列表。
    空列表表示校验通过。
    """
    errors = []

    # title
    title = data.get("title", "").strip()
    if not is_update or "title" in data:
        if not title:
            errors.append({"field": "title", "message": "标题不能为空"})
        elif len(title) > 255:
            errors.append({"field": "title", "message": "标题长度不能超过 255 字符"})

    # description
    description = data.get("description", "").strip()
    if not is_update or "description" in data:
        if not description:
            errors.append({"field": "description", "message": "描述不能为空"})
        elif len(description) > 5000:
            errors.append({"field": "description", "message": "描述长度不能超过 5000 字符"})

    # type
    valid_types = {"education", "work", "project", "achievement"}
    timeline_type = data.get("type")
    if not is_update or "type" in data:
        if not timeline_type:
            errors.append({"field": "type", "message": "事件类型不能为空"})
        elif timeline_type not in valid_types:
            errors.append({"field": "type", "message": "无效的事件类型"})

    # color
    color = data.get("color", "").strip()
    if color and not re.match(r'^#[0-9A-Fa-f]{6}$', color):
        errors.append({"field": "color", "message": "颜色值必须是有效的 HEX 格式（如 #2A53DD）"})

    # startDate
    start_date = data.get("startDate", "").strip()
    if not is_update or "startDate" in data:
        if not start_date:
            errors.append({"field": "startDate", "message": "开始日期不能为空"})
        else:
            try:
                datetime.strptime(start_date, "%Y-%m-%d")
            except ValueError:
                errors.append({"field": "startDate", "message": "开始日期格式无效，应为 YYYY-MM-DD"})

    # endDate
    end_date = data.get("endDate", "").strip()
    if end_date:
        try:
            end_dt = datetime.strptime(end_date, "%Y-%m-%d")
            if start_date:
                start_dt = datetime.strptime(start_date, "%Y-%m-%d")
                if end_dt < start_dt:
                    errors.append({"field": "endDate", "message": "结束日期不能早于开始日期"})
        except ValueError:
            errors.append({"field": "endDate", "message": "结束日期格式无效，应为 YYYY-MM-DD"})

    # skills
    skills = data.get("skills", [])
    if skills is not None and not isinstance(skills, list):
        errors.append({"field": "skills", "message": "技能标签必须是字符串数组"})
    elif isinstance(skills, list):
        if len(skills) > 20:
            errors.append({"field": "skills", "message": "技能标签数量不能超过 20 个"})
        for skill in skills:
            if not isinstance(skill, str) or len(skill.strip()) == 0 or len(skill.strip()) > 50:
                errors.append({"field": "skills", "message": "单个技能标签长度不能超过 50 字符"})
                break

    # achievements
    achievements = data.get("achievements", [])
    if achievements is not None and not isinstance(achievements, list):
        errors.append({"field": "achievements", "message": "成就列表必须是字符串数组"})
    elif isinstance(achievements, list):
        if len(achievements) > 20:
            errors.append({"field": "achievements", "message": "成就数量不能超过 20 个"})
        for achievement in achievements:
            if not isinstance(achievement, str) or len(achievement.strip()) == 0 or len(achievement.strip()) > 500:
                errors.append({"field": "achievements", "message": "单个成就描述长度不能超过 500 字符"})
                break

    # links
    links = data.get("links", [])
    if links is not None and not isinstance(links, list):
        errors.append({"field": "links", "message": "链接列表必须是对象数组"})
    elif isinstance(links, list):
        if len(links) > 10:
            errors.append({"field": "links", "message": "链接数量不能超过 10 个"})
        url_pattern = re.compile(r'^https?://[\w\-._~:/?#[\]@!$&\'()*+,;=%.]+$')
        valid_link_types = {"website", "certificate", "project", "other"}
        for link in links:
            if not isinstance(link, dict):
                errors.append({"field": "links", "message": "链接项必须是对象"})
                break
            name = link.get("name", "").strip()
            url = link.get("url", "").strip()
            link_type = link.get("type", "").strip()
            if not name or len(name) > 100:
                errors.append({"field": "links", "message": "链接名称不能为空且不能超过 100 字符"})
                break
            if not url or not url_pattern.match(url) or len(url) > 512:
                errors.append({"field": "links", "message": "链接地址必须是有效的 http/https URL"})
                break
            if link_type not in valid_link_types:
                errors.append({"field": "links", "message": "链接类型无效"})
                break

    # featured
    if "featured" in data and not isinstance(data["featured"], bool):
        errors.append({"field": "featured", "message": "featured 字段必须为布尔值"})

    return errors
```

---

## 7. 权限控制

### 7.1 权限矩阵

| 操作 | 访客 | 管理员 |
|------|------|--------|
| 查看列表 | 允许 | 允许 |
| 查看详情 | 允许 | 允许 |
| 按类型筛选 | 允许 | 允许 |
| 创建事件 | 拒绝 | 允许 |
| 更新事件 | 拒绝 | 允许 |
| 删除事件 | 拒绝 | 允许 |

### 7.2 认证机制

- 读操作接口公开访问，无需认证
- 写操作接口（创建、更新、删除）需要管理员身份认证
- 具体认证方式由系统统一规范决定

### 7.3 安全建议

- 所有写操作接口必须校验管理员身份
- 对敏感操作记录审计日志（谁、何时、做了什么操作）
- 生产环境启用 HTTPS

---

## 8. 性能与优化

### 8.1 查询优化

- 列表查询使用分页（`LIMIT`/`OFFSET`），避免全表扫描
- 关联数据查询使用 `JOIN` 或应用层组装（防范 N+1 问题）
- 高频查询场景（如首页展示）建议加缓存，缓存时间 5-15 分钟

### 8.2 缓存策略

```python
# 伪代码：列表缓存示例
CACHE_KEY = "timeline:list:{type}:{featured}:{sort_by}:{sort_order}:{limit}:{offset}"
CACHE_TTL = 300  # 5 分钟

def get_timeline_list(params):
    cache_key = build_cache_key(params)
    cached = redis.get(cache_key)
    if cached:
        return json.loads(cached)

    result = query_database(params)
    redis.setex(cache_key, CACHE_TTL, json.dumps(result))
    return result

def invalidate_timeline_cache():
    """写操作后清除相关缓存"""
    redis.delete_pattern("timeline:list:*")
```

### 8.3 数据库连接池

- 使用连接池管理数据库连接，避免频繁创建/销毁连接
- 建议连接池大小：最小 5，最大 20（根据实际并发调整）

---

## 9. 测试要点

### 9.1 单元测试场景

| 场景 | 期望结果 |
|------|----------|
| 创建有效事件 | 数据正确入库，关联数据完整 |
| 创建时缺少必填字段 | 返回校验错误，提示具体缺失字段 |
| 创建时 `type=education` 且未提供 `icon`/`color` | 自动填充默认值 |
| 更新不存在的事件 | 返回资源不存在错误 |
| 更新事件类型并变更 `icon`/`color` | 正确更新主记录和关联记录 |
| 删除事件 | 数据及关联记录被级联清除 |
| 访客调用写操作 | 返回权限不足错误 |
| 列表按 `featured` + `start_date` 排序 | 返回正确排序结果 |
| 按 `type` 筛选 | 仅返回匹配类型的事件 |
| 按 `featured=true` 筛选 | 仅返回精选事件 |
| `endDate` 早于 `startDate` | 返回日期范围校验错误 |

### 9.2 集成测试场景

- 数据库事务回滚：创建事件失败时，关联记录不应残留
- 并发创建：同时创建多个事件，ID 不冲突，数据完整
- 大数据量分页：验证分页在数据量大时的性能
- 级联删除：删除事件后验证关联表记录是否被清理

---

## 10. 附录

### 10.1 数据库迁移脚本示例

```sql
-- V1__create_timeline_tables.sql

CREATE TABLE IF NOT EXISTS timeline_events (
    id              VARCHAR(36) PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    type            VARCHAR(20) NOT NULL,
    icon            VARCHAR(100) NOT NULL,
    color           VARCHAR(7) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,
    location        VARCHAR(255),
    organization    VARCHAR(255),
    position        VARCHAR(255),
    featured        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_type CHECK (type IN ('education', 'work', 'project', 'achievement')),
    CONSTRAINT chk_color_format CHECK (color REGEXP '^#[0-9A-Fa-f]{6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS timeline_event_skills (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    timeline_event_id VARCHAR(36) NOT NULL,
    skill_name      VARCHAR(50) NOT NULL,
    CONSTRAINT fk_timeline_event_skills_event
        FOREIGN KEY (timeline_event_id) REFERENCES timeline_events(id) ON DELETE CASCADE,
    CONSTRAINT uq_timeline_event_skill UNIQUE (timeline_event_id, skill_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS timeline_event_achievements (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    timeline_event_id VARCHAR(36) NOT NULL,
    achievement     VARCHAR(500) NOT NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_timeline_event_achievements_event
        FOREIGN KEY (timeline_event_id) REFERENCES timeline_events(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS timeline_event_links (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    timeline_event_id VARCHAR(36) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    url             VARCHAR(512) NOT NULL,
    link_type       VARCHAR(20) NOT NULL,
    CONSTRAINT fk_timeline_event_links_event
        FOREIGN KEY (timeline_event_id) REFERENCES timeline_events(id) ON DELETE CASCADE,
    CONSTRAINT chk_link_type CHECK (link_type IN ('website', 'certificate', 'project', 'other'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_timeline_start_date ON timeline_events(start_date DESC);
CREATE INDEX idx_timeline_type ON timeline_events(type);
CREATE INDEX idx_timeline_featured ON timeline_events(featured);
CREATE INDEX idx_timeline_type_start_date ON timeline_events(type, start_date DESC);
CREATE INDEX idx_timeline_featured_start_date ON timeline_events(featured DESC, start_date DESC);
CREATE INDEX idx_skills_name ON timeline_event_skills(skill_name);
```

### 10.2 前端兼容数据结构

后端 API 必须返回以下格式，以确保前端 `TimelineItem` 接口无需修改即可工作：

```typescript
interface TimelineLink {
  name: string;
  url: string;
  type: "website" | "certificate" | "project" | "other";
}

interface TimelineItem {
  id: string;
  title: string;
  description: string;
  type: "education" | "work" | "project" | "achievement";
  startDate: string;        // YYYY-MM-DD
  endDate?: string;         // YYYY-MM-DD
  location?: string;
  organization?: string;
  position?: string;
  skills?: string[];
  achievements?: string[];
  links?: TimelineLink[];
  icon?: string;
  color?: string;
  featured?: boolean;
}
```

### 10.3 迁移注意事项

从静态 TS 文件迁移到后端 API 时：

1. **数据导入**：编写一次性脚本将 `timeline.ts` 中的数据导入数据库
   - 注意 `id` 为自定义字符串，需保留原值或转换为 UUID
   - `skills`、`achievements`、`links` 需拆分到关联表
2. **前端适配**：
   - 开发环境：前端可直接请求本地后端 API
   - 生产环境：Astro 构建时可通过 `getStaticPaths` 或 SSR 模式调用 API
3. **回滚方案**：保留原始 `timeline.ts` 作为备份，直至后端稳定运行

### 10.4 相关文档

- [module-timeline.md](./module-timeline.md) - Timeline 模块前端结构与数据流描述
