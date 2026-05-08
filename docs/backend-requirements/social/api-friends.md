# Friends（友情链接）模块后端功能需求文档

> **模块名称**: Friends
> **文档版本**: v2.0
> **最后更新**: 2026-05-04
> **关联文档**: [module-friends.md](./module-friends.md)

---

## 1. 概述

本文档为 Mizuki 博客项目 Friends 模块的后端开发提供需求说明，涵盖数据库设计、功能接口、业务逻辑规则、数据验证及图片存储方案。

### 1.1 设计目标

- 提供完整的友链 CRUD 能力
- 保持与现有前端 `FriendItem` 数据结构的兼容性
- 支持标签管理与多对多关联
- 支持外链图片和本地上传图片两种存储方式
- 提供随机排序和标签筛选能力

---

## 2. 数据库表设计

### 2.1 友链主表（`friends`）

```sql
CREATE TABLE friends (
    id                  BIGINT UNSIGNED     AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(100)        NOT NULL COMMENT '友链网站标题/名称',
    description         VARCHAR(255)        NOT NULL COMMENT '友链网站描述',
    siteurl             VARCHAR(500)        NOT NULL COMMENT '友链网站链接（完整 URL）',
    imgurl              VARCHAR(500)        NOT NULL COMMENT '头像/Logo 图片访问 URL',
    img_type            TINYINT             NOT NULL DEFAULT 0 COMMENT '图片类型：0=外链，1=本地上传',
    img_storage_path    VARCHAR(500)        DEFAULT NULL COMMENT '本地上传图片的存储路径',
    sort_order          INT UNSIGNED        NOT NULL DEFAULT 0 COMMENT '排序顺序，数值越小越靠前',
    is_active           BOOLEAN             NOT NULL DEFAULT TRUE COMMENT '是否启用/显示',
    created_at          DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    is_deleted          BOOLEAN             NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    deleted_at          DATETIME(3)         DEFAULT NULL COMMENT '删除时间',

    -- 索引
    UNIQUE INDEX idx_siteurl (siteurl(255)),
    INDEX idx_sort_order (sort_order ASC),
    INDEX idx_active_sort (is_deleted, is_active, sort_order ASC),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.2 标签表（`friend_tags`）

```sql
CREATE TABLE friend_tags (
    id          BIGINT UNSIGNED     AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)         NOT NULL COMMENT '标签名称',
    created_at  DATETIME(3)         NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    UNIQUE INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.3 友链-标签关联表（`friend_tag_links`）

