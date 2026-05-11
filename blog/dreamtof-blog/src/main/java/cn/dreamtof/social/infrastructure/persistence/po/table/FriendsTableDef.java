package cn.dreamtof.social.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

public class FriendsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final FriendsTableDef FRIENDS_PO = new FriendsTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn TITLE = new QueryColumn(this, "title");
    public final QueryColumn DESC = new QueryColumn(this, "desc");
    public final QueryColumn SITEURL = new QueryColumn(this, "siteurl");
    public final QueryColumn IMGURL = new QueryColumn(this, "imgurl");
    public final QueryColumn IMG_TYPE = new QueryColumn(this, "img_type");
    public final QueryColumn IMG_STORAGE_PATH = new QueryColumn(this, "img_storage_path");
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");
    public final QueryColumn HAS_ACTIVE = new QueryColumn(this, "has_active");
    public final QueryColumn VERSION = new QueryColumn(this, "version");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, DESC, SITEURL, IMGURL, IMG_TYPE, IMG_STORAGE_PATH, SORT_ORDER, HAS_ACTIVE, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

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
