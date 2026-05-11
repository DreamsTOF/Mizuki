package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

public class TimelineEventsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final TimelineEventsTableDef TIMELINE_EVENTS_PO = new TimelineEventsTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn TITLE = new QueryColumn(this, "title");
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");
    public final QueryColumn TYPE = new QueryColumn(this, "type");
    public final QueryColumn ICON = new QueryColumn(this, "icon");
    public final QueryColumn COLOR = new QueryColumn(this, "color");
    public final QueryColumn START_DATE = new QueryColumn(this, "start_date");
    public final QueryColumn END_DATE = new QueryColumn(this, "end_date");
    public final QueryColumn LOCATION = new QueryColumn(this, "location");
    public final QueryColumn ORGANIZATION = new QueryColumn(this, "organization");
    public final QueryColumn POSITION = new QueryColumn(this, "position");
    public final QueryColumn FEATURED = new QueryColumn(this, "featured");
    public final QueryColumn VERSION = new QueryColumn(this, "version");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, DESCRIPTION, TYPE, ICON, COLOR, START_DATE, END_DATE, LOCATION, ORGANIZATION, POSITION, FEATURED, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public TimelineEventsTableDef() {
        super("public", "timeline_events");
    }

    private TimelineEventsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public TimelineEventsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new TimelineEventsTableDef("public", "timeline_events", alias));
    }
}
