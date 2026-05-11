package cn.dreamtof.media.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

public class AlbumsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final AlbumsTableDef ALBUMS_PO = new AlbumsTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn ALBUM_KEY = new QueryColumn(this, "album_key");
    public final QueryColumn TITLE = new QueryColumn(this, "title");
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");
    public final QueryColumn DATE = new QueryColumn(this, "date");
    public final QueryColumn LOCATION = new QueryColumn(this, "location");
    public final QueryColumn TAGS = new QueryColumn(this, "tags");
    public final QueryColumn LAYOUT = new QueryColumn(this, "layout");
    public final QueryColumn COLUMNS = new QueryColumn(this, "columns");
    public final QueryColumn COVER = new QueryColumn(this, "cover");
    public final QueryColumn VERSION = new QueryColumn(this, "version");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ALBUM_KEY, TITLE, DESCRIPTION, DATE, LOCATION, TAGS, LAYOUT, COLUMNS, COVER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public AlbumsTableDef() {
        super("public", "albums");
    }

    private AlbumsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public AlbumsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new AlbumsTableDef("public", "albums", alias));
    }
}
