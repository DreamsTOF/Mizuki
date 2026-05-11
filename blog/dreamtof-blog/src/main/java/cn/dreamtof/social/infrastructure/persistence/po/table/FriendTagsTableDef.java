package cn.dreamtof.social.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 友链标签表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class FriendTagsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 友链标签表
     */
    public static final FriendTagsTableDef FRIEND_TAGS_PO = new FriendTagsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 标签名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, VERSION, CREATED_AT, UPDATED_AT};

    public FriendTagsTableDef() {
        super("public", "friend_tags");
    }

    private FriendTagsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public FriendTagsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new FriendTagsTableDef("public", "friend_tags", alias));
    }

}
