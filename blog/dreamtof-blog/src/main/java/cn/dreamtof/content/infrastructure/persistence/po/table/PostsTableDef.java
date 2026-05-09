package cn.dreamtof.content.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 文章主表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class PostsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章主表
     */
    public static final PostsTableDef POSTS_PO = new PostsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 语言代码
     */
    public final QueryColumn LANG = new QueryColumn(this, "lang");

    /**
     * URL 友好标识符
     */
    public final QueryColumn SLUG = new QueryColumn(this, "slug");

    /**
     * 文章别名
     */
    public final QueryColumn ALIAS = new QueryColumn(this, "alias");

    /**
     * 文章标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 作者名
     */
    public final QueryColumn AUTHOR = new QueryColumn(this, "author");

    /**
     * Markdown 正文内容
     */
    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    /**
     * 自动生成的摘要
     */
    public final QueryColumn EXCERPT = new QueryColumn(this, "excerpt");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 分类名称
     */
    public final QueryColumn CATEGORY = new QueryColumn(this, "category");

    /**
     * 是否为草稿状态
     */
    public final QueryColumn HAS_DRAFT = new QueryColumn(this, "has_draft");

    /**
     * 置顶优先级
     */
    public final QueryColumn PRIORITY = new QueryColumn(this, "priority");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 是否置顶
     */
    public final QueryColumn HAS_PINNED = new QueryColumn(this, "has_pinned");

    /**
     * 自定义固定链接
     */
    public final QueryColumn PERMALINK = new QueryColumn(this, "permalink");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 浏览次数
     */
    public final QueryColumn VIEW_COUNT = new QueryColumn(this, "view_count");

    /**
     * 字数统计
     */
    public final QueryColumn WORD_COUNT = new QueryColumn(this, "word_count");

    /**
     * 封面图片路径
     */
    public final QueryColumn COVER_IMAGE = new QueryColumn(this, "cover_image");

    /**
     * 许可证链接
     */
    public final QueryColumn LICENSE_URL = new QueryColumn(this, "license_url");

    /**
     * 原文链接
     */
    public final QueryColumn SOURCE_LINK = new QueryColumn(this, "source_link");

    /**
     * 文章描述/SEO 摘要
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 许可证名称
     */
    public final QueryColumn LICENSE_NAME = new QueryColumn(this, "license_name");

    /**
     * 发布日期时间
     */
    public final QueryColumn PUBLISHED_AT = new QueryColumn(this, "published_at");

    /**
     * 是否加密
     */
    public final QueryColumn HAS_ENCRYPTED = new QueryColumn(this, "has_encrypted");

    /**
     * 加密密码哈希
     */
    public final QueryColumn PASSWORD_HASH = new QueryColumn(this, "password_hash");

    /**
     * 密码提示信息
     */
    public final QueryColumn PASSWORD_HINT = new QueryColumn(this, "password_hint");

    /**
     * 是否启用评论
     */
    public final QueryColumn HAS_COMMENT_ENABLED = new QueryColumn(this, "has_comment_enabled");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, SLUG, CONTENT, EXCERPT, DESCRIPTION, AUTHOR, CATEGORY, LANG, HAS_DRAFT, HAS_PINNED, PRIORITY, HAS_ENCRYPTED, PASSWORD_HASH, PASSWORD_HINT, ALIAS, PERMALINK, LICENSE_NAME, LICENSE_URL, SOURCE_LINK, COVER_IMAGE, HAS_COMMENT_ENABLED, PUBLISHED_AT, VIEW_COUNT, WORD_COUNT, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public PostsTableDef() {
        super("public", "posts");
    }

    private PostsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public PostsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new PostsTableDef("public", "posts", alias));
    }

}
