-- ============================================================
-- Mizuki 博客系统 - PostgreSQL 数据库初始化脚本
-- 字符集: UTF-8
-- 设计规范:
--   1. 所有主键为 UUID，由业务层生成，数据库不自增
--   2. 不设置外键显式关联，表间关系通过字段注释说明
--   3. 所有表含 version 乐观锁字段，默认值为 0
--   4. 数组类型统一使用 JSONB
--   5. 枚举字段使用 VARCHAR(100)，后端负责枚举映射和校验
--   6. 布尔字段统一使用 has_ 前缀
--   7. 时间字段（created_at, updated_at, deleted_at）由应用层设置，默认值 CURRENT_TIMESTAMP
-- ============================================================

-- 启用必要扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ============================================================
-- 1. Content 模块（文章、标签、分类、归档）
-- ============================================================

-- posts: 文章主表
-- 存储博客文章的核心信息，支持草稿、置顶、加密、评论控制等功能

CREATE TABLE posts (
    id                  UUID PRIMARY KEY,                                     -- ID
    title               VARCHAR(255) NOT NULL,                                -- 文章标题
    slug                VARCHAR(255) NOT NULL UNIQUE,                         -- URL 友好标识符，用于生成文章链接
    content             TEXT NOT NULL,                                        -- Markdown 正文内容
    excerpt             VARCHAR(1000),                                        -- 自动生成的摘要，用于列表展示
    description         VARCHAR(500),                                         -- 文章描述/SEO 摘要
    author              VARCHAR(100),                                         -- 作者名
    category            VARCHAR(100),                                         -- 分类名称
    lang                VARCHAR(10) DEFAULT 'zh_CN',                          -- 语言代码，默认简体中文
    has_draft           BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否为草稿状态。TRUE=草稿，FALSE=已发布
    has_pinned          BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否置顶。TRUE=置顶，FALSE=普通
    priority            INTEGER DEFAULT 0,                                    -- 置顶优先级，数值越小越靠前，默认 0
    has_encrypted       BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否加密。TRUE=需要密码访问，FALSE=公开访问
    password_hash       VARCHAR(255),                                         -- 加密密码哈希（bcrypt），仅当 has_encrypted=TRUE 时有效
    password_hint       VARCHAR(255),                                         -- 密码提示信息
    alias               VARCHAR(255) UNIQUE,                                  -- 文章别名，用于自定义 URL
    permalink           VARCHAR(255) UNIQUE,                                  -- 自定义固定链接
    license_name        VARCHAR(100),                                         -- 许可证名称
    license_url         VARCHAR(512),                                         -- 许可证链接
    source_link         VARCHAR(512),                                         -- 原文链接（转载时使用）
    cover_image         VARCHAR(512),                                         -- 封面图片路径/URL
    has_comment_enabled BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用评论。TRUE=允许评论，FALSE=关闭评论
    published_at        TIMESTAMP WITH TIME ZONE NOT NULL,                    -- 发布日期时间（带时区）
    view_count          BIGINT NOT NULL DEFAULT 0,                            -- 浏览次数统计
    word_count          INTEGER NOT NULL DEFAULT 0,                           -- 字数统计
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号，用于并发控制
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳，NULL 表示未删除，有值表示已删除
);

COMMENT ON TABLE posts IS '文章主表';
COMMENT ON COLUMN posts.id IS 'ID';
COMMENT ON COLUMN posts.title IS '文章标题';
COMMENT ON COLUMN posts.slug IS 'URL 友好标识符';
COMMENT ON COLUMN posts.content IS 'Markdown 正文内容';
COMMENT ON COLUMN posts.excerpt IS '自动生成的摘要';
COMMENT ON COLUMN posts.description IS '文章描述/SEO 摘要';
COMMENT ON COLUMN posts.author IS '作者名';
COMMENT ON COLUMN posts.category IS '分类名称';
COMMENT ON COLUMN posts.lang IS '语言代码';
COMMENT ON COLUMN posts.has_draft IS '是否为草稿状态';
COMMENT ON COLUMN posts.has_pinned IS '是否置顶';
COMMENT ON COLUMN posts.priority IS '置顶优先级';
COMMENT ON COLUMN posts.has_encrypted IS '是否加密';
COMMENT ON COLUMN posts.password_hash IS '加密密码哈希';
COMMENT ON COLUMN posts.password_hint IS '密码提示信息';
COMMENT ON COLUMN posts.alias IS '文章别名';
COMMENT ON COLUMN posts.permalink IS '自定义固定链接';
COMMENT ON COLUMN posts.license_name IS '许可证名称';
COMMENT ON COLUMN posts.license_url IS '许可证链接';
COMMENT ON COLUMN posts.source_link IS '原文链接';
COMMENT ON COLUMN posts.cover_image IS '封面图片路径';
COMMENT ON COLUMN posts.has_comment_enabled IS '是否启用评论';
COMMENT ON COLUMN posts.published_at IS '发布日期时间';
COMMENT ON COLUMN posts.view_count IS '浏览次数';
COMMENT ON COLUMN posts.word_count IS '字数统计';
COMMENT ON COLUMN posts.version IS '乐观锁版本号';
COMMENT ON COLUMN posts.created_at IS '创建时间';
COMMENT ON COLUMN posts.updated_at IS '最后更新时间';
COMMENT ON COLUMN posts.deleted_at IS '软删除时间戳';

-- tags: 标签表
-- 存储文章标签，用于文章分类和检索
CREATE TABLE tags (
    id                  UUID PRIMARY KEY,                                     -- ID
    name                VARCHAR(100) NOT NULL UNIQUE,                         -- 标签名称，全局唯一
    slug                VARCHAR(100) NOT NULL UNIQUE,                         -- URL 友好的标签标识，全局唯一
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 最后更新时间
);

COMMENT ON TABLE tags IS '标签表';
COMMENT ON COLUMN tags.id IS 'ID';
COMMENT ON COLUMN tags.name IS '标签名称';
COMMENT ON COLUMN tags.slug IS 'URL 友好的标签标识';
COMMENT ON COLUMN tags.version IS '乐观锁版本号';
COMMENT ON COLUMN tags.created_at IS '创建时间';
COMMENT ON COLUMN tags.updated_at IS '最后更新时间';

-- post_tags: 文章-标签关联表
-- 维护文章和标签的多对多关系
CREATE TABLE post_tags (
    id                  UUID PRIMARY KEY,                                     -- ID
    post_id             UUID NOT NULL,                                        -- 关联的文章 ID，对应 posts.id
    tag_id              UUID NOT NULL,                                        -- 关联的标签 ID，对应 tags.id
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 关联创建时间
    UNIQUE (post_id, tag_id)                                                  -- 联合唯一约束：同一文章不能重复关联同一标签
);

COMMENT ON TABLE post_tags IS '文章-标签关联表';
COMMENT ON COLUMN post_tags.id IS 'ID';
COMMENT ON COLUMN post_tags.post_id IS '关联的文章 ID，对应 posts.id';
COMMENT ON COLUMN post_tags.tag_id IS '关联的标签 ID，对应 tags.id';
COMMENT ON COLUMN post_tags.version IS '乐观锁版本号';
COMMENT ON COLUMN post_tags.created_at IS '关联创建时间';

