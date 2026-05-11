package cn.dreamtof.content.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

public class DiaryEntriesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final DiaryEntriesTableDef DIARY_ENTRIES_PO = new DiaryEntriesTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CONTENT = new QueryColumn(this, "content");
    public final QueryColumn DATE = new QueryColumn(this, "date");
    public final QueryColumn IMAGES = new QueryColumn(this, "images");
    public final QueryColumn LOCATION = new QueryColumn(this, "location");
    public final QueryColumn MOOD = new QueryColumn(this, "mood");
    public final QueryColumn TAGS = new QueryColumn(this, "tags");
    public final QueryColumn VERSION = new QueryColumn(this, "version");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CONTENT, DATE, IMAGES, LOCATION, MOOD, TAGS, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public DiaryEntriesTableDef() {
        super("public", "diary_entries");
    }

    private DiaryEntriesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public DiaryEntriesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new DiaryEntriesTableDef("public", "diary_entries", alias));
    }
}
