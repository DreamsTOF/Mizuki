package cn.dreamtof.social.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 友链表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class FriendsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 友链表
     */
    public static final FriendsTableDef FRIENDS_PO = new FriendsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 友链网站标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 头像/Logo 图片 URL
     */
    public final QueryColumn IMGURL = new QueryColumn(this, "imgurl");

    /**
     * 图片类型：0=外链，1=本地
     */
    public final QueryColumn IMG_TYPE = new QueryColumn(this, "img_type");

    /**
     * 网站链接
     */
    public final QueryColumn SITEURL = new QueryColumn(this, "siteurl");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 是否启用
     */
    public final QueryColumn HAS_ACTIVE = new QueryColumn(this, "has_active");

    /**
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 友链网站描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 本地存储路径
     */
    public final QueryColumn IMG_STORAGE_PATH = new QueryColumn(this, "img_storage_path");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, DESCRIPTION, SITEURL, IMGURL, IMG_TYPE, IMG_STORAGE_PATH, SORT_ORDER, HAS_ACTIVE, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public FriendsTableDef() {
        super("public", "friends");
    }

    private FriendsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public FriendsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new FriendsTableDef("public", "friends", alias));
    }

}