-- categories: 文章分类表
-- 存储文章分类信息，支持层级结构和排序
CREATE TABLE categories (
    id                  UUID PRIMARY KEY,                                     -- ID
    name                VARCHAR(100) NOT NULL UNIQUE,                         -- 分类名称，全局唯一
    slug                VARCHAR(100) NOT NULL UNIQUE,                         -- URL 友好的分类标识
    description         VARCHAR(500),                                         -- 分类描述
    parent_id           UUID,                                                 -- 父分类 ID，NULL 表示顶级分类，对应 categories.id
    icon                VARCHAR(100),                                         -- 分类图标（Iconify）
    cover_image         VARCHAR(512),                                         -- 分类封面图片
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序
    has_enabled         BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE categories IS '文章分类表';
COMMENT ON COLUMN categories.id IS 'ID';
COMMENT ON COLUMN categories.name IS '分类名称';
COMMENT ON COLUMN categories.slug IS 'URL 友好的分类标识';
COMMENT ON COLUMN categories.description IS '分类描述';
COMMENT ON COLUMN categories.parent_id IS '父分类 ID，对应 categories.id';
COMMENT ON COLUMN categories.icon IS '分类图标';
COMMENT ON COLUMN categories.cover_image IS '分类封面图片';
COMMENT ON COLUMN categories.sort_order IS '排序顺序';
COMMENT ON COLUMN categories.has_enabled IS '是否启用';
COMMENT ON COLUMN categories.version IS '乐观锁版本号';
COMMENT ON COLUMN categories.created_at IS '创建时间';
COMMENT ON COLUMN categories.updated_at IS '最后更新时间';
COMMENT ON COLUMN categories.deleted_at IS '软删除时间戳';

-- archives: 文章归档索引表
-- 存储文章归档信息，用于快速生成归档页面
CREATE TABLE archives (
    id                  UUID PRIMARY KEY,                                     -- ID
    year                INTEGER NOT NULL,                                     -- 归档年份
    month               INTEGER NOT NULL,                                     -- 归档月份（1-12）
    post_count          INTEGER NOT NULL DEFAULT 0,                           -- 该年月下的文章数量
    post_ids            JSONB NOT NULL DEFAULT '[]',                          -- 该年月下的文章 ID 列表，JSONB 格式
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    UNIQUE (year, month)                                                      -- 联合唯一约束：同一年的同一月只有一条记录
);

COMMENT ON TABLE archives IS '文章归档索引表';
COMMENT ON COLUMN archives.id IS 'ID';
COMMENT ON COLUMN archives.year IS '归档年份';
COMMENT ON COLUMN archives.month IS '归档月份';
COMMENT ON COLUMN archives.post_count IS '该年月文章数量';
COMMENT ON COLUMN archives.post_ids IS '该年月文章 ID 列表';
COMMENT ON COLUMN archives.version IS '乐观锁版本号';
COMMENT ON COLUMN archives.created_at IS '创建时间';
COMMENT ON COLUMN archives.updated_at IS '最后更新时间';

-- ============================================================
-- 2. Diary 模块（日记）
-- ============================================================

-- diary_entries: 日记条目表
-- 存储个人日记/随想，支持图片、位置、心情、标签
CREATE TABLE diary_entries (
    id                  UUID PRIMARY KEY,                                     -- ID
    content             TEXT NOT NULL,                                        -- 日记正文内容
    entry_date          TIMESTAMP WITH TIME ZONE NOT NULL,                    -- 日记日期时间（带时区）
    images              JSONB NOT NULL DEFAULT '[]',                          -- 图片 URL 数组，JSONB 格式
    location            VARCHAR(255),                                         -- 地点信息
    mood                VARCHAR(100),                                         -- 心情描述或 Emoji
    tags                JSONB NOT NULL DEFAULT '[]',                          -- 标签字符串数组，JSONB 格式
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE diary_entries IS '日记条目表';
COMMENT ON COLUMN diary_entries.id IS 'ID';
COMMENT ON COLUMN diary_entries.content IS '日记正文内容';
COMMENT ON COLUMN diary_entries.entry_date IS '日记日期时间';
COMMENT ON COLUMN diary_entries.images IS '图片 URL 数组';
COMMENT ON COLUMN diary_entries.location IS '地点信息';
COMMENT ON COLUMN diary_entries.mood IS '心情描述';
COMMENT ON COLUMN diary_entries.tags IS '标签数组';
COMMENT ON COLUMN diary_entries.version IS '乐观锁版本号';
COMMENT ON COLUMN diary_entries.created_at IS '创建时间';
COMMENT ON COLUMN diary_entries.updated_at IS '最后更新时间';
COMMENT ON COLUMN diary_entries.deleted_at IS '软删除时间戳';

-- ============================================================
-- 3. Social 模块（友链、评论）
-- ============================================================

-- friends: 友链表
-- 存储友情链接信息，支持头像上传、排序、启用/禁用
CREATE TABLE friends (
    id                  UUID PRIMARY KEY,                                     -- ID
    title               VARCHAR(100) NOT NULL,                                -- 友链网站标题
    description         VARCHAR(255) NOT NULL,                                -- 友链网站描述
    siteurl             VARCHAR(500) NOT NULL,                                -- 网站链接（完整 URL），全局唯一
    imgurl              VARCHAR(500) NOT NULL,                                -- 头像/Logo 图片访问 URL
    img_type            SMALLINT NOT NULL DEFAULT 0,                          -- 图片类型：0=外链图片，1=本地上传图片
    img_storage_path    VARCHAR(500),                                         -- 本地上传图片的存储路径，仅当 img_type=1 时有效
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序，数值越小越靠前
    has_active          BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用/显示。TRUE=显示，FALSE=隐藏
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE,                             -- 软删除时间戳
    CONSTRAINT uq_friends_siteurl UNIQUE (siteurl)                            -- 唯一约束：网站链接全局唯一
);

COMMENT ON TABLE friends IS '友链表';
COMMENT ON COLUMN friends.id IS 'ID';
COMMENT ON COLUMN friends.title IS '友链网站标题';
COMMENT ON COLUMN friends.description IS '友链网站描述';
COMMENT ON COLUMN friends.siteurl IS '网站链接';
COMMENT ON COLUMN friends.imgurl IS '头像/Logo 图片 URL';
COMMENT ON COLUMN friends.img_type IS '图片类型：0=外链，1=本地';
COMMENT ON COLUMN friends.img_storage_path IS '本地存储路径';
COMMENT ON COLUMN friends.sort_order IS '排序顺序';
COMMENT ON COLUMN friends.has_active IS '是否启用';
COMMENT ON COLUMN friends.version IS '乐观锁版本号';
COMMENT ON COLUMN friends.created_at IS '创建时间';
COMMENT ON COLUMN friends.updated_at IS '最后更新时间';
COMMENT ON COLUMN friends.deleted_at IS '软删除时间戳';

-- friend_tags: 友链标签表
-- 存储友链分类标签
CREATE TABLE friend_tags (
    id                  UUID PRIMARY KEY,                                     -- ID
    name                VARCHAR(50) NOT NULL UNIQUE,                          -- 标签名称，全局唯一
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 最后更新时间
);

COMMENT ON TABLE friend_tags IS '友链标签表';
COMMENT ON COLUMN friend_tags.id IS 'ID';
COMMENT ON COLUMN friend_tags.name IS '标签名称';
COMMENT ON COLUMN friend_tags.version IS '乐观锁版本号';
COMMENT ON COLUMN friend_tags.created_at IS '创建时间';
COMMENT ON COLUMN friend_tags.updated_at IS '最后更新时间';

-- friend_tag_links: 友链-标签关联表
-- 维护友链和标签的多对多关系
CREATE TABLE friend_tag_links (
    id                  UUID PRIMARY KEY,                                     -- ID
    friend_id           UUID NOT NULL,                                        -- 关联的友链 ID，对应 friends.id
    tag_id              UUID NOT NULL,                                        -- 关联的标签 ID，对应 friend_tags.id
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 关联创建时间
    UNIQUE (friend_id, tag_id)                                                -- 联合唯一约束：同一友链不能重复关联同一标签
);

COMMENT ON TABLE friend_tag_links IS '友链-标签关联表';
COMMENT ON COLUMN friend_tag_links.id IS 'ID';
COMMENT ON COLUMN friend_tag_links.friend_id IS '关联的友链 ID，对应 friends.id';
COMMENT ON COLUMN friend_tag_links.tag_id IS '关联的标签 ID，对应 friend_tags.id';
COMMENT ON COLUMN friend_tag_links.version IS '乐观锁版本号';
COMMENT ON COLUMN friend_tag_links.created_at IS '关联创建时间';

-- comments: 评论表
-- 存储文章评论，支持嵌套回复
CREATE TABLE comments (
    id                  UUID PRIMARY KEY,                                     -- ID
    target_type         VARCHAR(100) NOT NULL,                                -- 评论目标类型。可选值：post(文章)、diary(日记)、page(页面)
    target_id           UUID NOT NULL,                                        -- 评论目标 ID，对应 posts.id 或 diary_entries.id 等
    parent_id           UUID,                                                 -- 父评论 ID，NULL 表示顶级评论，对应 comments.id
    author_name         VARCHAR(100) NOT NULL,                                -- 评论者昵称
    author_email        VARCHAR(255),                                         -- 评论者邮箱
    author_url          VARCHAR(512),                                         -- 评论者网站/主页
    author_avatar       VARCHAR(512),                                         -- 评论者头像 URL
    content             TEXT NOT NULL,                                        -- 评论内容
    ip_address          VARCHAR(45),                                          -- 评论者 IP 地址
    user_agent          VARCHAR(500),                                         -- 评论者 User-Agent
    has_approved        BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否已审核通过。TRUE=已通过，FALSE=待审核
    has_pinned          BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否置顶
    like_count          INTEGER NOT NULL DEFAULT 0,                           -- 点赞数
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE comments IS '评论表';
COMMENT ON COLUMN comments.id IS 'ID';
COMMENT ON COLUMN comments.target_type IS '评论目标类型';
COMMENT ON COLUMN comments.target_id IS '评论目标 ID';
COMMENT ON COLUMN comments.parent_id IS '父评论 ID，对应 comments.id';
COMMENT ON COLUMN comments.author_name IS '评论者昵称';
COMMENT ON COLUMN comments.author_email IS '评论者邮箱';
COMMENT ON COLUMN comments.author_url IS '评论者网站';
COMMENT ON COLUMN comments.author_avatar IS '评论者头像';
COMMENT ON COLUMN comments.content IS '评论内容';
COMMENT ON COLUMN comments.ip_address IS '评论者 IP';
COMMENT ON COLUMN comments.user_agent IS '评论者 User-Agent';
COMMENT ON COLUMN comments.has_approved IS '是否审核通过';
COMMENT ON COLUMN comments.has_pinned IS '是否置顶';
COMMENT ON COLUMN comments.like_count IS '点赞数';
COMMENT ON COLUMN comments.version IS '乐观锁版本号';
COMMENT ON COLUMN comments.created_at IS '创建时间';
COMMENT ON COLUMN comments.updated_at IS '最后更新时间';
COMMENT ON COLUMN comments.deleted_at IS '软删除时间戳';

-- ============================================================
-- 4. Portfolio 模块（项目、技能、时间线）
-- ============================================================

-- projects: 项目表
-- 存储项目展示信息，支持分类、状态、技术栈、标签
CREATE TABLE projects (
    id                  UUID PRIMARY KEY,                                     -- ID
    title               VARCHAR(255) NOT NULL,                                -- 项目标题
    description         TEXT NOT NULL,                                        -- 项目描述
    image               VARCHAR(512) NOT NULL DEFAULT '',                     -- 封面图片路径
    category            VARCHAR(100) NOT NULL,                                -- 项目类别。可选值：web(网站)、mobile(移动端)、desktop(桌面端)、other(其他)
    status              VARCHAR(100) NOT NULL,                                -- 项目状态。可选值：completed(已完成)、in-progress(进行中)、planned(计划中)
    live_demo_url       VARCHAR(512),                                         -- 在线演示地址
    source_code_url     VARCHAR(512),                                         -- 源码仓库地址
    visit_url           VARCHAR(512),                                         -- 项目主页地址
    start_date          DATE NOT NULL,                                        -- 开始日期
    end_date            DATE,                                                 -- 结束日期，NULL 表示进行中
    has_featured        BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否置顶/精选。TRUE=精选展示，FALSE=普通
    has_show_image      BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否显示封面图。TRUE=显示，FALSE=隐藏
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序权重，数值越小越靠前
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE projects IS '项目表';
COMMENT ON COLUMN projects.id IS 'ID';
COMMENT ON COLUMN projects.title IS '项目标题';
COMMENT ON COLUMN projects.description IS '项目描述';
COMMENT ON COLUMN projects.image IS '封面图片路径';
COMMENT ON COLUMN projects.category IS '项目类别';
COMMENT ON COLUMN projects.status IS '项目状态';
COMMENT ON COLUMN projects.live_demo_url IS '在线演示地址';
COMMENT ON COLUMN projects.source_code_url IS '源码仓库地址';
COMMENT ON COLUMN projects.visit_url IS '项目主页地址';
COMMENT ON COLUMN projects.start_date IS '开始日期';
COMMENT ON COLUMN projects.end_date IS '结束日期';
COMMENT ON COLUMN projects.has_featured IS '是否精选';
COMMENT ON COLUMN projects.has_show_image IS '是否显示封面';
COMMENT ON COLUMN projects.sort_order IS '排序权重';
COMMENT ON COLUMN projects.version IS '乐观锁版本号';
COMMENT ON COLUMN projects.created_at IS '创建时间';
COMMENT ON COLUMN projects.updated_at IS '最后更新时间';
COMMENT ON COLUMN projects.deleted_at IS '软删除时间戳';

-- project_tech_stacks: 项目技术栈关联表
-- 维护项目和技术栈的一对多关系
CREATE TABLE project_tech_stacks (
    id                  UUID PRIMARY KEY,                                     -- ID
    project_id          UUID NOT NULL,                                        -- 关联的项目 ID，对应 projects.id
    tech_name           VARCHAR(50) NOT NULL,                                 -- 技术名称，如 "React", "Spring Boot"
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 关联创建时间
    UNIQUE (project_id, tech_name)                                            -- 联合唯一约束：同一项目不能重复关联同一技术
);

COMMENT ON TABLE project_tech_stacks IS '项目技术栈关联表';
COMMENT ON COLUMN project_tech_stacks.id IS 'ID';
COMMENT ON COLUMN project_tech_stacks.project_id IS '关联的项目 ID，对应 projects.id';
COMMENT ON COLUMN project_tech_stacks.tech_name IS '技术名称';
COMMENT ON COLUMN project_tech_stacks.version IS '乐观锁版本号';
COMMENT ON COLUMN project_tech_stacks.created_at IS '关联创建时间';

-- project_tags: 项目标签关联表
-- 维护项目和标签的一对多关系
CREATE TABLE project_tags (
    id                  UUID PRIMARY KEY,                                     -- ID
    project_id          UUID NOT NULL,                                        -- 关联的项目 ID，对应 projects.id
    tag_name            VARCHAR(50) NOT NULL,                                 -- 标签名称，如 "开源", "个人项目"
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 关联创建时间
    UNIQUE (project_id, tag_name)                                             -- 联合唯一约束：同一项目不能重复关联同一标签
);

COMMENT ON TABLE project_tags IS '项目标签关联表';
COMMENT ON COLUMN project_tags.id IS 'ID';
COMMENT ON COLUMN project_tags.project_id IS '关联的项目 ID，对应 projects.id';
COMMENT ON COLUMN project_tags.tag_name IS '标签名称';
COMMENT ON COLUMN project_tags.version IS '乐观锁版本号';
COMMENT ON COLUMN project_tags.created_at IS '关联创建时间';

-- skills: 技能表
-- 存储个人技能信息，支持分类、等级、关联项目、认证证书
CREATE TABLE skills (
    id                  UUID PRIMARY KEY,                                     -- ID
    name                VARCHAR(50) NOT NULL,                                 -- 技能名称
    description         VARCHAR(500) NOT NULL,                                -- 技能描述
    icon                VARCHAR(100) NOT NULL,                                -- Iconify 图标名称
    category            VARCHAR(100) NOT NULL,                                -- 技能分类。可选值：frontend(前端)、backend(后端)、database(数据库)、tools(工具)、other(其他)
    level               VARCHAR(100) NOT NULL,                                -- 技能等级。可选值：beginner(入门)、intermediate(中级)、advanced(高级)、expert(专家)
    experience_years    INTEGER NOT NULL DEFAULT 0,                           -- 经验年数
    experience_months   INTEGER NOT NULL DEFAULT 0,                           -- 经验月数（0-11）
    color               VARCHAR(7),                                           -- HEX 主题色，如 #2A53DD
    projects            JSONB NOT NULL DEFAULT '[]',                          -- 关联项目 ID 列表，JSONB 格式
    certifications      JSONB NOT NULL DEFAULT '[]',                          -- 认证证书名称列表，JSONB 格式
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE skills IS '技能表';
COMMENT ON COLUMN skills.id IS 'ID';
COMMENT ON COLUMN skills.name IS '技能名称';
COMMENT ON COLUMN skills.description IS '技能描述';
COMMENT ON COLUMN skills.icon IS 'Iconify 图标名称';
COMMENT ON COLUMN skills.category IS '技能分类';
COMMENT ON COLUMN skills.level IS '技能等级';
COMMENT ON COLUMN skills.experience_years IS '经验年数';
COMMENT ON COLUMN skills.experience_months IS '经验月数';
COMMENT ON COLUMN skills.color IS '主题色';
COMMENT ON COLUMN skills.projects IS '关联项目 ID 列表';
COMMENT ON COLUMN skills.certifications IS '认证证书列表';
COMMENT ON COLUMN skills.version IS '乐观锁版本号';
COMMENT ON COLUMN skills.created_at IS '创建时间';
COMMENT ON COLUMN skills.updated_at IS '最后更新时间';
COMMENT ON COLUMN skills.deleted_at IS '软删除时间戳';

-- timeline_events: 时间线事件表
-- 存储个人经历时间线，支持教育、工作、项目、成就四种类型
CREATE TABLE timeline_events (
    id                  UUID PRIMARY KEY,                                     -- ID
    title               VARCHAR(255) NOT NULL,                                -- 事件标题
    description         TEXT NOT NULL,                                        -- 事件详细描述
    event_type          VARCHAR(100) NOT NULL,                                -- 事件类型。可选值：education(教育经历)、work(工作经历)、project(项目经历)、achievement(个人成就)
    icon                VARCHAR(100) NOT NULL,                                -- 图标标识符，使用 Iconify 图标名
    color               VARCHAR(7) NOT NULL,                                  -- HEX 颜色值，如 #2A53DD
    start_date          DATE NOT NULL,                                        -- 开始日期
    end_date            DATE,                                                 -- 结束日期，NULL 表示进行中
    location            VARCHAR(255),                                         -- 地点
    organization        VARCHAR(255),                                         -- 所属机构/组织
    position            VARCHAR(255),                                         -- 职位/角色
    has_featured        BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否为重点展示事件。TRUE=重点展示，FALSE=普通
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE timeline_events IS '时间线事件表';
COMMENT ON COLUMN timeline_events.id IS 'ID';
COMMENT ON COLUMN timeline_events.title IS '事件标题';
COMMENT ON COLUMN timeline_events.description IS '事件描述';
COMMENT ON COLUMN timeline_events.event_type IS '事件类型';
COMMENT ON COLUMN timeline_events.icon IS '图标标识符';
COMMENT ON COLUMN timeline_events.color IS '颜色值';
COMMENT ON COLUMN timeline_events.start_date IS '开始日期';
COMMENT ON COLUMN timeline_events.end_date IS '结束日期';
COMMENT ON COLUMN timeline_events.location IS '地点';
COMMENT ON COLUMN timeline_events.organization IS '所属机构';
COMMENT ON COLUMN timeline_events.position IS '职位/角色';
COMMENT ON COLUMN timeline_events.has_featured IS '是否重点展示';
COMMENT ON COLUMN timeline_events.version IS '乐观锁版本号';
COMMENT ON COLUMN timeline_events.created_at IS '创建时间';
COMMENT ON COLUMN timeline_events.updated_at IS '最后更新时间';
COMMENT ON COLUMN timeline_events.deleted_at IS '软删除时间戳';

-- timeline_event_skills: 时间线技能关联表
-- 维护事件和技能的一对多关系
CREATE TABLE timeline_event_skills (
    id                  UUID PRIMARY KEY,                                     -- ID
    timeline_event_id   UUID NOT NULL,                                        -- 关联的事件 ID，对应 timeline_events.id
    skill_name          VARCHAR(50) NOT NULL,                                 -- 技能名称
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 关联创建时间
    UNIQUE (timeline_event_id, skill_name)                                    -- 联合唯一约束：同一事件不能重复关联同一技能
);

COMMENT ON TABLE timeline_event_skills IS '时间线技能关联表';
COMMENT ON COLUMN timeline_event_skills.id IS 'ID';
COMMENT ON COLUMN timeline_event_skills.timeline_event_id IS '关联的事件 ID，对应 timeline_events.id';
COMMENT ON COLUMN timeline_event_skills.skill_name IS '技能名称';
COMMENT ON COLUMN timeline_event_skills.version IS '乐观锁版本号';
COMMENT ON COLUMN timeline_event_skills.created_at IS '关联创建时间';

-- timeline_event_achievements: 时间线成就关联表
-- 维护事件和成就描述的一对多关系
CREATE TABLE timeline_event_achievements (
    id                  UUID PRIMARY KEY,                                     -- ID
    timeline_event_id   UUID NOT NULL,                                        -- 关联的事件 ID，对应 timeline_events.id
    achievement         VARCHAR(500) NOT NULL,                                -- 成就描述
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序，数值越小越靠前
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- 关联创建时间
);

COMMENT ON TABLE timeline_event_achievements IS '时间线成就关联表';
COMMENT ON COLUMN timeline_event_achievements.id IS 'ID';
COMMENT ON COLUMN timeline_event_achievements.timeline_event_id IS '关联的事件 ID，对应 timeline_events.id';
COMMENT ON COLUMN timeline_event_achievements.achievement IS '成就描述';
COMMENT ON COLUMN timeline_event_achievements.sort_order IS '排序顺序';
COMMENT ON COLUMN timeline_event_achievements.version IS '乐观锁版本号';
COMMENT ON COLUMN timeline_event_achievements.created_at IS '关联创建时间';

-- timeline_event_links: 时间线链接关联表
-- 维护事件和相关链接的一对多关系
CREATE TABLE timeline_event_links (
    id                  UUID PRIMARY KEY,                                     -- ID
    timeline_event_id   UUID NOT NULL,                                        -- 关联的事件 ID，对应 timeline_events.id
    name                VARCHAR(100) NOT NULL,                                -- 链接名称
    url                 VARCHAR(512) NOT NULL,                                -- 链接地址
    link_type           VARCHAR(100) NOT NULL,                                -- 链接类型。可选值：website(网站链接)、certificate(证书链接)、project(项目链接)、other(其他链接)
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- 关联创建时间
);

COMMENT ON TABLE timeline_event_links IS '时间线链接关联表';
COMMENT ON COLUMN timeline_event_links.id IS 'ID';
COMMENT ON COLUMN timeline_event_links.timeline_event_id IS '关联的事件 ID，对应 timeline_events.id';
COMMENT ON COLUMN timeline_event_links.name IS '链接名称';
COMMENT ON COLUMN timeline_event_links.url IS '链接地址';
COMMENT ON COLUMN timeline_event_links.link_type IS '链接类型';
COMMENT ON COLUMN timeline_event_links.version IS '乐观锁版本号';
COMMENT ON COLUMN timeline_event_links.created_at IS '关联创建时间';

-- ============================================================
-- 5. Media 模块（相册、番剧、音乐）
-- ============================================================

-- albums: 相册表
-- 存储相册信息，支持瀑布流/网格布局、标签、封面图
CREATE TABLE albums (
    id                  UUID PRIMARY KEY,                                     -- ID
    album_key           VARCHAR(100) NOT NULL UNIQUE,                         -- 相册唯一标识（目录名），用于生成相册链接
    title               VARCHAR(255) NOT NULL,                                -- 相册标题
    description         TEXT,                                                 -- 相册描述
    album_date          DATE,                                                 -- 相册日期
    location            VARCHAR(255),                                         -- 拍摄地点
    tags                JSONB NOT NULL DEFAULT '[]',                          -- 标签列表，JSONB 格式
    layout              VARCHAR(100) NOT NULL DEFAULT 'masonry',              -- 相册布局方式。可选值：masonry(瀑布流布局)、grid(网格布局)
    columns             INTEGER NOT NULL DEFAULT 3,                           -- 图片展示列数，默认 3 列
    cover_image         VARCHAR(512),                                         -- 封面图片路径
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE albums IS '相册表';
COMMENT ON COLUMN albums.id IS 'ID';
COMMENT ON COLUMN albums.album_key IS '相册唯一标识（目录名）';
COMMENT ON COLUMN albums.title IS '相册标题';
COMMENT ON COLUMN albums.description IS '相册描述';
COMMENT ON COLUMN albums.album_date IS '相册日期';
COMMENT ON COLUMN albums.location IS '拍摄地点';
COMMENT ON COLUMN albums.tags IS '标签列表';
COMMENT ON COLUMN albums.layout IS '相册布局方式';
COMMENT ON COLUMN albums.columns IS '展示列数';
COMMENT ON COLUMN albums.cover_image IS '封面图片路径';
COMMENT ON COLUMN albums.version IS '乐观锁版本号';
COMMENT ON COLUMN albums.created_at IS '创建时间';
COMMENT ON COLUMN albums.updated_at IS '最后更新时间';
COMMENT ON COLUMN albums.deleted_at IS '软删除时间戳';

-- album_photos: 相册图片表
-- 存储相册中的图片信息，支持尺寸、封面标记
CREATE TABLE album_photos (
    id                  UUID PRIMARY KEY,                                     -- ID
    album_id            UUID NOT NULL,                                        -- 所属相册 ID，对应 albums.id
    filename            VARCHAR(255) NOT NULL,                                -- 图片文件名
    url                 VARCHAR(512) NOT NULL,                                -- 图片访问路径
    width               INTEGER,                                              -- 图片宽度（像素）
    height              INTEGER,                                              -- 图片高度（像素）
    size                BIGINT,                                               -- 文件大小（字节）
    mime_type           VARCHAR(50),                                          -- MIME 类型，如 image/jpeg, image/webp
    has_cover           BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否为封面。TRUE=封面图，FALSE=普通图片
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE,                             -- 软删除时间戳
    UNIQUE (album_id, filename)                                               -- 联合唯一约束：同一相册下文件名唯一
);

COMMENT ON TABLE album_photos IS '相册图片表';
COMMENT ON COLUMN album_photos.id IS 'ID';
COMMENT ON COLUMN album_photos.album_id IS '所属相册 ID，对应 albums.id';
COMMENT ON COLUMN album_photos.filename IS '图片文件名';
COMMENT ON COLUMN album_photos.url IS '图片访问路径';
COMMENT ON COLUMN album_photos.width IS '图片宽度';
COMMENT ON COLUMN album_photos.height IS '图片高度';
COMMENT ON COLUMN album_photos.size IS '文件大小';
COMMENT ON COLUMN album_photos.mime_type IS 'MIME 类型';
COMMENT ON COLUMN album_photos.has_cover IS '是否为封面';
COMMENT ON COLUMN album_photos.version IS '乐观锁版本号';
COMMENT ON COLUMN album_photos.created_at IS '创建时间';
COMMENT ON COLUMN album_photos.updated_at IS '最后更新时间';
COMMENT ON COLUMN album_photos.deleted_at IS '软删除时间戳';

-- anime: 番剧表
-- 存储番剧观看记录，支持观看状态、评分、进度追踪
CREATE TABLE anime (
    id                  UUID PRIMARY KEY,                                     -- ID
    title               VARCHAR(255) NOT NULL,                                -- 番剧标题
    status              VARCHAR(100) NOT NULL,                                -- 观看状态。可选值：watching(正在看)、completed(已看完)、planned(计划看)
    rating              DECIMAL(3, 1) NOT NULL DEFAULT 0,                     -- 评分（0-10），支持小数如 9.8
    cover               VARCHAR(512),                                         -- 封面图片路径/URL
    description         VARCHAR(1000),                                        -- 番剧描述
    episodes            VARCHAR(50),                                          -- 集数信息，如 "12 episodes"
    year                VARCHAR(10),                                          -- 年份
    genre               JSONB NOT NULL DEFAULT '[]',                          -- 类型/流派数组，JSONB 格式
    studio              VARCHAR(100),                                         -- 制作公司/工作室
    link                VARCHAR(512),                                         -- 番剧链接（如 Bilibili、Bangumi）
    progress            INTEGER NOT NULL DEFAULT 0,                           -- 当前观看进度（集数）
    total_episodes      INTEGER NOT NULL DEFAULT 0,                           -- 总集数
    start_date          VARCHAR(20),                                          -- 开始观看日期
    end_date            VARCHAR(20),                                          -- 结束观看日期
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序，数值越小越靠前
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE anime IS '番剧表';
COMMENT ON COLUMN anime.id IS 'ID';
COMMENT ON COLUMN anime.title IS '番剧标题';
COMMENT ON COLUMN anime.status IS '观看状态';
COMMENT ON COLUMN anime.rating IS '评分（0-10）';
COMMENT ON COLUMN anime.cover IS '封面图片';
COMMENT ON COLUMN anime.description IS '番剧描述';
COMMENT ON COLUMN anime.episodes IS '集数信息';
COMMENT ON COLUMN anime.year IS '年份';
COMMENT ON COLUMN anime.genre IS '类型/流派数组';
COMMENT ON COLUMN anime.studio IS '制作公司';
COMMENT ON COLUMN anime.link IS '番剧链接';
COMMENT ON COLUMN anime.progress IS '当前观看进度';
COMMENT ON COLUMN anime.total_episodes IS '总集数';
COMMENT ON COLUMN anime.start_date IS '开始观看日期';
COMMENT ON COLUMN anime.end_date IS '结束观看日期';
COMMENT ON COLUMN anime.sort_order IS '排序顺序';
COMMENT ON COLUMN anime.version IS '乐观锁版本号';
COMMENT ON COLUMN anime.created_at IS '创建时间';
COMMENT ON COLUMN anime.updated_at IS '最后更新时间';
COMMENT ON COLUMN anime.deleted_at IS '软删除时间戳';

-- music_playlists: 音乐播放列表表
-- 存储音乐播放列表信息
CREATE TABLE music_playlists (
    id                  UUID PRIMARY KEY,                                     -- ID
    name                VARCHAR(100) NOT NULL,                                -- 播放列表名称
    description         VARCHAR(500),                                         -- 播放列表描述
    cover_image         VARCHAR(512),                                         -- 封面图片 URL
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序
    has_enabled         BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE music_playlists IS '音乐播放列表表';
COMMENT ON COLUMN music_playlists.id IS 'ID';
COMMENT ON COLUMN music_playlists.name IS '播放列表名称';
COMMENT ON COLUMN music_playlists.description IS '播放列表描述';
COMMENT ON COLUMN music_playlists.cover_image IS '封面图片';
COMMENT ON COLUMN music_playlists.sort_order IS '排序顺序';
COMMENT ON COLUMN music_playlists.has_enabled IS '是否启用';
COMMENT ON COLUMN music_playlists.version IS '乐观锁版本号';
COMMENT ON COLUMN music_playlists.created_at IS '创建时间';
COMMENT ON COLUMN music_playlists.updated_at IS '最后更新时间';
COMMENT ON COLUMN music_playlists.deleted_at IS '软删除时间戳';

-- music_tracks: 音乐曲目表
-- 存储音乐曲目信息，支持本地音乐和外部链接
CREATE TABLE music_tracks (
    id                  UUID PRIMARY KEY,                                     -- ID
    playlist_id         UUID NOT NULL,                                        -- 所属播放列表 ID，对应 music_playlists.id
    title               VARCHAR(255) NOT NULL,                                -- 曲目名称
    artist              VARCHAR(100),                                         -- 艺术家/歌手
    album               VARCHAR(100),                                         -- 专辑名称
    cover_image         VARCHAR(512),                                         -- 封面图片 URL
    audio_url           VARCHAR(512),                                         -- 音频文件 URL（本地存储）
    external_url        VARCHAR(512),                                         -- 外部音频链接（如网易云音乐、QQ音乐）
    lyrics              TEXT,                                                 -- 歌词内容（LRC 格式）
    duration            INTEGER,                                              -- 时长（秒）
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE music_tracks IS '音乐曲目表';
COMMENT ON COLUMN music_tracks.id IS 'ID';
COMMENT ON COLUMN music_tracks.playlist_id IS '所属播放列表 ID，对应 music_playlists.id';
COMMENT ON COLUMN music_tracks.title IS '曲目名称';
COMMENT ON COLUMN music_tracks.artist IS '艺术家/歌手';
COMMENT ON COLUMN music_tracks.album IS '专辑名称';
COMMENT ON COLUMN music_tracks.cover_image IS '封面图片';
COMMENT ON COLUMN music_tracks.audio_url IS '音频文件 URL';
COMMENT ON COLUMN music_tracks.external_url IS '外部音频链接';
COMMENT ON COLUMN music_tracks.lyrics IS '歌词内容';
COMMENT ON COLUMN music_tracks.duration IS '时长（秒）';
COMMENT ON COLUMN music_tracks.sort_order IS '排序顺序';
COMMENT ON COLUMN music_tracks.version IS '乐观锁版本号';
COMMENT ON COLUMN music_tracks.created_at IS '创建时间';
COMMENT ON COLUMN music_tracks.updated_at IS '最后更新时间';
COMMENT ON COLUMN music_tracks.deleted_at IS '软删除时间戳';

-- ============================================================
-- 6. Analytics 模块（统计、搜索、公告）
-- ============================================================

-- page_views: 页面访问统计表
-- 存储页面访问记录，用于站点统计
CREATE TABLE page_views (
    id                  UUID PRIMARY KEY,                                     -- ID
    page_path           VARCHAR(500) NOT NULL,                                -- 访问页面路径，如 "/posts/hello-world/"
    page_type           VARCHAR(100) NOT NULL,                                -- 页面类型。可选值：post(文章)、page(页面)、home(首页)、archive(归档)
    target_id           UUID,                                                 -- 关联的文章/页面 ID
    ip_address          VARCHAR(45),                                          -- 访问者 IP 地址
    user_agent          VARCHAR(500),                                         -- 访问者 User-Agent
    referer             VARCHAR(1000),                                        -- 来源页面（Referer）
    visited_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- 访问时间
);

COMMENT ON TABLE page_views IS '页面访问统计表';
COMMENT ON COLUMN page_views.id IS 'ID';
COMMENT ON COLUMN page_views.page_path IS '访问页面路径';
COMMENT ON COLUMN page_views.page_type IS '页面类型';
COMMENT ON COLUMN page_views.target_id IS '关联的文章/页面 ID';
COMMENT ON COLUMN page_views.ip_address IS '访问者 IP';
COMMENT ON COLUMN page_views.user_agent IS '访问者 User-Agent';
COMMENT ON COLUMN page_views.referer IS '来源页面';
COMMENT ON COLUMN page_views.visited_at IS '访问时间';

-- daily_stats: 每日统计汇总表
-- 按天汇总站点访问数据
CREATE TABLE daily_stats (
    id                  UUID PRIMARY KEY,                                     -- ID
    stat_date           DATE NOT NULL UNIQUE,                                 -- 统计日期
    page_views          BIGINT NOT NULL DEFAULT 0,                            -- 页面浏览量（PV）
    unique_visitors     BIGINT NOT NULL DEFAULT 0,                            -- 独立访客数（UV）
    post_reads          BIGINT NOT NULL DEFAULT 0,                            -- 文章阅读量
    comment_count       INTEGER NOT NULL DEFAULT 0,                           -- 评论数
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 最后更新时间
);

COMMENT ON TABLE daily_stats IS '每日统计汇总表';
COMMENT ON COLUMN daily_stats.id IS 'ID';
COMMENT ON COLUMN daily_stats.stat_date IS '统计日期';
COMMENT ON COLUMN daily_stats.page_views IS '页面浏览量（PV）';
COMMENT ON COLUMN daily_stats.unique_visitors IS '独立访客数（UV）';
COMMENT ON COLUMN daily_stats.post_reads IS '文章阅读量';
COMMENT ON COLUMN daily_stats.comment_count IS '评论数';
COMMENT ON COLUMN daily_stats.created_at IS '创建时间';
COMMENT ON COLUMN daily_stats.updated_at IS '最后更新时间';

-- search_logs: 搜索记录表
-- 存储用户搜索关键词，用于搜索热词统计和搜索建议
CREATE TABLE search_logs (
    id                  UUID PRIMARY KEY,                                     -- ID
    keyword             VARCHAR(255) NOT NULL,                                -- 搜索关键词
    result_count        INTEGER NOT NULL DEFAULT 0,                           -- 搜索结果数量
    ip_address          VARCHAR(45),                                          -- 搜索者 IP 地址
    user_agent          VARCHAR(500),                                         -- 搜索者 User-Agent
    searched_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP -- 搜索时间
);

COMMENT ON TABLE search_logs IS '搜索记录表';
COMMENT ON COLUMN search_logs.id IS 'ID';
COMMENT ON COLUMN search_logs.keyword IS '搜索关键词';
COMMENT ON COLUMN search_logs.result_count IS '搜索结果数量';
COMMENT ON COLUMN search_logs.ip_address IS '搜索者 IP';
COMMENT ON COLUMN search_logs.user_agent IS '搜索者 User-Agent';
COMMENT ON COLUMN search_logs.searched_at IS '搜索时间';

-- announcements: 公告表
-- 存储站点公告信息，支持定时展示和关闭
CREATE TABLE announcements (
    id                  UUID PRIMARY KEY,                                     -- ID
    title               VARCHAR(255) NOT NULL,                                -- 公告标题
    content             TEXT NOT NULL,                                        -- 公告内容（支持 HTML/Markdown）
    link_text           VARCHAR(100),                                         -- 链接文本
    link_url            VARCHAR(512),                                         -- 链接 URL
    has_external_link   BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否外部链接
    has_closable        BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否允许用户关闭
    has_enabled         BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用
    start_time          TIMESTAMP WITH TIME ZONE,                             -- 开始展示时间，NULL 表示立即展示
    end_time            TIMESTAMP WITH TIME ZONE,                             -- 结束展示时间，NULL 表示永久展示
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE announcements IS '公告表';
COMMENT ON COLUMN announcements.id IS 'ID';
COMMENT ON COLUMN announcements.title IS '公告标题';
COMMENT ON COLUMN announcements.content IS '公告内容';
COMMENT ON COLUMN announcements.link_text IS '链接文本';
COMMENT ON COLUMN announcements.link_url IS '链接 URL';
COMMENT ON COLUMN announcements.has_external_link IS '是否外部链接';
COMMENT ON COLUMN announcements.has_closable IS '是否允许关闭';
COMMENT ON COLUMN announcements.has_enabled IS '是否启用';
COMMENT ON COLUMN announcements.start_time IS '开始展示时间';
COMMENT ON COLUMN announcements.end_time IS '结束展示时间';
COMMENT ON COLUMN announcements.sort_order IS '排序顺序';
COMMENT ON COLUMN announcements.version IS '乐观锁版本号';
COMMENT ON COLUMN announcements.created_at IS '创建时间';
COMMENT ON COLUMN announcements.updated_at IS '最后更新时间';
COMMENT ON COLUMN announcements.deleted_at IS '软删除时间戳';

-- ============================================================
-- 7. Device 模块（设备）
-- ============================================================

-- device_categories: 设备分类表
-- 存储设备分类，如电脑、手机、网络设备等
CREATE TABLE device_categories (
    id                  UUID PRIMARY KEY,                                     -- ID
    name                VARCHAR(100) NOT NULL UNIQUE,                         -- 分类名称，全局唯一
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序，数值越小越靠前
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE device_categories IS '设备分类表';
COMMENT ON COLUMN device_categories.id IS 'ID';
COMMENT ON COLUMN device_categories.name IS '分类名称';
COMMENT ON COLUMN device_categories.sort_order IS '排序顺序';
COMMENT ON COLUMN device_categories.version IS '乐观锁版本号';
COMMENT ON COLUMN device_categories.created_at IS '创建时间';
COMMENT ON COLUMN device_categories.updated_at IS '最后更新时间';
COMMENT ON COLUMN device_categories.deleted_at IS '软删除时间戳';

-- devices: 设备表
-- 存储设备信息，支持规格参数 JSONB 灵活存储
CREATE TABLE devices (
    id                  UUID PRIMARY KEY,                                     -- ID
    category_id         UUID NOT NULL,                                        -- 所属分类 ID，对应 device_categories.id
    name                VARCHAR(100) NOT NULL,                                -- 设备名称
    image               VARCHAR(512),                                         -- 设备图片地址
    specs               JSONB,                                                -- 设备规格参数，JSONB 格式
    description         TEXT,                                                 -- 设备描述
    link                VARCHAR(512),                                         -- 设备外部链接（购买链接或官网）
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序，数值越小越靠前
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE,                             -- 软删除时间戳
    UNIQUE (category_id, name)                                                -- 联合唯一约束：同一分类下设备名称唯一
);

COMMENT ON TABLE devices IS '设备表';
COMMENT ON COLUMN devices.id IS 'ID';
COMMENT ON COLUMN devices.category_id IS '所属分类 ID，对应 device_categories.id';
COMMENT ON COLUMN devices.name IS '设备名称';
COMMENT ON COLUMN devices.image IS '设备图片';
COMMENT ON COLUMN devices.specs IS '设备规格参数';
COMMENT ON COLUMN devices.description IS '设备描述';
COMMENT ON COLUMN devices.link IS '设备外部链接';
COMMENT ON COLUMN devices.sort_order IS '排序顺序';
COMMENT ON COLUMN devices.version IS '乐观锁版本号';
COMMENT ON COLUMN devices.created_at IS '创建时间';
COMMENT ON COLUMN devices.updated_at IS '最后更新时间';
COMMENT ON COLUMN devices.deleted_at IS '软删除时间戳';

-- ============================================================
-- 8. System 模块（站点配置、自定义页面、文件、导航、横幅、主题）
-- ============================================================

-- site_configs: 站点配置表
-- 存储站点全局配置，支持嵌套 JSONB 结构
CREATE TABLE site_configs (
    id                  UUID PRIMARY KEY,                                     -- ID
    config_key          VARCHAR(100) NOT NULL UNIQUE,                         -- 配置项键名，全局唯一。如 "site_meta", "appearance"
    config_value        JSONB NOT NULL DEFAULT '{}',                          -- 配置项值，JSONB 格式，支持嵌套对象和数组
    description         VARCHAR(255),                                         -- 配置项说明
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 最后更新时间
);

COMMENT ON TABLE site_configs IS '站点配置表';
COMMENT ON COLUMN site_configs.id IS 'ID';
COMMENT ON COLUMN site_configs.config_key IS '配置项键名';
COMMENT ON COLUMN site_configs.config_value IS '配置项值';
COMMENT ON COLUMN site_configs.description IS '配置项说明';
COMMENT ON COLUMN site_configs.version IS '乐观锁版本号';
COMMENT ON COLUMN site_configs.created_at IS '创建时间';
COMMENT ON COLUMN site_configs.updated_at IS '最后更新时间';

-- custom_pages: 自定义页面表
-- 存储用户自定义的独立页面，包含 About 页面
CREATE TABLE custom_pages (
    id                  UUID PRIMARY KEY,                                     -- ID
    page_key            VARCHAR(100) NOT NULL UNIQUE,                         -- 页面唯一标识（URL 路径），如 "about", "disclaimer"
    title               VARCHAR(255) NOT NULL,                                -- 页面标题
    content             TEXT NOT NULL,                                        -- 页面内容（Markdown/HTML）
    description         VARCHAR(500),                                         -- 页面描述/SEO 摘要
    cover_image         VARCHAR(512),                                         -- 封面图片
    has_comment_enabled BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否允许评论
    has_enabled         BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE custom_pages IS '自定义页面表';
COMMENT ON COLUMN custom_pages.id IS 'ID';
COMMENT ON COLUMN custom_pages.page_key IS '页面唯一标识';
COMMENT ON COLUMN custom_pages.title IS '页面标题';
COMMENT ON COLUMN custom_pages.content IS '页面内容';
COMMENT ON COLUMN custom_pages.description IS '页面描述';
COMMENT ON COLUMN custom_pages.cover_image IS '封面图片';
COMMENT ON COLUMN custom_pages.has_comment_enabled IS '是否允许评论';
COMMENT ON COLUMN custom_pages.has_enabled IS '是否启用';
COMMENT ON COLUMN custom_pages.version IS '乐观锁版本号';
COMMENT ON COLUMN custom_pages.created_at IS '创建时间';
COMMENT ON COLUMN custom_pages.updated_at IS '最后更新时间';
COMMENT ON COLUMN custom_pages.deleted_at IS '软删除时间戳';

-- uploaded_files: 文件上传记录表
-- 存储文件上传记录，支持图片元数据和缩略图
CREATE TABLE uploaded_files (
    id                  UUID PRIMARY KEY,                                     -- ID
    original_name       VARCHAR(255) NOT NULL,                                -- 原始文件名
    stored_name         VARCHAR(255) NOT NULL,                                -- 存储文件名（通常包含 UUID 或哈希）
    storage_path        VARCHAR(512) NOT NULL,                                -- 存储路径
    url                 VARCHAR(512) NOT NULL,                                -- 可访问 URL
    folder              VARCHAR(50) NOT NULL,                                 -- 目标目录类型，如 "posts", "diary", "albums"
    file_size           BIGINT,                                               -- 文件大小（字节）
    mime_type           VARCHAR(50),                                          -- MIME 类型
    width               INTEGER,                                              -- 图片宽度（仅图片类型）
    height              INTEGER,                                              -- 图片高度（仅图片类型）
    thumbnail_url       VARCHAR(512),                                         -- 缩略图 URL
    metadata            JSONB DEFAULT '{}',                                   -- 额外元数据，JSONB 格式
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE uploaded_files IS '文件上传记录表';
COMMENT ON COLUMN uploaded_files.id IS 'ID';
COMMENT ON COLUMN uploaded_files.original_name IS '原始文件名';
COMMENT ON COLUMN uploaded_files.stored_name IS '存储文件名';
COMMENT ON COLUMN uploaded_files.storage_path IS '存储路径';
COMMENT ON COLUMN uploaded_files.url IS '可访问 URL';
COMMENT ON COLUMN uploaded_files.folder IS '目标目录类型';
COMMENT ON COLUMN uploaded_files.file_size IS '文件大小';
COMMENT ON COLUMN uploaded_files.mime_type IS 'MIME 类型';
COMMENT ON COLUMN uploaded_files.width IS '图片宽度';
COMMENT ON COLUMN uploaded_files.height IS '图片高度';
COMMENT ON COLUMN uploaded_files.thumbnail_url IS '缩略图 URL';
COMMENT ON COLUMN uploaded_files.metadata IS '额外元数据';
COMMENT ON COLUMN uploaded_files.version IS '乐观锁版本号';
COMMENT ON COLUMN uploaded_files.created_at IS '创建时间';
COMMENT ON COLUMN uploaded_files.updated_at IS '最后更新时间';
COMMENT ON COLUMN uploaded_files.deleted_at IS '软删除时间戳';

-- nav_links: 导航链接表
-- 存储顶部导航栏和页脚导航链接
CREATE TABLE nav_links (
    id                  UUID PRIMARY KEY,                                     -- ID
    name                VARCHAR(100) NOT NULL,                                -- 链接名称
    url                 VARCHAR(512) NOT NULL,                                -- 链接 URL
    icon                VARCHAR(100),                                         -- 图标名称（Iconify）
    has_external        BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否外部链接
    has_new_window      BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否在新窗口打开
    parent_id           UUID,                                                 -- 父链接 ID，NULL 表示顶级链接，对应 nav_links.id
    position            VARCHAR(100) NOT NULL DEFAULT 'navbar',               -- 链接位置。可选值：navbar(顶部导航)、footer(页脚)、drawer(抽屉菜单)
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序
    has_enabled         BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE nav_links IS '导航链接表';
COMMENT ON COLUMN nav_links.id IS 'ID';
COMMENT ON COLUMN nav_links.name IS '链接名称';
COMMENT ON COLUMN nav_links.url IS '链接 URL';
COMMENT ON COLUMN nav_links.icon IS '图标名称';
COMMENT ON COLUMN nav_links.has_external IS '是否外部链接';
COMMENT ON COLUMN nav_links.has_new_window IS '是否新窗口打开';
COMMENT ON COLUMN nav_links.parent_id IS '父链接 ID，对应 nav_links.id';
COMMENT ON COLUMN nav_links.position IS '链接位置';
COMMENT ON COLUMN nav_links.sort_order IS '排序顺序';
COMMENT ON COLUMN nav_links.has_enabled IS '是否启用';
COMMENT ON COLUMN nav_links.version IS '乐观锁版本号';
COMMENT ON COLUMN nav_links.created_at IS '创建时间';
COMMENT ON COLUMN nav_links.updated_at IS '最后更新时间';
COMMENT ON COLUMN nav_links.deleted_at IS '软删除时间戳';

-- banners: 横幅图片表
-- 存储首页横幅和全屏壁纸图片，支持桌面端/移动端分别配置
CREATE TABLE banners (
    id                  UUID PRIMARY KEY,                                     -- ID
    title               VARCHAR(255),                                         -- 图片标题/描述
    image_url           VARCHAR(512) NOT NULL,                                -- 图片访问 URL
    device_type         VARCHAR(100) NOT NULL DEFAULT 'both',                 -- 适用设备类型。可选值：desktop(桌面端)、mobile(移动端)、both(通用)
    position            VARCHAR(100) NOT NULL DEFAULT 'banner',               -- 展示位置。可选值：banner(顶部横幅)、wallpaper(全屏壁纸)
    sort_order          INTEGER NOT NULL DEFAULT 0,                           -- 排序顺序
    has_carousel        BOOLEAN NOT NULL DEFAULT FALSE,                       -- 是否启用轮播
    has_enabled         BOOLEAN NOT NULL DEFAULT TRUE,                        -- 是否启用
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 最后更新时间
    deleted_at          TIMESTAMP WITH TIME ZONE                              -- 软删除时间戳
);

COMMENT ON TABLE banners IS '横幅图片表';
COMMENT ON COLUMN banners.id IS 'ID';
COMMENT ON COLUMN banners.title IS '图片标题';
COMMENT ON COLUMN banners.image_url IS '图片 URL';
COMMENT ON COLUMN banners.device_type IS '适用设备类型';
COMMENT ON COLUMN banners.position IS '展示位置';
COMMENT ON COLUMN banners.sort_order IS '排序顺序';
COMMENT ON COLUMN banners.has_carousel IS '是否轮播';
COMMENT ON COLUMN banners.has_enabled IS '是否启用';
COMMENT ON COLUMN banners.version IS '乐观锁版本号';
COMMENT ON COLUMN banners.created_at IS '创建时间';
COMMENT ON COLUMN banners.updated_at IS '最后更新时间';
COMMENT ON COLUMN banners.deleted_at IS '软删除时间戳';

-- theme_settings: 主题设置表
-- 存储用户自定义的主题配置，如主题色、字体、布局偏好等
CREATE TABLE theme_settings (
    id                  UUID PRIMARY KEY,                                     -- ID
    setting_key         VARCHAR(100) NOT NULL UNIQUE,                         -- 设置项键名，全局唯一
    setting_value       JSONB NOT NULL DEFAULT '{}',                          -- 设置项值，JSONB 格式
    description         VARCHAR(255),                                         -- 设置项说明
    has_user_customizable BOOLEAN NOT NULL DEFAULT TRUE,                      -- 是否允许用户自定义
    version             INTEGER NOT NULL DEFAULT 0,                           -- 乐观锁版本号
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 最后更新时间
);

COMMENT ON TABLE theme_settings IS '主题设置表';
COMMENT ON COLUMN theme_settings.id IS 'ID';
COMMENT ON COLUMN theme_settings.setting_key IS '设置项键名';
COMMENT ON COLUMN theme_settings.setting_value IS '设置项值';
COMMENT ON COLUMN theme_settings.description IS '设置项说明';
COMMENT ON COLUMN theme_settings.has_user_customizable IS '是否允许用户自定义';
COMMENT ON COLUMN theme_settings.version IS '乐观锁版本号';
COMMENT ON COLUMN theme_settings.created_at IS '创建时间';
COMMENT ON COLUMN theme_settings.updated_at IS '最后更新时间';

-- ============================================================
-- 索引创建
-- ============================================================

-- Posts 索引
CREATE INDEX idx_posts_pinned_priority ON posts(has_pinned DESC, priority ASC);        -- 置顶文章排序索引
CREATE INDEX idx_posts_published_at ON posts(published_at DESC);                       -- 发布时间倒序索引
CREATE INDEX idx_posts_draft ON posts(has_draft);                                      -- 草稿状态索引
CREATE INDEX idx_posts_category ON posts(category);                                    -- 分类索引
CREATE INDEX idx_posts_lang ON posts(lang);                                            -- 语言索引
CREATE INDEX idx_posts_encrypted ON posts(has_encrypted);                              -- 加密状态索引
CREATE INDEX idx_posts_slug ON posts(slug);                                            -- slug 索引
CREATE INDEX idx_posts_alias ON posts(alias) WHERE alias IS NOT NULL;                  -- 别名索引（部分）
CREATE INDEX idx_posts_permalink ON posts(permalink) WHERE permalink IS NOT NULL;      -- 固定链接索引（部分）
CREATE INDEX idx_posts_deleted_at ON posts(deleted_at) WHERE deleted_at IS NULL;       -- 软删除过滤索引（部分）
CREATE INDEX idx_posts_search ON posts USING gin(to_tsvector('simple', title || ' ' || COALESCE(content, ''))); -- 全文搜索索引（GIN）

-- Tags 索引
CREATE INDEX idx_tags_name ON tags(name);                                              -- 标签名称索引

-- Post tags 索引
CREATE INDEX idx_post_tags_post_id ON post_tags(post_id);                              -- 文章 ID 索引
CREATE INDEX idx_post_tags_tag_id ON post_tags(tag_id);                                -- 标签 ID 索引

-- Diary 索引
CREATE INDEX idx_diary_entry_date ON diary_entries(entry_date DESC);                   -- 日记时间倒序索引
CREATE INDEX idx_diary_deleted_at ON diary_entries(deleted_at) WHERE deleted_at IS NULL; -- 软删除过滤索引（部分）

-- Friends 索引
CREATE INDEX idx_friends_sort_order ON friends(sort_order ASC);                        -- 排序索引
CREATE INDEX idx_friends_active ON friends(has_active) WHERE deleted_at IS NULL;       -- 启用状态过滤索引（部分）
CREATE INDEX idx_friends_deleted_at ON friends(deleted_at) WHERE deleted_at IS NULL;   -- 软删除过滤索引（部分）

-- Friend tags 索引
CREATE INDEX idx_friend_tag_links_friend_id ON friend_tag_links(friend_id);            -- 友链 ID 索引
CREATE INDEX idx_friend_tag_links_tag_id ON friend_tag_links(tag_id);                  -- 标签 ID 索引

-- Projects 索引
CREATE INDEX idx_projects_category ON projects(category);                              -- 分类索引
CREATE INDEX idx_projects_status ON projects(status);                                  -- 状态索引
CREATE INDEX idx_projects_featured ON projects(has_featured);                          -- 精选索引
CREATE INDEX idx_projects_sort ON projects(sort_order ASC, created_at DESC);           -- 排序+时间组合索引
CREATE INDEX idx_projects_category_sort ON projects(category, sort_order ASC);         -- 分类排序索引
CREATE INDEX idx_projects_featured_sort ON projects(has_featured DESC, sort_order ASC); -- 精选排序索引
CREATE INDEX idx_projects_deleted_at ON projects(deleted_at) WHERE deleted_at IS NULL;  -- 软删除过滤索引（部分）

-- Project tech stacks / tags 索引
CREATE INDEX idx_project_tech_project_id ON project_tech_stacks(project_id);           -- 项目 ID 索引
CREATE INDEX idx_project_tech_name ON project_tech_stacks(tech_name);                  -- 技术名称索引
CREATE INDEX idx_project_tags_project_id ON project_tags(project_id);                  -- 项目 ID 索引
CREATE INDEX idx_project_tag_name ON project_tags(tag_name);                           -- 标签名称索引

-- Timeline 索引
CREATE INDEX idx_timeline_start_date ON timeline_events(start_date DESC);              -- 开始时间倒序索引
CREATE INDEX idx_timeline_type ON timeline_events(event_type);                         -- 事件类型索引
CREATE INDEX idx_timeline_featured ON timeline_events(has_featured);                   -- 精选索引
CREATE INDEX idx_timeline_type_start_date ON timeline_events(event_type, start_date DESC); -- 类型+时间组合索引
CREATE INDEX idx_timeline_featured_start_date ON timeline_events(has_featured DESC, start_date DESC); -- 精选+时间组合索引
CREATE INDEX idx_timeline_deleted_at ON timeline_events(deleted_at) WHERE deleted_at IS NULL; -- 软删除过滤索引（部分）

-- Timeline skills / achievements / links 索引
CREATE INDEX idx_timeline_skills_event_id ON timeline_event_skills(timeline_event_id); -- 事件 ID 索引
CREATE INDEX idx_timeline_skill_name ON timeline_event_skills(skill_name);             -- 技能名称索引
CREATE INDEX idx_timeline_achievements_event_id ON timeline_event_achievements(timeline_event_id); -- 事件 ID 索引
CREATE INDEX idx_timeline_links_event_id ON timeline_event_links(timeline_event_id);   -- 事件 ID 索引

-- Skills 索引
CREATE INDEX idx_skills_category ON skills(category);                                  -- 分类索引
CREATE INDEX idx_skills_level ON skills(level);                                        -- 等级索引
CREATE INDEX idx_skills_category_level ON skills(category, level);                     -- 分类+等级组合索引
CREATE INDEX idx_skills_deleted_at ON skills(deleted_at) WHERE deleted_at IS NULL;     -- 软删除过滤索引（部分）

-- Devices 索引
CREATE INDEX idx_devices_category_id ON devices(category_id);                          -- 分类 ID 索引
CREATE INDEX idx_devices_deleted_at ON devices(deleted_at) WHERE deleted_at IS NULL;   -- 软删除过滤索引（部分）

-- Device categories 索引
CREATE INDEX idx_device_categories_sort ON device_categories(sort_order ASC);          -- 排序索引

-- Albums 索引
CREATE INDEX idx_albums_date ON albums(album_date DESC);                               -- 相册时间倒序索引
CREATE INDEX idx_albums_deleted_at ON albums(deleted_at) WHERE deleted_at IS NULL;     -- 软删除过滤索引（部分）

-- Album photos 索引
CREATE INDEX idx_album_photos_album_id ON album_photos(album_id);                      -- 相册 ID 索引
CREATE INDEX idx_album_photos_cover ON album_photos(album_id, has_cover) WHERE has_cover = TRUE; -- 封面过滤索引（部分）

-- Anime 索引
CREATE INDEX idx_anime_status ON anime(status);                                        -- 状态索引
CREATE INDEX idx_anime_rating ON anime(rating DESC);                                   -- 评分倒序索引
CREATE INDEX idx_anime_year ON anime(year);                                            -- 年份索引
CREATE INDEX idx_anime_sort_order ON anime(sort_order ASC);                            -- 排序索引
CREATE INDEX idx_anime_deleted_at ON anime(deleted_at) WHERE deleted_at IS NULL;       -- 软删除过滤索引（部分）

-- Music playlists 索引
CREATE INDEX idx_music_playlists_sort ON music_playlists(sort_order ASC);              -- 排序索引
CREATE INDEX idx_music_playlists_enabled ON music_playlists(has_enabled) WHERE deleted_at IS NULL; -- 启用状态过滤索引（部分）

-- Music tracks 索引
CREATE INDEX idx_music_tracks_playlist_id ON music_tracks(playlist_id);                -- 播放列表 ID 索引
CREATE INDEX idx_music_tracks_sort ON music_tracks(sort_order ASC);                    -- 排序索引

-- Announcements 索引
CREATE INDEX idx_announcements_enabled ON announcements(has_enabled) WHERE deleted_at IS NULL; -- 启用状态过滤索引（部分）
CREATE INDEX idx_announcements_time ON announcements(start_time, end_time);            -- 时间范围索引

-- Comments 索引
CREATE INDEX idx_comments_target ON comments(target_type, target_id);                  -- 目标类型+ID 组合索引
CREATE INDEX idx_comments_parent_id ON comments(parent_id) WHERE parent_id IS NOT NULL; -- 父评论索引（部分）
CREATE INDEX idx_comments_approved ON comments(has_approved);                          -- 审核状态索引
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);                     -- 创建时间倒序索引

-- Page views 索引
CREATE INDEX idx_page_views_page_path ON page_views(page_path);                        -- 页面路径索引
CREATE INDEX idx_page_views_visited_at ON page_views(visited_at DESC);                 -- 访问时间倒序索引
CREATE INDEX idx_page_views_ip ON page_views(ip_address, visited_at);                  -- IP+时间组合索引

-- Daily stats 索引
CREATE INDEX idx_daily_stats_date ON daily_stats(stat_date DESC);                      -- 统计日期倒序索引

-- Archives 索引
CREATE INDEX idx_archives_year_month ON archives(year DESC, month DESC);               -- 年月倒序索引

-- Categories 索引
CREATE INDEX idx_categories_parent ON categories(parent_id) WHERE parent_id IS NOT NULL; -- 父分类索引（部分）
CREATE INDEX idx_categories_sort ON categories(sort_order ASC);                        -- 排序索引
CREATE INDEX idx_categories_enabled ON categories(has_enabled) WHERE deleted_at IS NULL; -- 启用状态过滤索引（部分）

-- Search logs 索引
CREATE INDEX idx_search_logs_keyword ON search_logs(keyword);                          -- 关键词索引
CREATE INDEX idx_search_logs_searched_at ON search_logs(searched_at DESC);             -- 搜索时间倒序索引

-- Banners 索引
CREATE INDEX idx_banners_position ON banners(position, device_type);                   -- 位置+设备类型组合索引
CREATE INDEX idx_banners_sort ON banners(sort_order ASC);                              -- 排序索引
CREATE INDEX idx_banners_enabled ON banners(has_enabled) WHERE deleted_at IS NULL;     -- 启用状态过滤索引（部分）

-- Custom pages 索引
CREATE INDEX idx_custom_pages_key ON custom_pages(page_key);                           -- 页面标识索引
CREATE INDEX idx_custom_pages_enabled ON custom_pages(has_enabled) WHERE deleted_at IS NULL; -- 启用状态过滤索引（部分）

-- Theme settings 索引
CREATE INDEX idx_theme_settings_key ON theme_settings(setting_key);                    -- 设置项键名索引

-- Site configs 索引
CREATE INDEX idx_site_configs_key ON site_configs(config_key);                         -- 配置项键名索引

-- Nav links 索引
CREATE INDEX idx_nav_links_position ON nav_links(position, sort_order ASC);            -- 位置+排序组合索引
CREATE INDEX idx_nav_links_parent ON nav_links(parent_id) WHERE parent_id IS NOT NULL; -- 父链接索引（部分）

-- Uploaded files 索引
CREATE INDEX idx_uploaded_files_folder ON uploaded_files(folder);                      -- 文件夹索引
CREATE INDEX idx_uploaded_files_deleted_at ON uploaded_files(deleted_at) WHERE deleted_at IS NULL; -- 软删除过滤索引（部分）