```sql
CREATE TABLE friend_tag_links (
    friend_id   BIGINT UNSIGNED     NOT NULL COMMENT '外键，关联 friends.id',
    tag_id      BIGINT UNSIGNED     NOT NULL COMMENT '外键，关联 friend_tags.id',

    PRIMARY KEY (friend_id, tag_id),
    FOREIGN KEY (friend_id) REFERENCES friends(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES friend_tags(id) ON DELETE CASCADE,
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.4 ER 关系图

```
+-----------------+       +--------------------+       +---------------+
|     friends     |       | friend_tag_links   |       |  friend_tags  |
+-----------------+       +--------------------+       +---------------+
| PK id           |◄──────| FK friend_id       |       | PK id         |
|    title        |  1:N  | FK tag_id          ├──────►|    name       |
|    description  |       +--------------------+  N:1  +---------------+
|    siteurl      |              N:M
|    imgurl       |
|    img_type     |
|    sort_order   |
|    is_active    |
|    created_at   |
|    updated_at   |
|    is_deleted   |
+-----------------+
```

---

## 3. 功能接口

### 3.1 接口概览

| 功能 | 说明 | 权限 |
|------|------|------|
| 获取友链列表 | 获取所有友链，支持随机排序、按标签筛选 | 公开 |
| 获取单条友链详情 | 根据 ID 获取友链完整信息 | 公开 |
| 创建友链 | 新增一条友情链接 | 管理员 |
| 更新友链 | 修改已有友链信息 | 管理员 |
| 删除友链 | 删除指定友链 | 管理员 |
| 获取所有标签 | 获取全部标签及其关联友链数量 | 公开 |
| 上传友链头像 | 本地上传友链头像图片 | 管理员 |
| 删除友链头像 | 删除本地上传的友链头像 | 管理员 |

### 3.2 获取友链列表

**功能说明**：获取友链列表，支持按标签筛选和随机排序。

**查询参数**：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `tag` | `string` | 否 | - | 按标签名称筛选 |
| `shuffle` | `boolean` | 否 | `false` | 是否随机打乱顺序 |
| `active_only` | `boolean` | 否 | `true` | 仅返回启用的友链 |

**响应数据**：

```json
{
  "items": [
    {
      "id": 1,
      "title": "Astro",
      "description": "The web framework for content-driven websites",
      "siteurl": "https://github.com/withastro/astro",
      "imgurl": "https://avatars.githubusercontent.com/u/44914786?v=4&s=640",
      "tags": ["Framework"],
      "sort_order": 0,
      "is_active": true,
      "created_at": "2025-01-15T10:30:00.000Z",
      "updated_at": "2025-01-15T10:30:00.000Z"
    }
  ],
  "total": 8
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `items` | `array` | 友链条目数组 |
| `items[].id` | `integer` | 友链 ID |
| `items[].title` | `string` | 友链标题 |
| `items[].description` | `string` | 友链描述 |
| `items[].siteurl` | `string` | 网站链接（完整 URL） |
| `items[].imgurl` | `string` | 图片访问 URL |
| `items[].tags` | `string[]` | 标签数组 |
| `items[].sort_order` | `integer` | 排序顺序 |
| `items[].is_active` | `boolean` | 是否启用 |
| `items[].created_at` | `string` | 创建时间 |
| `items[].updated_at` | `string` | 更新时间 |
| `total` | `integer` | 未删除的友链总数 |

**业务规则**：
- 默认按 `sort_order` 升序排列，相同 `sort_order` 按 `created_at` 降序
- `shuffle=true` 时在查询结果基础上随机打乱
- `active_only=true` 时只返回 `is_active = true` 且 `is_deleted = false` 的记录
- `tag` 参数存在时，只返回包含该标签的友链

### 3.3 获取单条友链详情

**功能说明**：根据友链 ID 获取完整信息。

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `integer` | 友链 ID |

**响应数据**：

```json
{
  "id": 1,
  "title": "Astro",
  "description": "The web framework for content-driven websites",
  "siteurl": "https://github.com/withastro/astro",
  "imgurl": "https://avatars.githubusercontent.com/u/44914786?v=4&s=640",
  "img_type": 0,
  "img_storage_path": null,
  "tags": ["Framework"],
  "sort_order": 0,
  "is_active": true,
  "created_at": "2025-01-15T10:30:00.000Z",
  "updated_at": "2025-01-15T10:30:00.000Z"
}
```

### 3.4 创建友链

**功能说明**：新增一条友情链接。

**请求体**：

```json
{
  "title": "Astro",
  "description": "The web framework for content-driven websites",
  "siteurl": "https://github.com/withastro/astro",
  "imgurl": "https://avatars.githubusercontent.com/u/44914786?v=4&s=640",
  "tags": ["Framework"],
  "sort_order": 0,
  "is_active": true
}
```

**字段说明**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | `string` | 是 | 友链网站标题/名称，长度 1-100 字符 |
| `description` | `string` | 是 | 友链网站描述，长度 1-255 字符 |
| `siteurl` | `string` | 是 | 网站链接，有效 URL 格式，长度 1-500 字符 |
| `imgurl` | `string` | 是 | 图片 URL 或本地路径，长度 1-500 字符 |
| `tags` | `string[]` | 否 | 标签数组，最多 10 个，每个 1-50 字符 |
| `sort_order` | `integer` | 否 | 排序顺序，非负整数，默认 0 |
| `is_active` | `boolean` | 否 | 是否启用，默认 true |

**业务规则**：
- `siteurl` 必须以 `http://` 或 `https://` 开头，不允许危险协议
- `siteurl` 需全局唯一，重复时拒绝创建
- `tags` 提供时，自动创建不存在的标签并建立关联
- ID 使用数据库自增，起始值为 1

### 3.5 更新友链

**功能说明**：修改已有友链信息。

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `integer` | 友链 ID |

**请求体**：与创建友链相同，所有字段可选，未提供的字段保持原值不变。

**业务规则**：
- `siteurl` 修改时需重新校验 URL 格式和唯一性（排除自身）
- `tags` 提供时完全替换原有标签关联
- `imgurl` 修改时，若原图为本地上传且新 URL 不同，建议清理旧图片

### 3.6 删除友链

**功能说明**：删除指定友链。

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `integer` | 友链 ID |

**业务规则**：
- 采用软删除策略，设置 `is_deleted = true` 并记录 `deleted_at`
- 若友链头像为本地上传（`img_type = 1`），可选择物理删除图片文件或保留
- 软删除的友链不再出现在列表和详情接口中
- 关联的 `friend_tag_links` 记录同步级联删除

### 3.7 获取所有标签

**功能说明**：获取全部标签及其关联的未删除且启用的友链数量。

**响应数据**：

```json
{
  "tags": [
    { "id": 1, "name": "Framework", "count": 3 },
    { "id": 2, "name": "Docs", "count": 2 },
    { "id": 3, "name": "Hosting", "count": 1 }
  ]
}
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `tags` | `array` | 标签数组 |
| `tags[].id` | `integer` | 标签 ID |
| `tags[].name` | `string` | 标签名称 |
| `tags[].count` | `integer` | 该标签关联的未删除且启用的友链数量 |

**业务规则**：
- 返回结果按 `name` 字母顺序排序
- `count` 只统计 `is_deleted = false` 且 `is_active = true` 的友链

### 3.8 上传友链头像

**功能说明**：本地上传友链头像图片。

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `image` | `file` | 是 | 图片文件，单文件上传 |

**文件验证规则**：

| 规则 | 限制 |
|------|------|
| 文件类型 | `image/jpeg`, `image/png`, `image/webp`, `image/gif` |
| 单文件大小 | 不超过 5MB |
| 图片尺寸 | 建议 64x64 到 1024x1024 像素 |

**响应数据**：

```json
{
  "friend_id": 2,
  "imgurl": "/images/friends/2025/03/mizuki_docs.webp",
  "img_type": 1,
  "filename": "mizuki_docs.webp",
  "size": 20480,
  "mime_type": "image/webp",
  "width": 640,
  "height": 640
}
```

**业务规则**：
- 上传成功后自动更新对应友链的 `imgurl`、`img_type`、`img_storage_path` 字段
- 若该友链原头像为本地上传，上传新图片后自动替换并删除旧图片
- 建议将非 WebP 格式自动转换为 WebP，质量 85%
- 建议等比缩放，最大边长 640px（友链头像无需大尺寸）

### 3.9 删除友链头像

**功能说明**：删除本地上传的友链头像。

**路径参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | `integer` | 友链 ID |

**业务规则**：
- 仅当 `img_type = 1`（本地上传）时执行删除
- 从存储中物理删除图片文件
- 将友链的 `imgurl` 重置为空字符串，`img_type` 重置为 0

---

## 4. 业务逻辑规则

### 4.1 ID 生成策略

- 使用数据库自增 `BIGINT UNSIGNED`
- 起始值为 1
- 前端当前使用 `number` 类型，后端应确保 ID 在 JavaScript 安全整数范围内

### 4.2 排序规则

- **默认排序**：按 `sort_order` 字段升序排列
- **相同 sort_order**：按 `created_at` 降序排列（新创建的在前）
- **随机排序**：当 `shuffle=true` 时，在查询结果基础上随机打乱

### 4.3 图片处理规则

#### 图片类型区分

| 类型 | `img_type` | `imgurl` 示例 | `img_storage_path` |
|------|-----------|---------------|-------------------|
| 外链图片 | `0` | `https://avatars.githubusercontent.com/...` | `NULL` |
| 本地上传 | `1` | `/images/friends/2025/03/astro.webp` | `friends/2025/03/astro.webp` |

#### 外链图片处理

- 直接存储用户提供的 URL，不做上传处理
- 需验证 URL 格式合法性
- 不保证外链图片的可用性，前端需处理加载失败情况

#### 本地上传图片处理

- **存储路径规范**：`/images/friends/{year}/{month}/{filename}`
- **文件名生成**：原始文件名清洗后添加唯一标识，如 `{timestamp}_{random}_{clean_filename}.webp`
- **图片处理**：建议格式转换为 WebP（质量 85%），等比缩放最大边长 640px

### 4.4 标签处理规则

#### 标签规范化

1. **去重**：同一友链中不允许重复标签（不区分大小写）
2. **大小写处理**：统一存储为原始大小写，但去重时不区分大小写
3. **特殊字符**：去除首尾空白，中间空格替换为连字符
4. **长度限制**：单个标签 1-50 字符
5. **数量限制**：单个友链最多 10 个标签

#### 标签创建/关联流程

```
接收标签数组
       │
       ▼
逐个规范化处理
       │
       ▼
查询 friend_tags 表
       │
       ├── 存在 ──▶ 获取 tag_id
       └── 不存在 ──▶ 插入新标签 ──▶ 获取 tag_id
       │
       ▼
删除旧的 friend_tag_links 关联
       │
       ▼
插入新的 friend_tag_links 关联
```

### 4.5 友链去重规则

- `siteurl` 字段设置唯一索引，防止重复添加同一网站
- URL 规范化后再比较：去除末尾斜杠、统一协议和域名部分为小写
- 更新时排除自身 ID 后再检查唯一性

---

## 5. 数据验证

### 5.1 输入验证总览

| 字段 | 类型 | 必填 | 最小长度 | 最大长度 | 格式要求 |
|------|------|------|----------|----------|----------|
| `title` | `string` | 是 | 1 | 100 | 任意文本 |
| `description` | `string` | 是 | 1 | 255 | 任意文本 |
| `siteurl` | `string` | 是 | 1 | 500 | 有效 HTTP/HTTPS URL |
| `imgurl` | `string` | 是 | 1 | 500 | 有效 URL 或本地路径 |
| `tags` | `string[]` | 否 | - | 10 个元素 | 每个元素 1-50 字符 |
| `sort_order` | `integer` | 否 | 0 | - | 非负整数 |
| `is_active` | `boolean` | 否 | - | - | `true` 或 `false` |

### 5.2 详细验证规则

#### title（友链标题）

- 不能为空字符串或仅包含空白字符
- 去除首尾空白后长度 >= 1
- 长度 <= 100 字符（UTF-8）

#### description（友链描述）

- 不能为空字符串或仅包含空白字符
- 去除首尾空白后长度 >= 1
- 长度 <= 255 字符（UTF-8）

#### siteurl（网站链接）

- 必须为有效的 URL 格式
- 必须以 `http://` 或 `https://` 开头
- 长度 <= 500 字符
- 不允许危险协议：`javascript:`、`data:`、`file:`、`vbscript:`
- 必须包含有效的域名或 IP 地址

#### imgurl（图片 URL）

- 外链图片：必须为有效的 HTTP/HTTPS URL，长度 <= 500
- 本地路径：必须以 `/images/` 开头，长度 <= 500
- 不允许危险协议

#### tags（标签）

- 必须为数组类型
- 数组长度 <= 10
- 每个标签去除首尾空白后长度 >= 1 且 <= 50
- 不允许为空字符串
- 不允许包含 `,` 逗号
- 数组内去重（不区分大小写）

---

## 6. 图片存储方案

### 6.1 方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| **本地文件系统** | 简单、无额外成本 | 不易扩展、备份麻烦 | 个人博客、低流量 |
| **对象存储（OSS/S3）** | 高可用、易扩展、CDN 友好 | 有存储费用 | 推荐方案 |
| **外链图床** | 免维护、零存储成本 | 依赖第三方、可能失效 | 可接受但需有备选 |

### 6.2 推荐方案：对象存储（S3 兼容）

#### 存储结构

```
bucket-name/
├── friends/              # 友链头像目录
│   ├── 2025/
│   │   ├── 01/
│   │   │   ├── 20250115103000_a1b2c3d4_astro.webp
│   │   │   └── 20250115103000_e5f6g7h8_mizuki.webp
│   │   └── 03/
│   │       └── 20250321103000_i9j0k1l2_tailwind.webp
│   └── temp/             # 临时上传目录
│       └── 20250321/
│           └── temp_abc123.webp
└── thumbnails/           # 缩略图（友链头像无需）
```

#### 访问 URL 格式

- **正式图片**：`https://cdn.example.com/images/friends/2025/03/astro.webp`
- **API 返回路径**：`/images/friends/2025/03/astro.webp`（由前端拼接 CDN 域名）

### 6.3 图片处理流水线

```
用户上传 / 提供外链 URL
         │
         ├── 外链 URL ───────────────────────────────┐
         │                                          │
         ▼                                          ▼
┌─────────────────┐                        ┌─────────────────┐
│  验证 URL 格式   │                        │  接收文件       │
│  检查协议安全    │                        └─────────────────┘
└─────────────────┘                                  │
         │                                           ▼
         │                              ┌─────────────────┐
         │                              │ 类型校验        │──失败──▶ 拒绝
         │                              │ (MIME类型)      │
         │                              └─────────────────┘
         │                                        │ 是
         │                                        ▼
         │                              ┌─────────────────┐
         │                              │ 大小校验        │──失败──▶ 拒绝
         │                              │ (<=5MB)         │
         │                              └─────────────────┘
         │                                        │ 是
         │                                        ▼
         │                              ┌─────────────────┐
         │                              │ 保存到临时目录   │
         │                              └─────────────────┘
         │                                        │
         │                                        ▼
         │                              ┌─────────────────┐
         │                              │ 格式转换 WebP   │
         │                              │ 质量 85%        │
         │                              └─────────────────┘
         │                                        │
         │                                        ▼
         │                              ┌─────────────────┐
         │                              │ 等比缩放        │
         │                              │ 最大边长 640px  │
         │                              └─────────────────┘
         │                                        │
         │                                        ▼
         │                              ┌─────────────────┐
         │                              │ 移动到正式路径   │
         │                              └─────────────────┘
         │                                        │
         └────────────────┬───────────────────────┘
                          ▼
                   ┌─────────────────┐
                   │ 更新数据库记录   │
                   │ imgurl/img_type │
                   └─────────────────┘
                          │
                          ▼
                   ┌─────────────────┐
                   │ 返回图片信息     │
                   └─────────────────┘
```

### 6.4 图片清理策略

#### 孤儿图片清理

定期任务扫描并删除：

1. `friends/temp/` 目录下超过 24 小时的文件
2. `friends` 表中 `img_type = 1` 但 `img_storage_path` 对应的文件不存在的记录
3. 已软删除友链的本地上传图片（根据配置决定是否物理删除）

---

## 7. 数据迁移建议

### 7.1 从现有 friends.ts 迁移

现有数据位于 `src/data/friends.ts`，迁移步骤：

1. **解析现有数据**：提取 `friendsData` 数组
2. **数据转换**：
   - `id` -> `id`（保持原值）
   - `title` -> `title`
   - `desc` -> `description`
   - `siteurl` -> `siteurl`
   - `imgurl` -> `imgurl`
   - `img_type` -> 根据 `imgurl` 判断：以 `http` 开头为 `0`，以 `/images/` 开头为 `1`
   - `tags` -> 插入/关联 `friend_tags` 表
3. **图片迁移**：
   - 外链图片：无需迁移，直接保留 URL
   - 本地图片：将文件移动到新的存储路径，更新 `img_storage_path`

### 7.2 回滚策略

- 迁移前完整备份 `src/data/friends.ts` 和 `public/images/friends/`（如有本地图片）
- 使用事务包裹批量插入操作
- 记录迁移日志，包括成功/失败条目
- 保留原数据文件至少 30 天

---

## 8. 附录

### 8.1 前端向后端兼容映射

| 前端字段 | 后端字段 | 说明 |
|----------|----------|------|
| `id` | `id` | 保持 `number` 类型 |
| `title` | `title` | 直接映射 |
| `desc` | `description` | 前端 `desc` 对应后端 `description` |
| `siteurl` | `siteurl` | 直接映射 |
| `imgurl` | `imgurl` | 直接映射 |
| `tags` | `tags` | 字符串数组 |
| - | `img_type` | 新增字段，前端可忽略 |
| - | `sort_order` | 新增字段，控制排序 |
| - | `is_active` | 新增字段，控制显示 |

### 8.2 版本演进建议

| 版本 | 功能 | 说明 |
|------|------|------|
| `v1` | 基础 CRUD + 标签 + 图片上传 | 当前需求 |
| `v2` | 友链分组/分类 + 排序拖拽 | 友链数量增长后 |
| `v3` | 友链状态检测（自动检测对方网站是否可访问） | 运维功能 |
| `v4` | 友链申请/审核流程 | 开放用户提交友链 |
