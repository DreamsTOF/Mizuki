package cn.dreamtof.social.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 评论表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class CommentsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论表
     */
    public static final CommentsTableDef COMMENTS_PO = new CommentsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 评论内容
     */
    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 父评论 ID，对应 comments.id
     */
    public final QueryColumn PARENT_ID = new QueryColumn(this, "parent_id");

    /**
     * 评论目标 ID
     */
    public final QueryColumn TARGET_ID = new QueryColumn(this, "target_id");

    /**
     * 评论者网站
     */
    public final QueryColumn AUTHOR_URL = new QueryColumn(this, "author_url");

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
     * 评论者 IP
     */
    public final QueryColumn IP_ADDRESS = new QueryColumn(this, "ip_address");

    /**
     * 点赞数
     */
    public final QueryColumn LIKE_COUNT = new QueryColumn(this, "like_count");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 评论者 User-Agent
     */
    public final QueryColumn USER_AGENT = new QueryColumn(this, "user_agent");

    /**
     * 评论者昵称
     */
    public final QueryColumn AUTHOR_NAME = new QueryColumn(this, "author_name");

    /**
     * 评论目标类型
     */
    public final QueryColumn TARGET_TYPE = new QueryColumn(this, "target_type");

    /**
     * 评论者邮箱
     */
    public final QueryColumn AUTHOR_EMAIL = new QueryColumn(this, "author_email");

    /**
     * 是否审核通过
     */
    public final QueryColumn HAS_APPROVED = new QueryColumn(this, "has_approved");

    /**
     * 评论者头像
     */
    public final QueryColumn AUTHOR_AVATAR = new QueryColumn(this, "author_avatar");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TARGET_TYPE, TARGET_ID, PARENT_ID, AUTHOR_NAME, AUTHOR_EMAIL, AUTHOR_URL, AUTHOR_AVATAR, CONTENT, IP_ADDRESS, USER_AGENT, HAS_APPROVED, HAS_PINNED, LIKE_COUNT, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public CommentsTableDef() {
        super("public", "comments");
    }

    private CommentsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public CommentsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new CommentsTableDef("public", "comments", alias));
    }

}
