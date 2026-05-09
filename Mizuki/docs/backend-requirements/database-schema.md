# Mizuki 博客系统 PostgreSQL 数据库表结构设计

> **文档版本**: v2.0
> **最后更新**: 2026-05-04
> **数据库**: PostgreSQL 15+
> **设计目标**: 支撑 Mizuki 博客全部 8 个业务模块 + 系统通用模块

***

## 目录

1. [数据库选型说明](#1-数据库选型说明)
2. [完整建表 SQL](#2-完整建表-sql)
3. [表字段详细说明](#3-表字段详细说明)
4. [表关系 ER 图](#4-表关系-er-图)
5. [JSONB 字段使用说明](#5-jsonb-字段使用说明)
6. [索引设计说明](#6-索引设计说明)
7. [数据迁移注意事项](#7-数据迁移注意事项)

***

## 1. 数据库选型说明

### 1.1 为什么选用 PostgreSQL

| 考量维度         | 选型理由                                                           |
| ------------ | -------------------------------------------------------------- |
| **数据完整性**    | PostgreSQL 提供强大的约束系统（CHECK、UNIQUE），适合博客系统对数据一致性的要求             |
| **JSONB 支持** | 原生 JSONB 类型支持 GIN 索引，可在关系型结构中灵活存储半结构化数据，完美契合 Mizuki 大量配置/元数据场景 |
| **全文搜索**     | 内置全文搜索能力（tsvector/tsquery），满足文章搜索需求，无需引入额外搜索引擎                 |
| **扩展生态**     | 支持 uuid-ossp、pg\_trgm（模糊搜索）、btree\_gin 等扩展，满足未来扩展需求            |
| **并发性能**     | MVCC 实现优秀，读写冲突小，适合博客前台高并发读取 + 后台管理的混合负载                        |
| **开源可控**     | 无商业授权风险，与项目开源属性一致                                              |

### 1.2 JSONB 使用策略

本设计遵循\*\*"结构化字段优先，JSONB 兜底复杂/可变结构"\*\*的原则：

- **JSONB 存储**: 结构多变、层级嵌套、无需单独检索子属性的复杂数据使用 JSONB（如站点配置、设备规格、相册布局参数、图片列表、标签列表）
- **独立表存储**: 实体间明确的一对多关系使用独立关联表（如文章-标签、项目-技术栈），保证查询性能和数据完整性

### 1.3 主键设计原则

- **所有表统一使用 UUID 主键**，由业务层生成并传入，数据库不设置自增
- **无外键显式关联**，表间关系通过字段注释说明，由业务层维护一致性
- **所有表含 version 字段**，用于乐观锁并发控制

***

## 2. 完整建表 SQL

```sql
-- ============================================================
-- Mizuki 博客系统 - PostgreSQL 数据库初始化脚本
-- 字符集: UTF-8
-- 设计规范:
--   1. 所有主键为 UUID，由业务层生成
--   2. 不设置外键显式关联，关系在注释中说明
--   3. 所有表含 version 乐观锁字段
--   4. 数组类型统一使用 JSONB
-- ============================================================

-- 启用必要扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================
-- 1. Posts 模块（文章）
-- ============================================================

CREATE TABLE posts (
    id                  UUID PRIMARY KEY,
    uuid                UUID NOT NULL UNIQUE,
    title               VARCHAR(255) NOT NULL,
    slug                VARCHAR(255) NOT NULL UNIQUE,
    content             TEXT NOT NULL,
    excerpt             VARCHAR(1000),
    description         VARCHAR(500),
    author              VARCHAR(100),
    category            VARCHAR(100),
    lang                VARCHAR(10) DEFAULT 'zh_CN',
    has_draft           BOOLEAN NOT NULL DEFAULT FALSE,
    has_pinned          BOOLEAN NOT NULL DEFAULT FALSE,
    priority            INTEGER DEFAULT 0,
    has_encrypted       BOOLEAN NOT NULL DEFAULT FALSE,
    password_hash       VARCHAR(255),
    password_hint       VARCHAR(255),
    alias               VARCHAR(255) UNIQUE,
    permalink           VARCHAR(255) UNIQUE,
    license_name        VARCHAR(100),
    license_url         VARCHAR(512),
    source_link         VARCHAR(512),
    cover_image         VARCHAR(512),
    has_comment_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    published_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    view_count          BIGINT NOT NULL DEFAULT 0,
    word_count          INTEGER NOT NULL DEFAULT 0,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

CREATE TABLE tags (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(100) NOT NULL UNIQUE,
    slug                VARCHAR(100) NOT NULL UNIQUE,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE post_tags (
    id                  UUID PRIMARY KEY,
    post_id             UUID NOT NULL,          -- 对应 posts.id
    tag_id              UUID NOT NULL,          -- 对应 tags.id
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (post_id, tag_id)
);

-- ============================================================
-- 2. Diary 模块（日记）
-- ============================================================

CREATE TABLE diary_entries (
    id                  UUID PRIMARY KEY,
    content             TEXT NOT NULL,
    entry_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    images              JSONB NOT NULL DEFAULT '[]',
    location            VARCHAR(255),
    mood                VARCHAR(100),
    tags                JSONB NOT NULL DEFAULT '[]',
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

-- ============================================================
-- 3. Friends 模块（友链）
-- ============================================================

CREATE TABLE friends (
    id                  UUID PRIMARY KEY,
    title               VARCHAR(100) NOT NULL,
    description         VARCHAR(255) NOT NULL,
    siteurl             VARCHAR(500) NOT NULL,
    imgurl              VARCHAR(500) NOT NULL,
    img_type            SMALLINT NOT NULL DEFAULT 0,
    img_storage_path    VARCHAR(500),
    sort_order          INTEGER NOT NULL DEFAULT 0,
    has_active          BOOLEAN NOT NULL DEFAULT TRUE,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_friends_siteurl UNIQUE (siteurl)
);

CREATE TABLE friend_tags (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(50) NOT NULL UNIQUE,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE friend_tag_links (
    id                  UUID PRIMARY KEY,
    friend_id           UUID NOT NULL,          -- 对应 friends.id
    tag_id              UUID NOT NULL,          -- 对应 friend_tags.id
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (friend_id, tag_id)
);

-- ============================================================
-- 4. Projects 模块（项目）
-- ============================================================

CREATE TABLE projects (
    id                  UUID PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    description         TEXT NOT NULL,
    image               VARCHAR(512) NOT NULL DEFAULT '',
    category            VARCHAR(100) NOT NULL,
    status              VARCHAR(100) NOT NULL,
    live_demo_url       VARCHAR(512),
    source_code_url     VARCHAR(512),
    visit_url           VARCHAR(512),
    start_date          DATE NOT NULL,
    end_date            DATE,
    has_featured        BOOLEAN NOT NULL DEFAULT FALSE,
    has_show_image      BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

CREATE TABLE project_tech_stacks (
    id                  UUID PRIMARY KEY,
    project_id          UUID NOT NULL,          -- 对应 projects.id
    tech_name           VARCHAR(50) NOT NULL,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, tech_name)
);

CREATE TABLE project_tags (
    id                  UUID PRIMARY KEY,
    project_id          UUID NOT NULL,          -- 对应 projects.id
    tag_name            VARCHAR(50) NOT NULL,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (project_id, tag_name)
);

-- ============================================================
-- 5. Timeline 模块（时间线）
-- ============================================================

CREATE TABLE timeline_events (
    id                  UUID PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    description         TEXT NOT NULL,
    event_type          VARCHAR(100) NOT NULL,
    icon                VARCHAR(100) NOT NULL,
    color               VARCHAR(7) NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE,
    location            VARCHAR(255),
    organization        VARCHAR(255),
    position            VARCHAR(255),
    has_featured        BOOLEAN NOT NULL DEFAULT FALSE,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

CREATE TABLE timeline_event_skills (
    id                  UUID PRIMARY KEY,
    timeline_event_id   UUID NOT NULL,          -- 对应 timeline_events.id
    skill_name          VARCHAR(50) NOT NULL,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (timeline_event_id, skill_name)
);

CREATE TABLE timeline_event_achievements (
    id                  UUID PRIMARY KEY,
    timeline_event_id   UUID NOT NULL,          -- 对应 timeline_events.id
    achievement         VARCHAR(500) NOT NULL,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE timeline_event_links (
    id                  UUID PRIMARY KEY,
    timeline_event_id   UUID NOT NULL,          -- 对应 timeline_events.id
    name                VARCHAR(100) NOT NULL,
    url                 VARCHAR(512) NOT NULL,
    link_type           VARCHAR(100) NOT NULL,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 6. Skills 模块（技能）
-- ============================================================

CREATE TABLE skills (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(50) NOT NULL,
    description         VARCHAR(500) NOT NULL,
    icon                VARCHAR(100) NOT NULL,
    category            VARCHAR(100) NOT NULL,
    level               VARCHAR(100) NOT NULL,
    experience_years    INTEGER NOT NULL DEFAULT 0,
    experience_months   INTEGER NOT NULL DEFAULT 0,
    color               VARCHAR(7),
    projects            JSONB NOT NULL DEFAULT '[]',
    certifications      JSONB NOT NULL DEFAULT '[]',
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

-- ============================================================
-- 7. Devices 模块（设备）
-- ============================================================

CREATE TABLE device_categories (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(100) NOT NULL UNIQUE,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

CREATE TABLE devices (
    id                  UUID PRIMARY KEY,
    category_id         UUID NOT NULL,          -- 对应 device_categories.id
    name                VARCHAR(100) NOT NULL,
    image               VARCHAR(512),
    specs               JSONB,
    description         TEXT,
    link                VARCHAR(512),
    sort_order          INTEGER NOT NULL DEFAULT 0,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE,
    UNIQUE (category_id, name)
);

-- ============================================================
-- 8. Albums 模块（相册）
-- ============================================================

CREATE TABLE albums (
    id                  UUID PRIMARY KEY,
    album_key           VARCHAR(100) NOT NULL UNIQUE,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    album_date          DATE,
    location            VARCHAR(255),
    tags                JSONB NOT NULL DEFAULT '[]',
    layout              VARCHAR(100) NOT NULL DEFAULT 'masonry',
    columns             INTEGER NOT NULL DEFAULT 3,
    cover_image         VARCHAR(512),
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

CREATE TABLE album_photos (
    id                  UUID PRIMARY KEY,
    album_id            UUID NOT NULL,          -- 对应 albums.id
    filename            VARCHAR(255) NOT NULL,
    url                 VARCHAR(512) NOT NULL,
    width               INTEGER,
    height              INTEGER,
    size                BIGINT,
    mime_type           VARCHAR(50),
    has_cover           BOOLEAN NOT NULL DEFAULT FALSE,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE,
    UNIQUE (album_id, filename)
);

-- ============================================================
-- 9. System 模块（站点配置、About、文件上传记录）
-- ============================================================

CREATE TABLE site_configs (
    id                  UUID PRIMARY KEY,
    config_key          VARCHAR(100) NOT NULL UNIQUE,
    config_value        JSONB NOT NULL DEFAULT '{}',
    description         VARCHAR(255),
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE about_pages (
    id                  UUID PRIMARY KEY,
    page_key            VARCHAR(50) NOT NULL DEFAULT 'about',
    content             TEXT NOT NULL,
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE uploaded_files (
    id                  UUID PRIMARY KEY,
    original_name       VARCHAR(255) NOT NULL,
    stored_name         VARCHAR(255) NOT NULL,
    storage_path        VARCHAR(512) NOT NULL,
    url                 VARCHAR(512) NOT NULL,
    folder              VARCHAR(50) NOT NULL,
    file_size           BIGINT,
    mime_type           VARCHAR(50),
    width               INTEGER,
    height              INTEGER,
    thumbnail_url       VARCHAR(512),
    metadata            JSONB DEFAULT '{}',
    version             INTEGER NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          TIMESTAMP WITH TIME ZONE
);

-- ============================================================
-- 索引创建
-- ============================================================

-- Posts 索引
CREATE INDEX idx_posts_pinned_priority ON posts(has_pinned DESC, priority ASC);
CREATE INDEX idx_posts_published_at ON posts(published_at DESC);
CREATE INDEX idx_posts_draft ON posts(has_draft);
CREATE INDEX idx_posts_category ON posts(category);
CREATE INDEX idx_posts_lang ON posts(lang);
CREATE INDEX idx_posts_encrypted ON posts(has_encrypted);
CREATE INDEX idx_posts_slug ON posts(slug);
CREATE INDEX idx_posts_alias ON posts(alias) WHERE alias IS NOT NULL;
CREATE INDEX idx_posts_permalink ON posts(permalink) WHERE permalink IS NOT NULL;
CREATE INDEX idx_posts_deleted_at ON posts(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_posts_search ON posts USING gin(to_tsvector('simple', title || ' ' || COALESCE(content, '')));

-- Tags 索引
CREATE INDEX idx_tags_name ON tags(name);

-- Post tags 索引
CREATE INDEX idx_post_tags_post_id ON post_tags(post_id);
CREATE INDEX idx_post_tags_tag_id ON post_tags(tag_id);

-- Diary 索引
CREATE INDEX idx_diary_entry_date ON diary_entries(entry_date DESC);
CREATE INDEX idx_diary_deleted_at ON diary_entries(deleted_at) WHERE deleted_at IS NULL;

-- Friends 索引
CREATE INDEX idx_friends_sort_order ON friends(sort_order ASC);
CREATE INDEX idx_friends_active ON friends(has_active) WHERE deleted_at IS NULL;
CREATE INDEX idx_friends_deleted_at ON friends(deleted_at) WHERE deleted_at IS NULL;

-- Friend tags 索引
CREATE INDEX idx_friend_tag_links_friend_id ON friend_tag_links(friend_id);
CREATE INDEX idx_friend_tag_links_tag_id ON friend_tag_links(tag_id);

-- Projects 索引
CREATE INDEX idx_projects_category ON projects(category);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_featured ON projects(has_featured);
CREATE INDEX idx_projects_sort ON projects(sort_order ASC, created_at DESC);
CREATE INDEX idx_projects_category_sort ON projects(category, sort_order ASC);
CREATE INDEX idx_projects_featured_sort ON projects(has_featured DESC, sort_order ASC);
CREATE INDEX idx_projects_deleted_at ON projects(deleted_at) WHERE deleted_at IS NULL;

-- Project tech stacks / tags 索引
CREATE INDEX idx_project_tech_project_id ON project_tech_stacks(project_id);
CREATE INDEX idx_project_tech_name ON project_tech_stacks(tech_name);
CREATE INDEX idx_project_tags_project_id ON project_tags(project_id);
CREATE INDEX idx_project_tag_name ON project_tags(tag_name);

-- Timeline 索引
CREATE INDEX idx_timeline_start_date ON timeline_events(start_date DESC);
CREATE INDEX idx_timeline_type ON timeline_events(event_type);
CREATE INDEX idx_timeline_featured ON timeline_events(has_featured);
CREATE INDEX idx_timeline_type_start_date ON timeline_events(event_type, start_date DESC);
CREATE INDEX idx_timeline_featured_start_date ON timeline_events(has_featured DESC, start_date DESC);
CREATE INDEX idx_timeline_deleted_at ON timeline_events(deleted_at) WHERE deleted_at IS NULL;

-- Timeline skills / achievements / links 索引
CREATE INDEX idx_timeline_skills_event_id ON timeline_event_skills(timeline_event_id);
CREATE INDEX idx_timeline_skill_name ON timeline_event_skills(skill_name);
CREATE INDEX idx_timeline_achievements_event_id ON timeline_event_achievements(timeline_event_id);
CREATE INDEX idx_timeline_links_event_id ON timeline_event_links(timeline_event_id);

-- Skills 索引
CREATE INDEX idx_skills_category ON skills(category);
CREATE INDEX idx_skills_level ON skills(level);
CREATE INDEX idx_skills_category_level ON skills(category, level);
CREATE INDEX idx_skills_deleted_at ON skills(deleted_at) WHERE deleted_at IS NULL;

-- Devices 索引
CREATE INDEX idx_devices_category_id ON devices(category_id);
CREATE INDEX idx_devices_deleted_at ON devices(deleted_at) WHERE deleted_at IS NULL;

-- Device categories 索引
CREATE INDEX idx_device_categories_sort ON device_categories(sort_order ASC);

-- Albums 索引
CREATE INDEX idx_albums_date ON albums(album_date DESC);
CREATE INDEX idx_albums_deleted_at ON albums(deleted_at) WHERE deleted_at IS NULL;

-- Album photos 索引
CREATE INDEX idx_album_photos_album_id ON album_photos(album_id);
CREATE INDEX idx_album_photos_cover ON album_photos(album_id, has_cover) WHERE has_cover = TRUE;

-- Site configs 索引
CREATE INDEX idx_site_configs_key ON site_configs(config_key);

-- Uploaded files 索引
CREATE INDEX idx_uploaded_files_folder ON uploaded_files(folder);
CREATE INDEX idx_uploaded_files_deleted_at ON uploaded_files(deleted_at) WHERE deleted_at IS NULL;

-- ============================================================
-- 触发器: 自动更新 updated_at
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_posts_updated_at BEFORE UPDATE ON posts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tags_updated_at BEFORE UPDATE ON tags
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_post_tags_updated_at BEFORE UPDATE ON post_tags
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_diary_entries_updated_at BEFORE UPDATE ON diary_entries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_friends_updated_at BEFORE UPDATE ON friends
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_friend_tags_updated_at BEFORE UPDATE ON friend_tags
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_friend_tag_links_updated_at BEFORE UPDATE ON friend_tag_links
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_project_tech_stacks_updated_at BEFORE UPDATE ON project_tech_stacks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_project_tags_updated_at BEFORE UPDATE ON project_tags
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_timeline_events_updated_at BEFORE UPDATE ON timeline_events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_timeline_event_skills_updated_at BEFORE UPDATE ON timeline_event_skills
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_timeline_event_achievements_updated_at BEFORE UPDATE ON timeline_event_achievements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_timeline_event_links_updated_at BEFORE UPDATE ON timeline_event_links
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_skills_updated_at BEFORE UPDATE ON skills
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_device_categories_updated_at BEFORE UPDATE ON device_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_devices_updated_at BEFORE UPDATE ON devices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_albums_updated_at BEFORE UPDATE ON albums
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_album_photos_updated_at BEFORE UPDATE ON album_photos
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_site_configs_updated_at BEFORE UPDATE ON site_configs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_about_pages_updated_at BEFORE UPDATE ON about_pages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_uploaded_files_updated_at BEFORE UPDATE ON uploaded_files
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

***

## 3. 表字段详细说明

### 3.1 `posts`（文章主表）

| 字段名                   | 类型            | 约束                      | 说明                |
| --------------------- | ------------- | ----------------------- | ----------------- |
| id                    | UUID          | PRIMARY KEY             | 文章主键，由业务层生成       |
| title                 | VARCHAR(255)  | NOT NULL                | 文章标题              |
| slug                  | VARCHAR(255)  | NOT NULL, UNIQUE        | URL 友好标识符         |
| content               | TEXT          | NOT NULL                | Markdown 正文内容     |
| excerpt               | VARCHAR(1000) | <br />                  | 自动生成的摘要           |
| description           | VARCHAR(500)  | <br />                  | 文章描述/SEO 摘要       |
| author                | VARCHAR(100)  | <br />                  | 作者名               |
| category              | VARCHAR(100)  | <br />                  | 分类名称              |
| lang                  | VARCHAR(10)   | DEFAULT 'zh\_CN'        | 语言代码              |
| has\_draft            | BOOLEAN       | NOT NULL, DEFAULT FALSE | 草稿状态              |
| has\_pinned           | BOOLEAN       | NOT NULL, DEFAULT FALSE | 是否置顶              |
| priority              | INTEGER       | DEFAULT 0               | 置顶优先级，越小越靠前       |
| has\_encrypted        | BOOLEAN       | NOT NULL, DEFAULT FALSE | 是否加密              |
| password\_hash        | VARCHAR(255)  | <br />                  | 加密密码哈希（bcrypt）    |
| password\_hint        | VARCHAR(255)  | <br />                  | 密码提示              |
| alias                 | VARCHAR(255)  | UNIQUE                  | 文章别名              |
| permalink             | VARCHAR(255)  | UNIQUE                  | 自定义固定链接           |
| license\_name         | VARCHAR(100)  | <br />                  | 许可证名称             |
| license\_url          | VARCHAR(512)  | <br />                  | 许可证链接             |
| source\_link          | VARCHAR(512)  | <br />                  | 原文链接              |
| cover\_image          | VARCHAR(512)  | <br />                  | 封面图片路径/URL        |
| has\_comment\_enabled | BOOLEAN       | NOT NULL, DEFAULT TRUE  | 是否启用评论            |
| published\_at         | TIMESTAMPTZ   | NOT NULL                | 发布日期时间            |
| view\_count           | BIGINT        | NOT NULL, DEFAULT 0     | 浏览次数              |
| word\_count           | INTEGER       | NOT NULL, DEFAULT 0     | 字数统计              |
| version               | INTEGER       | NOT NULL, DEFAULT 0     | 乐观锁版本号            |
| created\_at           | TIMESTAMPTZ   | NOT NULL                | 创建时间              |
| updated\_at           | TIMESTAMPTZ   | NOT NULL                | 最后更新时间            |
| deleted\_at           | TIMESTAMPTZ   | <br />                  | 软删除时间戳，NULL 表示未删除 |

### 3.2 `tags`（标签表）

| 字段名         | 类型           | 约束                  | 说明          |
| ----------- | ------------ | ------------------- | ----------- |
| id          | UUID         | PRIMARY KEY         | 标签主键，由业务层生成 |
| name        | VARCHAR(100) | NOT NULL, UNIQUE    | 标签名称        |
| slug        | VARCHAR(100) | NOT NULL, UNIQUE    | URL 友好的标签标识 |
| version     | INTEGER      | NOT NULL, DEFAULT 0 | 乐观锁版本号      |
| created\_at | TIMESTAMPTZ  | NOT NULL            | 创建时间        |
| updated\_at | TIMESTAMPTZ  | NOT NULL            | 最后更新时间      |

### 3.3 `post_tags`（文章-标签关联表）

| 字段名         | 类型                  | 约束                  | 说明          |
| ----------- | ------------------- | ------------------- | ----------- |
| id          | UUID                | PRIMARY KEY         | 关联主键，由业务层生成 |
| post\_id    | UUID                | NOT NULL            | 对应 posts.id |
| tag\_id     | UUID                | NOT NULL            | 对应 tags.id  |
| version     | INTEGER             | NOT NULL, DEFAULT 0 | 乐观锁版本号      |
| created\_at | TIMESTAMPTZ         | NOT NULL            | 关联创建时间      |
| UNIQUE      | (post\_id, tag\_id) | <br />              | 联合唯一约束      |

### 3.4 `diary_entries`（日记条目表）

| 字段名         | 类型           | 约束                      | 说明               |
| ----------- | ------------ | ----------------------- | ---------------- |
| id          | UUID         | PRIMARY KEY             | 日记主键，由业务层生成      |
| content     | TEXT         | NOT NULL                | 日记正文内容           |
| entry\_date | TIMESTAMPTZ  | NOT NULL                | 日记日期时间（ISO 8601） |
| images      | JSONB        | NOT NULL, DEFAULT '\[]' | 图片 URL 数组        |
| location    | VARCHAR(255) | <br />                  | 地点信息             |
| mood        | VARCHAR(100) | <br />                  | 心情描述或 Emoji      |
| tags        | JSONB        | NOT NULL, DEFAULT '\[]' | 标签字符串数组          |
| version     | INTEGER      | NOT NULL, DEFAULT 0     | 乐观锁版本号           |
| created\_at | TIMESTAMPTZ  | NOT NULL                | 创建时间             |
| updated\_at | TIMESTAMPTZ  | NOT NULL                | 最后更新时间           |
| deleted\_at | TIMESTAMPTZ  | <br />                  | 软删除时间戳           |

### 3.5 `friends`（友链表）

| 字段名                | 类型           | 约束                     | 说明               |
| ------------------ | ------------ | ---------------------- | ---------------- |
| id                 | UUID         | PRIMARY KEY            | 友链主键，由业务层生成      |
| title              | VARCHAR(100) | NOT NULL               | 友链网站标题           |
| description        | VARCHAR(255) | NOT NULL               | 友链网站描述           |
| siteurl            | VARCHAR(500) | NOT NULL, UNIQUE       | 网站链接（完整 URL）     |
| imgurl             | VARCHAR(500) | NOT NULL               | 头像/Logo 图片访问 URL |
| img\_type          | SMALLINT     | NOT NULL, DEFAULT 0    | 图片类型：0=外链，1=本地上传 |
| img\_storage\_path | VARCHAR(500) | <br />                 | 本地上传图片的存储路径      |
| sort\_order        | INTEGER      | NOT NULL, DEFAULT 0    | 排序顺序             |
| has\_active        | BOOLEAN      | NOT NULL, DEFAULT TRUE | 是否启用/显示          |
| version            | INTEGER      | NOT NULL, DEFAULT 0    | 乐观锁版本号           |
| created\_at        | TIMESTAMPTZ  | NOT NULL               | 创建时间             |
| updated\_at        | TIMESTAMPTZ  | NOT NULL               | 最后更新时间           |
| deleted\_at        | TIMESTAMPTZ  | <br />                 | 软删除时间戳           |

### 3.6 `friend_tags`（友链标签表）

| 字段名         | 类型          | 约束                  | 说明          |
| ----------- | ----------- | ------------------- | ----------- |
| id          | UUID        | PRIMARY KEY         | 标签主键，由业务层生成 |
| name        | VARCHAR(50) | NOT NULL, UNIQUE    | 标签名称        |
| version     | INTEGER     | NOT NULL, DEFAULT 0 | 乐观锁版本号      |
| created\_at | TIMESTAMPTZ | NOT NULL            | 创建时间        |
| updated\_at | TIMESTAMPTZ | NOT NULL            | 最后更新时间      |

### 3.7 `friend_tag_links`（友链-标签关联表）

| 字段名         | 类型                    | 约束                  | 说明                 |
| ----------- | --------------------- | ------------------- | ------------------ |
| id          | UUID                  | PRIMARY KEY         | 关联主键，由业务层生成        |
| friend\_id  | UUID                  | NOT NULL            | 对应 friends.id      |
| tag\_id     | UUID                  | NOT NULL            | 对应 friend\_tags.id |
| version     | INTEGER               | NOT NULL, DEFAULT 0 | 乐观锁版本号             |
| created\_at | TIMESTAMPTZ           | NOT NULL            | 关联创建时间             |
| UNIQUE      | (friend\_id, tag\_id) | <br />              | 联合唯一约束             |

### 3.8 `projects`（项目表）

| 字段名               | 类型           | 约束                      | 说明                                                    |
| ----------------- | ------------ | ----------------------- | ----------------------------------------------------- |
| id                | UUID         | PRIMARY KEY             | 项目主键，由业务层生成                                           |
| title             | VARCHAR(255) | NOT NULL                | 项目标题                                                  |
| description       | TEXT         | NOT NULL                | 项目描述                                                  |
| image             | VARCHAR(512) | NOT NULL, DEFAULT ''    | 封面图片路径                                                |
| category          | VARCHAR(100) | NOT NULL                | 项目类别。可选值：web(网站)、mobile(移动端)、desktop(桌面端)、other(其他)   |
| status            | VARCHAR(100) | NOT NULL                | 项目状态。可选值：completed(已完成)、in-progress(进行中)、planned(计划中) |
| live\_demo\_url   | VARCHAR(512) | <br />                  | 在线演示地址                                                |
| source\_code\_url | VARCHAR(512) | <br />                  | 源码仓库地址                                                |
| visit\_url        | VARCHAR(512) | <br />                  | 项目主页地址                                                |
| start\_date       | DATE         | NOT NULL                | 开始日期                                                  |
| end\_date         | DATE         | <br />                  | 结束日期                                                  |
| has\_featured     | BOOLEAN      | NOT NULL, DEFAULT FALSE | 是否置顶/精选                                               |
| has\_show\_image  | BOOLEAN      | NOT NULL, DEFAULT TRUE  | 是否显示封面图                                               |
| sort\_order       | INTEGER      | NOT NULL, DEFAULT 0     | 排序权重                                                  |
| version           | INTEGER      | NOT NULL, DEFAULT 0     | 乐观锁版本号                                                |
| created\_at       | TIMESTAMPTZ  | NOT NULL                | 创建时间                                                  |
| updated\_at       | TIMESTAMPTZ  | NOT NULL                | 最后更新时间                                                |
| deleted\_at       | TIMESTAMPTZ  | <br />                  | 软删除时间戳                                                |

### 3.9 `project_tech_stacks`（项目技术栈关联表）

| 字段名         | 类型                        | 约束                  | 说明             |
| ----------- | ------------------------- | ------------------- | -------------- |
| id          | UUID                      | PRIMARY KEY         | 主键，由业务层生成      |
| project\_id | UUID                      | NOT NULL            | 对应 projects.id |
| tech\_name  | VARCHAR(50)               | NOT NULL            | 技术名称           |
| version     | INTEGER                   | NOT NULL, DEFAULT 0 | 乐观锁版本号         |
| created\_at | TIMESTAMPTZ               | NOT NULL            | 创建时间           |
| UNIQUE      | (project\_id, tech\_name) | <br />              | 联合唯一约束         |

### 3.10 `project_tags`（项目标签关联表）

| 字段名         | 类型                       | 约束                  | 说明             |
| ----------- | ------------------------ | ------------------- | -------------- |
| id          | UUID                     | PRIMARY KEY         | 主键，由业务层生成      |
| project\_id | UUID                     | NOT NULL            | 对应 projects.id |
| tag\_name   | VARCHAR(50)              | NOT NULL            | 标签名称           |
| version     | INTEGER                  | NOT NULL, DEFAULT 0 | 乐观锁版本号         |
| created\_at | TIMESTAMPTZ              | NOT NULL            | 创建时间           |
| UNIQUE      | (project\_id, tag\_name) | <br />              | 联合唯一约束         |

### 3.11 `timeline_events`（时间线事件表）

| 字段名           | 类型           | 约束                      | 说明                                                                  |
| ------------- | ------------ | ----------------------- | ------------------------------------------------------------------- |
| id            | UUID         | PRIMARY KEY             | 事件主键，由业务层生成                                                         |
| title         | VARCHAR(255) | NOT NULL                | 事件标题                                                                |
| description   | TEXT         | NOT NULL                | 事件详细描述                                                              |
| event\_type   | VARCHAR(100) | NOT NULL                | 事件类型。可选值：education(教育经历)、work(工作经历)、project(项目经历)、achievement(个人成就) |
| icon          | VARCHAR(100) | NOT NULL                | 图标标识符                                                               |
| color         | VARCHAR(7)   | NOT NULL                | HEX 颜色值（如 #2A53DD）                                                  |
| start\_date   | DATE         | NOT NULL                | 开始日期                                                                |
| end\_date     | DATE         | <br />                  | 结束日期，NULL 表示进行中                                                     |
| location      | VARCHAR(255) | <br />                  | 地点                                                                  |
| organization  | VARCHAR(255) | <br />                  | 所属机构/组织                                                             |
| position      | VARCHAR(255) | <br />                  | 职位/角色                                                               |
| has\_featured | BOOLEAN      | NOT NULL, DEFAULT FALSE | 是否为重点展示事件                                                           |
| version       | INTEGER      | NOT NULL, DEFAULT 0     | 乐观锁版本号                                                              |
| created\_at   | TIMESTAMPTZ  | NOT NULL                | 创建时间                                                                |
| updated\_at   | TIMESTAMPTZ  | NOT NULL                | 最后更新时间                                                              |
| deleted\_at   | TIMESTAMPTZ  | <br />                  | 软删除时间戳                                                              |

### 3.12 `timeline_event_skills`（时间线技能关联表）

| 字段名                 | 类型                                 | 约束                  | 说明                     |
| ------------------- | ---------------------------------- | ------------------- | ---------------------- |
| id                  | UUID                               | PRIMARY KEY         | 主键，由业务层生成              |
| timeline\_event\_id | UUID                               | NOT NULL            | 对应 timeline\_events.id |
| skill\_name         | VARCHAR(50)                        | NOT NULL            | 技能名称                   |
| version             | INTEGER                            | NOT NULL, DEFAULT 0 | 乐观锁版本号                 |
| created\_at         | TIMESTAMPTZ                        | NOT NULL            | 创建时间                   |
| UNIQUE              | (timeline\_event\_id, skill\_name) | <br />              | 联合唯一约束                 |

### 3.13 `timeline_event_achievements`（时间线成就关联表）

| 字段名                 | 类型           | 约束                  | 说明                     |
| ------------------- | ------------ | ------------------- | ---------------------- |
| id                  | UUID         | PRIMARY KEY         | 主键，由业务层生成              |
| timeline\_event\_id | UUID         | NOT NULL            | 对应 timeline\_events.id |
| achievement         | VARCHAR(500) | NOT NULL            | 成就描述                   |
| sort\_order         | INTEGER      | NOT NULL, DEFAULT 0 | 排序顺序                   |
| version             | INTEGER      | NOT NULL, DEFAULT 0 | 乐观锁版本号                 |
| created\_at         | TIMESTAMPTZ  | NOT NULL            | 创建时间                   |

### 3.14 `timeline_event_links`（时间线链接关联表）

| 字段名                 | 类型           | 约束                  | 说明                                                                 |
| ------------------- | ------------ | ------------------- | ------------------------------------------------------------------ |
| id                  | UUID         | PRIMARY KEY         | 主键，由业务层生成                                                          |
| timeline\_event\_id | UUID         | NOT NULL            | 对应 timeline\_events.id                                             |
| name                | VARCHAR(100) | NOT NULL            | 链接名称                                                               |
| url                 | VARCHAR(512) | NOT NULL            | 链接地址                                                               |
| link\_type          | VARCHAR(100) | NOT NULL            | 链接类型。可选值：website(网站链接)、certificate(证书链接)、project(项目链接)、other(其他链接) |
| version             | INTEGER      | NOT NULL, DEFAULT 0 | 乐观锁版本号                                                             |
| created\_at         | TIMESTAMPTZ  | NOT NULL            | 创建时间                                                               |

### 3.15 `skills`（技能表）

| 字段名                | 类型           | 约束                      | 说明                                                                  |
| ------------------ | ------------ | ----------------------- | ------------------------------------------------------------------- |
| id                 | UUID         | PRIMARY KEY             | 技能主键，由业务层生成                                                         |
| name               | VARCHAR(50)  | NOT NULL                | 技能名称                                                                |
| description        | VARCHAR(500) | NOT NULL                | 技能描述                                                                |
| icon               | VARCHAR(100) | NOT NULL                | Iconify 图标名称                                                        |
| category           | VARCHAR(100) | NOT NULL                | 技能分类。可选值：frontend(前端)、backend(后端)、database(数据库)、tools(工具)、other(其他) |
| level              | VARCHAR(100) | NOT NULL                | 技能等级。可选值：beginner(入门)、intermediate(中级)、advanced(高级)、expert(专家)      |
| experience\_years  | INTEGER      | NOT NULL, DEFAULT 0     | 经验年数                                                                |
| experience\_months | INTEGER      | NOT NULL, DEFAULT 0     | 经验月数（0-11）                                                          |
| color              | VARCHAR(7)   | <br />                  | HEX 主题色                                                             |
| projects           | JSONB        | NOT NULL, DEFAULT '\[]' | 关联项目 ID 列表                                                          |
| certifications     | JSONB        | NOT NULL, DEFAULT '\[]' | 认证证书名称列表                                                            |
| version            | INTEGER      | NOT NULL, DEFAULT 0     | 乐观锁版本号                                                              |
| created\_at        | TIMESTAMPTZ  | NOT NULL                | 创建时间                                                                |
| updated\_at        | TIMESTAMPTZ  | NOT NULL                | 最后更新时间                                                              |
| deleted\_at        | TIMESTAMPTZ  | <br />                  | 软删除时间戳                                                              |

### 3.16 `device_categories`（设备分类表）

| 字段名         | 类型           | 约束                  | 说明          |
| ----------- | ------------ | ------------------- | ----------- |
| id          | UUID         | PRIMARY KEY         | 分类主键，由业务层生成 |
| name        | VARCHAR(100) | NOT NULL, UNIQUE    | 分类名称        |
| sort\_order | INTEGER      | NOT NULL, DEFAULT 0 | 排序顺序        |
| version     | INTEGER      | NOT NULL, DEFAULT 0 | 乐观锁版本号      |
| created\_at | TIMESTAMPTZ  | NOT NULL            | 创建时间        |
| updated\_at | TIMESTAMPTZ  | NOT NULL            | 最后更新时间      |
| deleted\_at | TIMESTAMPTZ  | <br />              | 软删除时间戳      |

### 3.17 `devices`（设备表）

| 字段名          | 类型                   | 约束                  | 说明                       |
| ------------ | -------------------- | ------------------- | ------------------------ |
| id           | UUID                 | PRIMARY KEY         | 设备主键，由业务层生成              |
| category\_id | UUID                 | NOT NULL            | 对应 device\_categories.id |
| name         | VARCHAR(100)         | NOT NULL            | 设备名称                     |
| image        | VARCHAR(512)         | <br />              | 设备图片地址                   |
| specs        | JSONB                | <br />              | 设备规格参数（结构化存储）            |
| description  | TEXT                 | <br />              | 设备描述                     |
| link         | VARCHAR(512)         | <br />              | 设备外部链接                   |
| sort\_order  | INTEGER              | NOT NULL, DEFAULT 0 | 排序顺序                     |
| version      | INTEGER              | NOT NULL, DEFAULT 0 | 乐观锁版本号                   |
| created\_at  | TIMESTAMPTZ          | NOT NULL            | 创建时间                     |
| updated\_at  | TIMESTAMPTZ          | NOT NULL            | 最后更新时间                   |
| deleted\_at  | TIMESTAMPTZ          | <br />              | 软删除时间戳                   |
| UNIQUE       | (category\_id, name) | <br />              | 同一分类下设备名称唯一              |

### 3.18 `albums`（相册表）

| 字段名          | 类型           | 约束                          | 说明                                   |
| ------------ | ------------ | --------------------------- | ------------------------------------ |
| id           | UUID         | PRIMARY KEY                 | 相册主键，由业务层生成                          |
| album\_key   | VARCHAR(100) | NOT NULL, UNIQUE            | 相册唯一标识（目录名）                          |
| title        | VARCHAR(255) | NOT NULL                    | 相册标题                                 |
| description  | TEXT         | <br />                      | 相册描述                                 |
| album\_date  | DATE         | <br />                      | 相册日期                                 |
| location     | VARCHAR(255) | <br />                      | 拍摄地点                                 |
| tags         | JSONB        | NOT NULL, DEFAULT '\[]'     | 标签列表                                 |
| layout       | VARCHAR(100) | NOT NULL, DEFAULT 'masonry' | 相册布局方式。可选值：masonry(瀑布流布局)、grid(网格布局) |
| columns      | INTEGER      | NOT NULL, DEFAULT 3         | 图片展示列数                               |
| cover\_image | VARCHAR(512) | <br />                      | 封面图片路径                               |
| version      | INTEGER      | NOT NULL, DEFAULT 0         | 乐观锁版本号                               |
| created\_at  | TIMESTAMPTZ  | NOT NULL                    | 创建时间                                 |
| updated\_at  | TIMESTAMPTZ  | NOT NULL                    | 最后更新时间                               |
| deleted\_at  | TIMESTAMPTZ  | <br />                      | 软删除时间戳                               |

### 3.19 `album_photos`（相册图片表）

| 字段名         | 类型                    | 约束                      | 说明           |
| ----------- | --------------------- | ----------------------- | ------------ |
| id          | UUID                  | PRIMARY KEY             | 图片主键，由业务层生成  |
| album\_id   | UUID                  | NOT NULL                | 对应 albums.id |
| filename    | VARCHAR(255)          | NOT NULL                | 图片文件名        |
| url         | VARCHAR(512)          | NOT NULL                | 图片访问路径       |
| width       | INTEGER               | <br />                  | 图片宽度（像素）     |
| height      | INTEGER               | <br />                  | 图片高度（像素）     |
| size        | BIGINT                | <br />                  | 文件大小（字节）     |
| mime\_type  | VARCHAR(50)           | <br />                  | MIME 类型      |
| has\_cover  | BOOLEAN               | NOT NULL, DEFAULT FALSE | 是否为封面        |
| version     | INTEGER               | NOT NULL, DEFAULT 0     | 乐观锁版本号       |
| created\_at | TIMESTAMPTZ           | NOT NULL                | 创建时间         |
| updated\_at | TIMESTAMPTZ           | NOT NULL                | 最后更新时间       |
| deleted\_at | TIMESTAMPTZ           | <br />                  | 软删除时间戳       |
| UNIQUE      | (album\_id, filename) | <br />                  | 同一相册下文件名唯一   |

### 3.20 `site_configs`（站点配置表）

| 字段名           | 类型           | 约束                     | 说明              |
| ------------- | ------------ | ---------------------- | --------------- |
| id            | UUID         | PRIMARY KEY            | 主键，由业务层生成       |
| config\_key   | VARCHAR(100) | NOT NULL, UNIQUE       | 配置项键名           |
| config\_value | JSONB        | NOT NULL, DEFAULT '{}' | 配置项值（支持嵌套对象/数组） |
| description   | VARCHAR(255) | <br />                 | 配置项说明           |
| version       | INTEGER      | NOT NULL, DEFAULT 0    | 乐观锁版本号          |
| created\_at   | TIMESTAMPTZ  | NOT NULL               | 创建时间            |
| updated\_at   | TIMESTAMPTZ  | NOT NULL               | 最后更新时间          |

### 3.21 `about_pages`（About 页面内容表）

| 字段名         | 类型          | 约束                        | 说明            |
| ----------- | ----------- | ------------------------- | ------------- |
| id          | UUID        | PRIMARY KEY               | 主键，由业务层生成     |
| page\_key   | VARCHAR(50) | NOT NULL, DEFAULT 'about' | 页面标识          |
| content     | TEXT        | NOT NULL                  | Markdown 原始内容 |
| version     | INTEGER     | NOT NULL, DEFAULT 0       | 乐观锁版本号        |
| created\_at | TIMESTAMPTZ | NOT NULL                  | 创建时间          |
| updated\_at | TIMESTAMPTZ | NOT NULL                  | 最后更新时间        |

### 3.22 `uploaded_files`（文件上传记录表）

| 字段名            | 类型           | 约束                  | 说明           |
| -------------- | ------------ | ------------------- | ------------ |
| id             | UUID         | PRIMARY KEY         | 主键，由业务层生成    |
| original\_name | VARCHAR(255) | NOT NULL            | 原始文件名        |
| stored\_name   | VARCHAR(255) | NOT NULL            | 存储文件名        |
| storage\_path  | VARCHAR(512) | NOT NULL            | 存储路径         |
| url            | VARCHAR(512) | NOT NULL            | 可访问 URL      |
| folder         | VARCHAR(50)  | NOT NULL            | 目标目录类型       |
| file\_size     | BIGINT       | <br />              | 文件大小（字节）     |
| mime\_type     | VARCHAR(50)  | <br />              | MIME 类型      |
| width          | INTEGER      | <br />              | 图片宽度         |
| height         | INTEGER      | <br />              | 图片高度         |
| thumbnail\_url | VARCHAR(512) | <br />              | 缩略图 URL      |
| metadata       | JSONB        | DEFAULT '{}'        | 额外元数据（如处理参数） |
| version        | INTEGER      | NOT NULL, DEFAULT 0 | 乐观锁版本号       |
| created\_at    | TIMESTAMPTZ  | NOT NULL            | 创建时间         |
| updated\_at    | TIMESTAMPTZ  | NOT NULL            | 最后更新时间       |
| deleted\_at    | TIMESTAMPTZ  | <br />              | 软删除时间戳       |

***

## 4. 表关系 ER 图

```
+----------------------------------------------------------+
|                      Mizuki 数据库 ER 图                   |
+----------------------------------------------------------+

  +-----------------+       +--------------------+       +---------------+
  |     posts       |       |    post_tags       |       |     tags      |
  +-----------------+       +--------------------+       +---------------+
  | PK id (UUID)    |◄──────| FK post_id (UUID)  |       | PK id (UUID)  |
  |    uuid         |  1:N  | FK tag_id (UUID)   ├──────►|    name       |
  |    title        |       |    version         |  N:1  |    slug       |
  |    slug         |       +--------------------+       |    ...        |
  |    content      |                                    +---------------+
  |    version      |
  +-----------------+
         │
         │ 1
         │
         ▼ N
  +-----------------+
  |  uploaded_files |
  +-----------------+
  | PK id (UUID)    |
  |    folder       |
  |    metadata     |
  |    (JSONB)      |
  +-----------------+


  +-----------------+       +--------------------+       +---------------+
  |    friends      |       | friend_tag_links   |       | friend_tags   |
  +-----------------+       +--------------------+       +---------------+
  | PK id (UUID)    |◄──────| FK friend_id (UUID)|       | PK id (UUID)  |
  |    title        |  1:N  | FK tag_id (UUID)   ├──────►|    name       |
  |    siteurl      |       |    version         |  N:1  |    ...        |
  |    version      |       +--------------------+       +---------------+
  +-----------------+


  +-----------------+       +-------------------------+
  |    projects     |       |   project_tech_stacks   |
  +-----------------+       +-------------------------+
  | PK id (UUID)    |◄──────| FK project_id (UUID)    |
  |    title        |  1:N  |    tech_name            |
  |    category     |       |    version              |
  |    version      |       +-------------------------+
  +-----------------+       +-------------------------+
         |                  |     project_tags        |
         |                  +-------------------------+
         |                  | FK project_id (UUID)    |
         |                  |    tag_name             |
         |                  |    version              |
         |                  +-------------------------+
         │
         │ 1
         │
         ▼ N
  +-------------------------+
  |    timeline_events      |
  +-------------------------+
  | PK id (UUID)            |
  |    title                |
  |    event_type           |
  |    version              |
  +-------------------------+
         │
         │ 1
         │
         ▼ N
  +-------------------------+    +---------------------------+    +------------------------+
  | timeline_event_skills   |    | timeline_event_achievements|    | timeline_event_links   |
  +-------------------------+    +---------------------------+    +------------------------+
  | PK id (UUID)            |    | PK id (UUID)              |    | PK id (UUID)           |
  | FK timeline_event_id    |    | FK timeline_event_id      |    | FK timeline_event_id   |
  |    skill_name           |    |    achievement            |    |    name                |
  |    version              |    |    sort_order             |    |    url                 |
  +-------------------------+    |    version                |    |    link_type           |
                                 +---------------------------+    |    version             |
                                                                  +------------------------+


  +-----------------+       +-----------------+
  |device_categories|       |    devices      |
  +-----------------+       +-----------------+
  | PK id (UUID)    │◄──────| FK category_id  |
  |    name         |  1:N  | PK id (UUID)    |
  |    version      |       |    name         |
  +-----------------+       |    specs(JSONB) |
                            |    version      |
                            +-----------------+


  +-----------------+       +-----------------+
  |     albums      |       |  album_photos   |
  +-----------------+       +-----------------+
  | PK id (UUID)    │◄──────| FK album_id     |
  |    album_key    |  1:N  | PK id (UUID)    |
  |    tags(JSONB)  |       |    filename     |
  |    version      |       |    url          |
  +-----------------+       |    is_cover     |
                            |    version      |
                            +-----------------+


  +-----------------+
  |     skills      |
  +-----------------+
  | PK id (UUID)    |
  |    name         |
  |    category     |
  |    level        |
  |    projects     |
  |    (JSONB)      |
  |    certifications|
  |    (JSONB)      |
  |    version      |
  +-----------------+


  +-----------------+       +-----------------+
  |  site_configs   |       |  about_pages    |
  +-----------------+       +-----------------+
  | PK id (UUID)    |       | PK id (UUID)    |
  |    config_key   |       |    page_key     |
  |    config_value |       |    content      |
  |    (JSONB)      |       |    version      |
  |    version      |       +-----------------+
  +-----------------+

  +-----------------+
  | diary_entries   |
  +-----------------+
  | PK id (UUID)    |
  |    content      |
  |    entry_date   |
  |    images(JSONB)|
  |    tags(JSONB)  |
  |    version      |
  +-----------------+
```

***

## 5. JSONB 字段使用说明

### 5.1 JSONB 字段清单

| 表名              | 字段名            | 使用原因                                      |
| --------------- | -------------- | ----------------------------------------- |
| diary\_entries  | images         | 图片 URL 数组，使用 JSONB 比 TEXT\[] 更灵活，支持嵌套对象扩展 |
| diary\_entries  | tags           | 标签字符串数组，JSONB 支持更丰富的查询操作                  |
| skills          | projects       | 关联项目 ID 列表，JSONB 支持对象数组扩展                 |
| skills          | certifications | 认证证书名称列表，JSONB 支持对象数组扩展                   |
| devices         | specs          | 设备规格参数结构多变，不同设备类型规格差异大，JSONB 可灵活存储任意规格键值对 |
| albums          | tags           | 相册标签列表，JSONB 支持更丰富的查询操作                   |
| uploaded\_files | metadata       | 文件上传后的处理元数据不固定，JSONB 支持动态扩展               |
| site\_configs   | config\_value  | 站点配置结构高度嵌套且多变，JSONB 完美支持嵌套对象和数组存储         |

### 5.2 各 JSONB 字段详细说明

#### `diary_entries.images`

**数据结构示例**:

```json
[
  "/images/diary/2025/01/sakura.webp",
  "/images/diary/2025/01/park.jpg"
]
```

**查询示例**:

```sql
-- 查询包含某张图片的日记
SELECT * FROM diary_entries WHERE images @> '["/images/diary/2025/01/sakura.webp"]';

-- 查询有图片的日记
SELECT * FROM diary_entries WHERE jsonb_array_length(images) > 0;
```

#### `diary_entries.tags`

**数据结构示例**:

```json
["travel", "anime", "daily"]
```

**查询示例**:

```sql
-- 查询包含特定标签的日记
SELECT * FROM diary_entries WHERE tags @> '["travel"]';

-- 查询同时包含多个标签的日记
SELECT * FROM diary_entries WHERE tags @> '["travel", "anime"]';
```

#### `skills.projects`

**数据结构示例**:

```json
["mizuki", "folkpatch", "personal-site"]
```

#### `skills.certifications`

**数据结构示例**:

```json
["AWS SAA", "CKA", "PMP"]
```

#### `devices.specs`

**使用 JSONB 原因**: 不同设备类型的规格参数差异极大，无法设计统一的结构化字段。

**典型数据结构示例**:

```json
{
  "cpu": "MediaTek MT7981B",
  "ram": "512MB DDR4",
  "storage": "128MB NAND",
  "ports": ["1x 2.5G WAN", "1x 2.5G LAN", "1x USB 3.0"],
  "wireless": {
    "wifi6": true,
    "bands": ["2.4GHz", "5GHz"]
  },
  "dimensions": {
    "width": 98,
    "height": 98,
    "depth": 30,
    "unit": "mm"
  }
}
```

**查询示例**:

```sql
-- 查询支持 WiFi6 的设备
SELECT * FROM devices WHERE specs @> '{"wireless": {"wifi6": true}}';

-- 查询 RAM 为 512MB 的设备
SELECT * FROM devices WHERE specs->>'ram' = '512MB DDR4';
```

**索引建议**:

```sql
-- 为常用规格查询创建 GIN 索引
CREATE INDEX idx_devices_specs ON devices USING gin(specs);
```

#### `albums.tags`

**数据结构示例**:

```json
["landscape", "japan", "sakura"]
```

#### `uploaded_files.metadata`

**使用 JSONB 原因**: 不同文件类型的元数据差异大，且未来可能新增处理参数。

**典型数据结构示例**:

```json
{
  "compression": {
    "original_size": 2048000,
    "compressed_size": 512000,
    "quality": 85,
    "format": "webp"
  },
  "thumbnail": {
    "width": 300,
    "height": 200,
    "path": "/thumbnails/abc.webp"
  },
  "exif": {
    "camera": "Sony A7M4",
    "lens": "FE 24-70mm GM",
    "iso": 100,
    "aperture": "f/2.8"
  }
}
```

#### `site_configs.config_value`

**使用 JSONB 原因**: 站点配置项结构极其复杂且嵌套层级深，使用独立字段会导致表结构臃肿且难以扩展。

**典型数据结构示例**:

```json
{
  "title": "Mizuki Blog",
  "subtitle": "A beautiful blog theme",
  "siteURL": "https://mizuki.example.com/",
  "themeColor": {
    "hue": 250,
    "fixed": false
  },
  "featurePages": {
    "anime": true,
    "diary": true,
    "friends": true
  },
  "navBarConfig": {
    "links": [
      { "name": "Home", "url": "/" },
      { "name": "Posts", "url": "/posts/" }
    ]
  }
}
```

**查询示例**:

```sql
-- 查询特定配置项
SELECT config_value->>'title' FROM site_configs WHERE config_key = 'site_meta';

-- 查询启用了日记页面的配置
SELECT * FROM site_configs WHERE config_value @> '{"featurePages": {"diary": true}}';

-- 使用路径操作符查询嵌套值
SELECT config_value#>'{themeColor,hue}' FROM site_configs WHERE config_key = 'appearance';
```

**索引建议**:

```sql
-- 为配置键值查询创建 GIN 索引
CREATE INDEX idx_site_configs_value ON site_configs USING gin(config_value);
```

***

## 6. 索引设计说明

### 6.1 索引总览

本设计共创建 **50+** 个索引，覆盖所有高频查询场景。索引设计遵循以下原则：

1. **主键自动索引**: 所有 PRIMARY KEY 自动创建 B-tree 唯一索引
2. **查询条件索引**: 所有 WHERE 子句常用字段创建索引
3. **排序索引**: 所有 ORDER BY 字段创建索引
4. **复合索引**: 高频组合查询条件创建复合索引（最左前缀原则）
5. **部分索引**: 软删除表对 `deleted_at IS NULL` 创建部分索引，减少索引体积
6. **GIN 索引**: JSONB 字段使用 GIN 索引
7. **全文索引**: 文章标题+内容使用 GIN + tsvector 实现全文搜索

### 6.2 核心索引详解

#### Posts 模块索引

| 索引名                          | 字段                             | 类型     | 用途             |
| ---------------------------- | ------------------------------ | ------ | -------------- |
| idx\_posts\_pinned\_priority | has\_pinned DESC, priority ASC | B-tree | 置顶文章排序（核心列表查询） |
| idx\_posts\_published\_at    | published\_at DESC             | B-tree | 按发布日期排序        |
| idx\_posts\_draft            | has\_draft                     | B-tree | 草稿过滤           |
| idx\_posts\_category         | category                       | B-tree | 按分类筛选          |
| idx\_posts\_search           | to\_tsvector(title + content)  | GIN    | 全文搜索           |
| idx\_posts\_deleted\_at      | deleted\_at                    | 部分索引   | 软删除过滤，仅索引未删除记录 |

#### Projects 模块索引

| 索引名                           | 字段                                  | 类型     | 用途      |
| ----------------------------- | ----------------------------------- | ------ | ------- |
| idx\_projects\_sort           | sort\_order ASC, created\_at DESC   | B-tree | 默认排序查询  |
| idx\_projects\_category\_sort | category, sort\_order ASC           | B-tree | 分类筛选后排序 |
| idx\_projects\_featured\_sort | has\_featured DESC, sort\_order ASC | B-tree | 置顶项目查询  |

#### Timeline 模块索引

| 索引名                                  | 字段                                   | 类型     | 用途         |
| ------------------------------------ | ------------------------------------ | ------ | ---------- |
| idx\_timeline\_start\_date           | start\_date DESC                     | B-tree | 按日期排序      |
| idx\_timeline\_type\_start\_date     | event\_type, start\_date DESC        | B-tree | 类型筛选后按日期排序 |
| idx\_timeline\_featured\_start\_date | has\_featured DESC, start\_date DESC | B-tree | 精选事件优先排序   |

#### 部分索引（Partial Index）说明

PostgreSQL 的部分索引仅索引满足条件的记录，大幅减少索引体积，特别适合软删除场景：

```sql
-- 仅索引未删除的记录，已删除记录不进入索引
CREATE INDEX idx_posts_deleted_at ON posts(deleted_at) WHERE deleted_at IS NULL;

-- 效果：当查询 WHERE deleted_at IS NULL 时，索引完全覆盖
-- 当表中有大量历史软删除数据时，索引体积比全量索引小很多
```

### 6.3 索引维护建议

```sql
-- 定期分析表，更新查询规划器统计信息
ANALYZE posts;
ANALYZE projects;
ANALYZE timeline_events;

-- 监控索引使用情况（PostgreSQL 内置统计）
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;
```

***

## 7. 数据迁移注意事项

### 7.1 迁移源数据概览

| 模块            | 当前存储位置                      | 数据格式                        | 迁移复杂度 |
| ------------- | --------------------------- | --------------------------- | ----- |
| Posts         | `src/content/posts/`        | Markdown + Frontmatter YAML | 中     |
| Diary         | `src/data/diary.ts`         | TypeScript 数组               | 低     |
| Friends       | `src/data/friends.ts`       | TypeScript 数组               | 低     |
| Projects      | `src/data/projects.ts`      | TypeScript 数组               | 中     |
| Timeline      | `src/data/timeline.ts`      | TypeScript 数组               | 中     |
| Skills        | `src/data/skills.ts`        | TypeScript 数组               | 低     |
| Devices       | `src/data/devices.ts`       | TypeScript Record           | 低     |
| Albums        | `public/images/albums/`     | 目录 + info.json              | 中     |
| System Config | `src/config.ts`             | TypeScript 对象               | 中     |
| About         | `src/content/spec/about.md` | Markdown 文件                 | 低     |

### 7.2 各模块迁移要点

#### Posts 模块迁移

**关键步骤**:

1. **解析 Markdown 文件**: 使用 `gray-matter` 或 `python-frontmatter` 提取 YAML Frontmatter
2. **生成 UUID**: 为每篇文章生成 UUID 作为主键
3. **生成 Slug**: 基于文件名或标题自动生成 URL slug，处理冲突（追加 `-n`）
4. **标签处理**: 提取所有唯一标签，插入 `tags` 表，建立 `post_tags` 关联
5. **正文处理**: Markdown 正文原样存入 `content` 字段
6. **日期转换**: `published` 字段转为 `published_at`（添加时间部分 `T00:00:00Z`）
7. **密码处理**: 若 `encrypted=true`，使用 bcrypt 对 `password` 字段哈希后存入 `password_hash`
8. **图片迁移**: 封面图片复制到统一存储目录，更新 `cover_image` 路径

#### Diary / Friends / Skills 模块迁移

**通用步骤**:

1. **解析 TS 文件**: 提取导出数组
2. **生成 UUID**: 为每条记录生成 UUID 主键
3. **JSONB 转换**: `images` / `tags` / `projects` / `certifications` 等数组转为 JSONB 格式
4. **日期格式**: ISO 8601 字符串直接存入 TIMESTAMPTZ 字段

#### Projects / Timeline 模块迁移

**关键步骤**:

1. **生成 UUID**: 为项目和事件生成 UUID 主键（替代原有自定义字符串 ID）
2. **关联表拆分**: `techStack` / `tags` / `skills` / `achievements` / `links` 等拆分到独立关联表
3. **关联表 UUID**: 关联表记录也需要生成 UUID 主键

#### Devices 模块迁移

**关键步骤**:

1. **分类生成 UUID**: `device_categories` 每条分类生成 UUID
2. **设备生成 UUID**: `devices` 每条设备生成 UUID
3. **Specs 转换**: `specs` 字符串解析为 JSONB 对象

#### Albums 模块迁移

**关键步骤**:

1. **相册生成 UUID**: `albums` 不再使用目录名作为主键，改为 UUID，`album_key` 存储目录名
2. **图片生成 UUID**: `album_photos` 每条图片记录生成 UUID
3. **标签转换**: `tags` 从数组转为 JSONB

#### System 模块迁移

**关键步骤**:

1. **配置生成 UUID**: `site_configs` 每条配置生成 UUID
2. **JSONB 保留**: 配置值直接以 JSONB 存入，保留原嵌套结构

### 7.3 迁移检查清单

- [ ] 所有主键为 UUID，由业务层生成
- [ ] 无外键显式关联，关系通过字段命名和注释说明
- [ ] 所有表含 `version` 字段，默认值为 0
- [ ] 数组类型已转为 JSONB（images, tags, projects, certifications）
- [ ] 联合主键已改为独立 UUID 主键 + 联合唯一约束
- [ ] 日期时间字段时区处理正确（统一 UTC）
- [ ] 软删除标记（`deleted_at`）默认 NULL
- [ ] 图片/文件路径在新系统中可访问
- [ ] 标签去重处理正确
- [ ] 密码哈希使用 bcrypt（而非明文存储）
- [ ] 迁移前后数据条数核对一致
- [ ] 索引创建完成并验证生效
- [ ] 触发器（updated\_at 自动更新）正常工作
- [ ] 保留原始数据文件备份至少 30 天

***

## 附录 A: 命名规范汇总

| 规范项   | 规则                                                   |
| ----- | ---------------------------------------------------- |
| 表名    | 下划线命名法，复数形式（如 `posts`, `timeline_events`）            |
| 字段名   | 下划线命名法（如 `created_at`, `entry_date`）                 |
| 主键    | `id`，类型 UUID，由业务层生成                                  |
| 外键引用  | `{表名}_id`（如 `post_id`, `timeline_event_id`），注释说明对应关系 |
| 审计字段  | `created_at`, `updated_at`, `deleted_at`             |
| 乐观锁   | `version` INTEGER NOT NULL DEFAULT 0                 |
| 软删除   | `deleted_at` 为 NULL 表示未删除，有值表示已删除                    |
| 布尔字段  | 统一使用 `has_` 前缀（如 `has_active`, `has_encrypted`）      |
| 枚举字段  | VARCHAR(100)，后端负责枚举映射和校验                             |
| 保留字规避 | `type` 改为 `event_type`，避免与 PostgreSQL 保留字冲突          |

## 附录 B: 字段类型映射参考

| TypeScript 类型        | PostgreSQL 类型          | 说明           |
| -------------------- | ---------------------- | ------------ |
| `string` (ID)        | `UUID`                 | 业务层生成        |
| `string` (短文本)       | `VARCHAR(n)`           | 有长度限制的文本     |
| `string` (长文本)       | `TEXT`                 | 无长度限制的文本     |
| `string` (日期)        | `DATE` / `TIMESTAMPTZ` | 纯日期或带时区时间戳   |
| `boolean`            | `BOOLEAN`              | 布尔值          |
| `string[]` / `any[]` | `JSONB`                | 数组统一使用 JSONB |
| `object` / 嵌套结构      | `JSONB`                | 复杂嵌套对象       |
| `enum`               | `VARCHAR(100)`         | 后端负责枚举映射和校验  |

