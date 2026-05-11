package cn.dreamtof.social.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 友链-标签关联表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class FriendTagLinksTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 友链-标签关联表
     */
    public static final FriendTagLinksTableDef FRIEND_TAG_LINKS_PO = new FriendTagLinksTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 关联的标签 ID，对应 friend_tags.id
     */
    public final QueryColumn TAG_ID = new QueryColumn(this, "tag_id");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 关联的友链 ID，对应 friends.id
     */
    public final QueryColumn FRIEND_ID = new QueryColumn(this, "friend_id");

    /**
     * 关联创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, FRIEND_ID, TAG_ID, VERSION, CREATED_AT};

    public FriendTagLinksTableDef() {
        super("public", "friend_tag_links");
    }

    private FriendTagLinksTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public FriendTagLinksTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new FriendTagLinksTableDef("public", "friend_tag_links", alias));
    }

}
