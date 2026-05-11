-- ============================================================
-- Mizuki 博客系统 - Posts 表字段对齐前端 Content Collection
-- 前端 schema: posts.md/mdx frontmatter 字段
-- ============================================================

-- 1. has_draft → draft (前端字段: draft)
ALTER TABLE posts RENAME COLUMN has_draft TO draft;

-- 2. has_pinned → pinned (前端字段: pinned)
ALTER TABLE posts RENAME COLUMN has_pinned TO pinned;

-- 3. has_encrypted → encrypted (前端字段: encrypted)
ALTER TABLE posts RENAME COLUMN has_encrypted TO encrypted;

-- 4. password_hash → password (前端字段: password, 存储 bcrypt hash)
ALTER TABLE posts RENAME COLUMN password_hash TO password;

-- 5. cover_image → image (前端字段: image)
ALTER TABLE posts RENAME COLUMN cover_image TO image;

-- 6. has_comment_enabled → comment (前端字段: comment)
ALTER TABLE posts RENAME COLUMN has_comment_enabled TO comment;

-- 7. published_at → published (前端字段: published)
ALTER TABLE posts RENAME COLUMN published_at TO published;

-- 索引重建
DROP INDEX IF EXISTS idx_posts_pinned_priority;
CREATE INDEX idx_posts_pinned_priority ON posts(pinned DESC, priority ASC);
DROP INDEX IF EXISTS idx_posts_published_at;
CREATE INDEX idx_posts_published_at ON posts(published DESC);
DROP INDEX IF EXISTS idx_posts_draft;
CREATE INDEX idx_posts_draft ON posts(draft);
DROP INDEX IF EXISTS idx_posts_encrypted;
CREATE INDEX idx_posts_encrypted ON posts(encrypted);
DROP INDEX IF EXISTS idx_posts_search;
CREATE INDEX idx_posts_search ON posts USING gin(to_tsvector('simple', title || ' ' || COALESCE(content, '')));

-- COMMENT 更新
COMMENT ON COLUMN posts.draft IS '是否为草稿状态。TRUE=草稿，FALSE=已发布';
COMMENT ON COLUMN posts.pinned IS '是否置顶。TRUE=置顶，FALSE=普通';
COMMENT ON COLUMN posts.encrypted IS '是否加密。TRUE=需要密码访问，FALSE=公开访问';
COMMENT ON COLUMN posts.password IS '加密密码哈希（bcrypt），仅当 encrypted=TRUE 时有效';
COMMENT ON COLUMN posts.image IS '封面图片路径/URL';
COMMENT ON COLUMN posts.comment IS '是否启用评论。TRUE=允许评论，FALSE=关闭评论';
COMMENT ON COLUMN posts.published IS '发布日期时间（带时区）';
