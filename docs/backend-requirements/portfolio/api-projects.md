# Projects（项目展示）模块后端 API 需求文档

> **模块名称**: Projects
> **文档类型**: 后端 API 需求文档
> **目标读者**: 后端开发工程师、数据库工程师、API 测试工程师
> **文档版本**: v2.0
> **最后更新**: 2026-05-04

---

## 1. 概述

本文档为 Mizuki 博客系统 Projects（项目展示）模块的后端开发提供详细需求说明，涵盖数据库设计、API 功能定义、业务逻辑规则、数据校验等方面。

### 1.1 设计原则

- 前后端契约：严格对齐前端 `Project` 数据结构
- 安全性：写操作强制管理员身份认证
- 可扩展性：数据库设计预留未来扩展字段

---

## 2. 数据库表设计

### 2.1 主表: `projects`

存储项目展示模块的核心信息。

```sql
CREATE TABLE projects (
    id              VARCHAR(64) PRIMARY KEY,        -- 自定义字符串 ID，全局唯一
    title           VARCHAR(255) NOT NULL,          -- 项目标题
    description     TEXT NOT NULL,                  -- 项目描述
    image           VARCHAR(512) NOT NULL DEFAULT '', -- 封面图片路径
    category        VARCHAR(20) NOT NULL,           -- 项目类别枚举值
    status          VARCHAR(20) NOT NULL,           -- 项目状态枚举值
    live_demo_url   VARCHAR(512),                   -- 在线演示地址
    source_code_url VARCHAR(512),                   -- 源码仓库地址
    visit_url       VARCHAR(512),                   -- 项目主页地址
    start_date      DATE NOT NULL,                  -- 开始日期
    end_date        DATE,                           -- 结束日期
    featured        BOOLEAN NOT NULL DEFAULT FALSE, -- 是否置顶/精选
    show_image      BOOLEAN NOT NULL DEFAULT TRUE,  -- 是否显示封面图
    sort_order      INT NOT NULL DEFAULT 0,         -- 排序权重，越小越靠前

    -- 审计字段
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 约束
    CONSTRAINT chk_category CHECK (category IN ('web', 'mobile', 'desktop', 'other')),
    CONSTRAINT chk_status CHECK (status IN ('completed', 'in-progress', 'planned'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 字段说明

| 字段 | 类型 | 可空 | 默认值 | 说明 |
|------|------|------|--------|------|
| `id` | `VARCHAR(64)` | 否 | - | 自定义字符串 ID（如 `mizuki`, `folkpatch`），由应用层生成 |
| `title` | `VARCHAR(255)` | 否 | - | 项目标题，最大 255 字符 |
| `description` | `TEXT` | 否 | - | 项目描述，建议限制 2000 字符 |
| `image` | `VARCHAR(512)` | 否 | `''` | 封面图片路径，空字符串表示无图 |
| `category` | `VARCHAR(20)` | 否 | - | 枚举：`web`, `mobile`, `desktop`, `other` |
| `status` | `VARCHAR(20)` | 否 | - | 枚举：`completed`, `in-progress`, `planned` |
| `live_demo_url` | `VARCHAR(512)` | 是 | `NULL` | 在线演示 URL |
| `source_code_url` | `VARCHAR(512)` | 是 | `NULL` | 源码仓库 URL |
| `visit_url` | `VARCHAR(512)` | 是 | `NULL` | 项目主页 URL |
| `start_date` | `DATE` | 否 | - | 项目开始日期，格式 `YYYY-MM-DD` |
| `end_date` | `DATE` | 是 | `NULL` | 项目结束日期，格式 `YYYY-MM-DD` |
| `featured` | `BOOLEAN` | 否 | `FALSE` | 是否为置顶/精选项目 |
| `show_image` | `BOOLEAN` | 否 | `TRUE` | 是否在前端显示封面图区域 |
| `sort_order` | `INT` | 否 | `0` | 排序权重，用于自定义排序 |
| `created_at` | `TIMESTAMP` | 否 | `CURRENT_TIMESTAMP` | 创建时间 |
| `updated_at` | `TIMESTAMP` | 否 | `CURRENT_TIMESTAMP` | 最后更新时间 |

### 2.2 关联表: `project_tech_stacks`

存储项目与技术栈的关联关系（多对多）。

```sql
CREATE TABLE project_tech_stacks (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL,           -- 关联的项目 ID
    tech_name       VARCHAR(50) NOT NULL,           -- 技术名称

    -- 约束
    CONSTRAINT fk_project_tech_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_tech UNIQUE (project_id, tech_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 设计说明

- 采用独立关联表而非 JSON 数组，便于后续技术栈统计、筛选及全文检索
- `tech_name` 使用 `VARCHAR(50)`，限制单个技术名称长度
- 设置联合唯一索引 `(project_id, tech_name)` 防止重复技术标签
- 外键级联删除：删除项目时自动清理关联技术栈记录

### 2.3 关联表: `project_tags`

存储项目与额外标签的关联关系（多对多）。

```sql
CREATE TABLE project_tags (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL,           -- 关联的项目 ID
    tag_name        VARCHAR(50) NOT NULL,           -- 标签名称

    -- 约束
    CONSTRAINT fk_project_tags_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_tag UNIQUE (project_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.4 索引设计

```sql
-- 按排序权重 + 创建时间查询（最常用）
CREATE INDEX idx_projects_sort ON projects(sort_order ASC, created_at DESC);

-- 按类别筛选
CREATE INDEX idx_projects_category ON projects(category);

-- 按状态筛选
CREATE INDEX idx_projects_status ON projects(status);

-- 置顶项目查询
CREATE INDEX idx_projects_featured ON projects(featured);

-- 复合索引：类别 + 排序（用于筛选后排序）
CREATE INDEX idx_projects_category_sort ON projects(category, sort_order ASC);

-- 复合索引：置顶 + 排序（用于首页精选查询）
CREATE INDEX idx_projects_featured_sort ON projects(featured, sort_order ASC);

-- 技术名称索引（用于技术栈筛选/统计）
CREATE INDEX idx_tech_name ON project_tech_stacks(tech_name);

-- 标签名称索引（用于标签筛选/统计）
CREATE INDEX idx_tag_name ON project_tags(tag_name);
```

### 2.5 ER 图

```
+-------------------------+         +-----------------------------+
|        projects         |         |    project_tech_stacks      |
+-------------------------+         +-----------------------------+
| PK  id          VARCHAR |<-------| FK project_id     VARCHAR   |
|     title       VARCHAR |         | PK  id             BIGINT    |
|     description TEXT    |         |     tech_name      VARCHAR   |
|     image       VARCHAR |         +-----------------------------+
|     category    VARCHAR |
|     status      VARCHAR |         +-----------------------------+
|     live_demo_url   VARCHAR|      |      project_tags           |
|     source_code_url VARCHAR|      +-----------------------------+
|     visit_url   VARCHAR |<-------| FK project_id     VARCHAR   |
|     start_date  DATE    |         | PK  id             BIGINT    |
|     end_date    DATE    |         |     tag_name       VARCHAR   |
|     featured    BOOLEAN |         +-----------------------------+
|     show_image  BOOLEAN |
|     sort_order  INT     |
|     created_at  TIMESTAMP
|     updated_at  TIMESTAMP
+-------------------------+
```

---

## 3. API 功能列表

### 3.1 接口概览

| 功能 | 访问权限 |
|------|----------|
| 获取项目列表（支持分类筛选、状态筛选、featured 优先排序） | 公开 |
| 获取单个项目详情 | 公开 |
| 创建项目 | 管理员 |
| 更新项目 | 管理员 |
| 删除项目 | 管理员 |
| 获取所有技术栈列表（去重） | 公开 |
| 获取所有项目分类 | 公开 |
| 获取项目统计数据 | 公开 |
| 上传项目封面图 | 管理员 |

---

### 3.2 获取项目列表

**功能描述**：获取所有公开的项目列表，默认按 `sort_order` 升序、`created_at` 倒序排列，featured 项目优先。

**请求参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `category` | `string` | 否 | - | 按类别筛选：`web`, `mobile`, `desktop`, `other` |
| `status` | `string` | 否 | - | 按状态筛选：`completed`, `in-progress`, `planned` |
| `featured` | `boolean` | 否 | - | 按是否置顶筛选：`true`, `false` |
| `tech_stack` | `string` | 否 | - | 按技术栈筛选（精确匹配技术名称） |
| `sort_by` | `string` | 否 | `sort_order` | 排序字段：`sort_order`, `start_date`, `created_at`, `title` |
| `sort_order` | `string` | 否 | `asc` | 排序方向：`asc`, `desc` |
| `limit` | `integer` | 否 | `50` | 每页数量，最大 `100` |
| `offset` | `integer` | 否 | `0` | 偏移量 |

**响应数据**：
- 项目对象数组，每个项目包含完整字段（id, title, description, image, category, techStack, status, liveDemo, sourceCode, visitUrl, startDate, endDate, featured, tags, showImage, createdAt, updatedAt）
- 分页元数据（total, limit, offset, sortBy, sortOrder）

**业务规则**：
1. 默认排序：按 `featured DESC, sort_order ASC, created_at DESC`
2. 日期格式：返回的 `startDate`/`endDate` 统一为 `YYYY-MM-DD` 格式字符串
3. 技术栈组装：从 `project_tech_stacks` 关联表查询并组装为 `techStack` 字符串数组
4. 标签组装：从 `project_tags` 关联表查询并组装为 `tags` 字符串数组
5. 空列表：无数据时返回空数组
6. URL 字段处理：数据库中 `NULL` 的 URL 字段返回空字符串 `""`，保持前端兼容性

---

### 3.3 获取单个项目详情

**功能描述**：根据 ID 获取单个项目的完整详情。

**请求参数**：
- `id`：项目自定义字符串 ID

**响应数据**：
- 项目完整字段（同列表接口中的单条数据格式）

**业务规则**：
- 若项目不存在，返回错误提示

---

### 3.4 创建项目

**功能描述**：创建新的项目记录。需要管理员认证。

**请求数据**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `id` | `string` | 是 | 自定义字符串 ID，全局唯一 |
| `title` | `string` | 是 | 项目标题，1-255 字符 |
| `description` | `string` | 是 | 项目描述，1-2000 字符 |
| `image` | `string` | 是 | 封面图片路径，最大 512 字符，可为空字符串 |
| `category` | `string` | 是 | 项目类别：`web`, `mobile`, `desktop`, `other` |
| `techStack` | `string[]` | 是 | 技术栈数组，每项 1-50 字符，最多 20 项 |
| `status` | `string` | 是 | 项目状态：`completed`, `in-progress`, `planned` |
| `liveDemo` | `string` | 否 | 在线演示 URL，必须是合法 URL |
| `sourceCode` | `string` | 否 | 源码仓库 URL，必须是合法 URL |
| `visitUrl` | `string` | 否 | 项目主页 URL，必须是合法 URL |
| `startDate` | `string` | 是 | 开始日期，格式 `YYYY-MM-DD` |
| `endDate` | `string` | 否 | 结束日期，格式 `YYYY-MM-DD`，必须 >= startDate |
| `featured` | `boolean` | 是 | 是否置顶 |
| `tags` | `string[]` | 否 | 标签数组，每项 1-50 字符，最多 20 项 |
| `showImage` | `boolean` | 否 | 是否显示封面图，默认 `true` |

**校验规则**：
- `id` 必填，1-64 字符，只允许字母、数字、连字符、下划线，全局唯一
- `title` 必填，1-255 字符
- `description` 必填，1-2000 字符
- `category` 必填，必须为有效枚举值
- `techStack` 必填，必须为字符串数组，每项 1-50 字符，最多 20 项
- `status` 必填，必须为有效枚举值
- `startDate` 必填，必须为有效日期格式 `YYYY-MM-DD`
- `endDate` 可选，若提供必须 >= `startDate`
- URL 字段若提供必须是合法 URL，最大 512 字符

**响应数据**：
- 创建成功的项目完整对象（含 createdAt, updatedAt）

---

### 3.5 更新项目

**功能描述**：更新指定 ID 的项目信息。需要管理员认证。

**请求参数**：
- `id`：项目自定义字符串 ID（路径参数）

**请求数据**：
- 可更新字段：title, description, image, category, techStack, status, liveDemo, sourceCode, visitUrl, startDate, endDate, featured, tags, showImage
- 所有字段均为可选（部分更新），但建议前端提交完整对象
- `id` 不可变更

**校验规则**：
- 同创建项目的字段校验规则
- 更新前需确认项目存在

**业务规则**：
1. 存在性检查：更新前必须确认项目存在
2. ID 不可变更：路径参数中的 ID 即为主键
3. 技术栈更新策略：全量替换——删除旧关联，插入新关联
4. 标签更新策略：全量替换——与技术栈相同
5. 部分更新：若某字段未提供，保持原值不变

**响应数据**：
- 更新后的项目完整对象（含 updatedAt）

---

### 3.6 删除项目

**功能描述**：删除指定 ID 的项目及其关联技术栈、标签记录。需要管理员认证。

**请求参数**：
- `id`：项目自定义字符串 ID

**业务规则**：
1. 级联删除：由于外键设置了 `ON DELETE CASCADE`，删除项目时关联技术栈和标签记录自动清理
2. 图片清理：删除项目后，检查该项目的 `image` 路径对应的物理文件是否被其他项目引用，若无引用则删除物理文件（防止孤儿文件）
3. 幂等性：对同一 ID 重复删除应返回相同结果，避免报错
4. 存在性检查：若项目不存在，返回错误提示

---

### 3.7 获取所有技术栈列表

**功能描述**：获取所有项目中使用的不重复技术栈列表，按字母排序。

**响应数据**：
- 去重后的技术栈名称字符串数组，按字母升序排列

**业务规则**：
- 从技术栈关联表中提取所有不重复的 `tech_name`
- 统一做 `trim()` 处理
- 按字母顺序排序后返回

---

### 3.8 获取所有项目分类

**功能描述**：获取所有有效的项目分类枚举值及统计信息。

**响应数据**：
- 分类数组，每项包含：
  - `value`：分类值（`web`, `mobile`, `desktop`, `other`）
  - `label`：分类显示名称
  - `count`：该分类下的项目数量

---

### 3.9 获取项目统计数据

**功能描述**：获取项目统计信息，用于前端统计面板展示。

**响应数据**：
- `total`：项目总数
- `byStatus`：各状态项目数量（completed, inProgress, planned）
- `byCategory`：各分类项目数量（web, mobile, desktop, other）
- `featuredCount`：置顶项目数量

---

### 3.10 上传项目封面图

**功能描述**：上传项目封面图片。需要管理员认证。

**请求格式**：`multipart/form-data`

**请求字段**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `file` | `File` | 是 | 图片文件 |
| `project_id` | `string` | 否 | 项目 ID，用于生成文件名 |

**文件验证规则**：

| 规则 | 限制 |
|------|------|
| 文件类型 | `image/jpeg`, `image/png`, `image/webp`, `image/gif` |
| 文件大小 | <= 5MB |
| 文件名长度 | <= 255 字符 |

**存储逻辑**：
1. 生成存储路径：`{storage_root}/assets/projects/`
2. 文件名生成策略：
   - 若提供 `project_id`，使用 `{project_id}.webp`
   - 若未提供，使用原始文件名（去除非法字符，转换为小写）
3. 重名处理：若文件已存在，自动覆盖
4. 格式转换：建议将上传的非 WebP 格式自动转换为 WebP，以减小体积
5. 返回可访问的 URL 路径

**响应数据**：
- `original_name`：原始文件名
- `stored_name`：存储文件名
- `url`：可访问的 URL 路径
- `size`：文件大小（字节）
- `mime_type`：文件 MIME 类型

---

## 4. 业务逻辑规则

### 4.1 Featured 置顶逻辑

#### 4.1.1 置顶规则

- `featured` 为布尔字段，用于标记置顶/精选项目
- 置顶项目在前端展示时显示星标图标
- 置顶项目可被 FeaturedProjects 组件单独展示
- 公开列表接口支持 `featured=true` 筛选置顶项目

#### 4.1.2 置顶数量限制（建议）

- 数据库层不做硬性数量限制
- 应用层建议：置顶项目数量建议控制在 2-6 个，避免首页展示过多
- 管理策略：当管理员将新项目设为置顶且已有 N 个置顶项目时，可提示"置顶项目数量已达建议上限"

#### 4.1.3 置顶排序

- 置顶项目在列表中优先展示（通过 `sort_order` 控制）
- 建议置顶项目的 `sort_order` 设置为较小的值（如 0, 1, 2...）
- 非置顶项目 `sort_order` 建议从 10 开始，避免与置顶项目冲突

### 4.2 状态流转规则

#### 4.2.1 状态定义

| 状态 | 含义 | 前端展示 |
|------|------|----------|
| `planned` | 已计划 | "已计划"（灰色/次要样式） |
| `in-progress` | 进行中 | "进行中"（主色/高亮样式） |
| `completed` | 已完成 | "已完成"（成功/绿色样式） |

#### 4.2.2 允许的状态流转

```
planned --> in-progress --> completed
   ^            |              |
   |            |              |
   +------------+--------------+
   （允许反向流转：项目可重新计划或重新开始）
```

- 允许任意流转：项目状态可在 `planned` / `in-progress` / `completed` 之间自由切换
- 自动日期填充建议：状态变为 `completed` 时，若 `endDate` 为空，建议自动填充当前日期
- 状态从 `completed` 变为其他状态时，不清除 `endDate`

#### 4.2.3 状态与日期一致性校验

- 若状态为 `completed` 但无 `endDate`，可给出警告（非强制错误）

### 4.3 日期处理逻辑

- 数据库层使用 `DATE` 类型存储，仅保留日期部分
- API 层使用 `YYYY-MM-DD` 字符串格式传输
- `startDate` 必填，`endDate` 可选
- 日期范围校验：`endDate` 若提供，必须大于等于 `startDate`
- 时区处理：日期本身无时区信息，按纯日期处理

### 4.4 Tech Stack / Tags 处理逻辑

- 技术栈和标签分别存储于独立关联表，支持多对多关系
- 创建/更新时采用全量替换策略
- 技术名称/标签名称统一做 `trim()` 处理，去除首尾空格
- 重复名称在单项目中应去重（数据库层通过唯一索引保证）
- 空数组 `[]` 是合法值，表示该项目无关联技术栈或标签
- 技术栈和标签名称统一转换为首字母大写格式存储，保持展示一致性

### 4.5 ID 生成策略

- 项目 ID 使用自定义字符串，非自增数字或 UUID
- 建议格式：小写字母、数字、连字符、下划线（如 `mizuki`, `folk-patch`, `folk_tool_v2`）
- 创建时由管理员在表单中手动输入
- 服务端校验唯一性，若已存在返回校验错误
- 不可变更：项目创建后 `id` 不允许修改

---

## 5. 数据验证规则汇总

### 5.1 输入校验矩阵

| 字段 | 必填 | 类型 | 最小长度 | 最大长度 | 格式/枚举 |
|------|------|------|----------|----------|-----------|
| `id` | 是 | string | 1 | 64 | `^[a-zA-Z0-9_-]+$` |
| `title` | 是 | string | 1 | 255 | - |
| `description` | 是 | string | 1 | 2000 | - |
| `image` | 是 | string | 0 | 512 | - |
| `category` | 是 | string | - | 20 | `web`, `mobile`, `desktop`, `other` |
| `techStack` | 是 | array | 0 | 20 | 每项 string, 1-50 字符 |
| `status` | 是 | string | - | 20 | `completed`, `in-progress`, `planned` |
| `liveDemo` | 否 | string | 1 | 512 | 合法 URL |
| `sourceCode` | 否 | string | 1 | 512 | 合法 URL |
| `visitUrl` | 否 | string | 1 | 512 | 合法 URL |
| `startDate` | 是 | string | 10 | 10 | `YYYY-MM-DD` |
| `endDate` | 否 | string | 10 | 10 | `YYYY-MM-DD`，>= startDate |
| `featured` | 是 | boolean | - | - | `true` / `false` |
| `tags` | 否 | array | 0 | 20 | 每项 string, 1-50 字符 |
| `showImage` | 否 | boolean | - | - | `true` / `false` |

---

## 6. 权限控制

### 6.1 权限矩阵

| 操作 | 访客 | 管理员 |
|------|------|--------|
| 查看项目列表 | 允许 | 允许 |
| 查看单个项目详情 | 允许 | 允许 |
| 查看项目统计 | 允许 | 允许 |
| 查看技术栈列表 | 允许 | 允许 |
| 查看项目分类 | 允许 | 允许 |
| 创建项目 | 拒绝 | 允许 |
| 更新项目 | 拒绝 | 允许 |
| 删除项目 | 拒绝 | 允许 |
| 上传封面图 | 拒绝 | 允许 |

### 6.2 认证机制

- 写操作接口需要管理员身份认证
- 读操作接口公开访问，无需认证
- 建议对敏感操作记录审计日志（谁、何时、做了什么操作）

### 6.3 安全建议

- 所有写操作接口必须进行管理员鉴权
- 上传接口严格校验文件类型和大小，防止恶意文件上传
- 图片存储路径做安全校验，防止目录遍历攻击

---

## 7. 图片存储方案

### 7.1 存储架构

```
+-----------------+     +-----------------+     +-----------------+
|   客户端上传     |---->|   后端服务       |---->|   本地文件系统   |
|   (multipart)   |     |   (接收/验证/转换)|     |   /public/assets/|
+-----------------+     +-----------------+     +-----------------+
                               |
                               v
                        +-----------------+
                        |   可选：对象存储  |
                        |   (S3/OSS/CDN)  |
                        +-----------------+
```

### 7.2 本地存储方案（默认）

**目录结构：**

```
public/
└── assets/
    └── projects/
        ├── mizuki.webp
        ├── folkpatch.webp
        ├── new-project.webp
        └── ...
```

**文件名规范：**

- 文件名与项目 ID 保持一致（如项目 ID 为 `mizuki`，则文件名为 `mizuki.webp`）
- 仅允许：小写字母、数字、连字符、下划线
- 空格替换为连字符
- 连续特殊字符合并
- 统一使用 `.webp` 扩展名

### 7.3 图片处理流程

1. 接收：接收 multipart 文件上传
2. 验证：检查文件类型（JPG/PNG/WebP/GIF）、大小（<= 5MB）
3. 处理：
   - 压缩图片至最大尺寸（如 1920x1080，保持比例）
   - 转换为 WebP 格式以减小体积（质量建议 85-90）
   - 生成缩略图（可选）
4. 存储：保存到 `public/assets/projects/` 目录
5. 记录：返回图片 URL 供前端使用

### 7.4 图片清理策略

- 即时清理：删除项目时，检查该项目的 `image` 路径对应的物理文件是否被其他项目引用，若无引用则删除物理文件
- 定时清理：可配置定时任务清理 `assets/projects/` 下未被引用的孤儿图片
- 安全删除：删除前确认文件路径在允许目录内，防止目录遍历攻击
- 覆盖策略：同一项目上传新封面图时，直接覆盖旧文件

---

## 8. 性能与优化

### 8.1 查询优化

- 列表查询使用分页（limit/offset），避免全表扫描
- 关联技术栈/标签查询使用 JOIN 或应用层组装（防范 N+1 问题）
- 高频查询场景（如首页展示）建议加缓存，缓存时间 5-15 分钟
- 统计接口建议缓存 1-5 分钟

### 8.2 缓存策略

- 列表查询可按参数组合缓存
- 写操作后清除相关缓存（列表缓存、统计缓存、技术栈缓存）
- 缓存时间建议 5 分钟

### 8.3 图片优化

- 限制上传图片最大尺寸（5MB）
- 上传后自动压缩/转换为 WebP
- 响应头添加图片缓存策略

---

## 9. 测试要点

### 9.1 单元测试场景

| 场景 | 期望结果 |
|------|----------|
| 创建有效项目 | 数据正确入库，技术栈/标签关联正确 |
| 创建时缺少必填字段 | 提示具体缺失字段 |
| 创建时 ID 已存在 | 提示 ID 冲突 |
| 创建时 endDate 早于 startDate | 提示日期范围错误 |
| 更新不存在项目 | 提示项目不存在 |
| 更新项目技术栈 | 旧技术栈被完全替换 |
| 删除项目 | 数据及关联记录被清除，图片文件被清理 |
| 访客调用写操作 | 拒绝访问 |
| 列表按 sort_order 排序 | 返回正确排序结果 |
| 按 category 筛选 | 仅返回匹配类别的项目 |
| 按 featured=true 筛选 | 仅返回置顶项目 |

### 9.2 集成测试场景

- 数据库事务回滚：创建项目失败时，技术栈/标签关联记录不应残留
- 并发创建：同时创建多个项目，ID 不冲突，数据完整
- 大数据量分页：验证 limit/offset 在数据量大时的性能
- 图片上传：验证格式转换、压缩、存储路径正确
- 图片清理：删除项目后验证物理文件是否被正确清理

---

## 10. 附录

### 10.1 数据库迁移脚本示例

```sql
-- V1__create_projects_tables.sql

CREATE TABLE IF NOT EXISTS projects (
    id              VARCHAR(64) PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT NOT NULL,
    image           VARCHAR(512) NOT NULL DEFAULT '',
    category        VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    live_demo_url   VARCHAR(512),
    source_code_url VARCHAR(512),
    visit_url       VARCHAR(512),
    start_date      DATE NOT NULL,
    end_date        DATE,
    featured        BOOLEAN NOT NULL DEFAULT FALSE,
    show_image      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_category CHECK (category IN ('web', 'mobile', 'desktop', 'other')),
    CONSTRAINT chk_status CHECK (status IN ('completed', 'in-progress', 'planned'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS project_tech_stacks (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL,
    tech_name       VARCHAR(50) NOT NULL,
    CONSTRAINT fk_project_tech_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_tech UNIQUE (project_id, tech_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS project_tags (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    project_id      VARCHAR(64) NOT NULL,
    tag_name        VARCHAR(50) NOT NULL,
    CONSTRAINT fk_project_tags_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_project_tag UNIQUE (project_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_projects_sort ON projects(sort_order ASC, created_at DESC);
CREATE INDEX idx_projects_category ON projects(category);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_featured ON projects(featured);
CREATE INDEX idx_projects_category_sort ON projects(category, sort_order ASC);
CREATE INDEX idx_projects_featured_sort ON projects(featured, sort_order ASC);
CREATE INDEX idx_tech_name ON project_tech_stacks(tech_name);
CREATE INDEX idx_tag_name ON project_tags(tag_name);
```

### 10.2 前端兼容数据结构

后端 API 必须返回以下格式，以确保前端 `Project` 接口无需修改即可工作：

```typescript
interface Project {
  id: string;
  title: string;
  description: string;
  image: string;
  category: "web" | "mobile" | "desktop" | "other";
  techStack: string[];
  status: "completed" | "in-progress" | "planned";
  liveDemo?: string;
  sourceCode?: string;
  visitUrl?: string;
  startDate: string;        // YYYY-MM-DD
  endDate?: string;         // YYYY-MM-DD
  featured?: boolean;
  tags?: string[];
  showImage?: boolean;
}
```

### 10.3 迁移注意事项

从静态 TS 文件迁移到后端 API 时：

1. **数据导入**：编写一次性脚本将 `projects.ts` 中的数据导入数据库
   - 注意 `id` 为自定义字符串，需保留原值
   - `techStack` 和 `tags` 需拆分到关联表
2. **图片路径**：确保现有 `public/assets/projects/` 下的图片在新系统中可访问
3. **前端适配**：
   - 开发环境：前端可直接请求本地后端 API
   - 生产环境：Astro 构建时可通过 `getStaticPaths` 或 SSR 模式调用 API
4. **回滚方案**：保留原始 `projects.ts` 作为备份，直至后端稳定运行

### 10.4 相关文档

- [module-projects.md](./module-projects.md) - Projects 模块前端结构与数据流描述
