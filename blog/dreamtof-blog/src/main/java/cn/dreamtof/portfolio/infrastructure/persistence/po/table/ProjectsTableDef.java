package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

public class ProjectsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final ProjectsTableDef PROJECTS_PO = new ProjectsTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn TITLE = new QueryColumn(this, "title");
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");
    public final QueryColumn IMAGE = new QueryColumn(this, "image");
    public final QueryColumn CATEGORY = new QueryColumn(this, "category");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn LIVE_DEMO = new QueryColumn(this, "live_demo");
    public final QueryColumn SOURCE_CODE = new QueryColumn(this, "source_code");
    public final QueryColumn VISIT_URL = new QueryColumn(this, "visit_url");
    public final QueryColumn START_DATE = new QueryColumn(this, "start_date");
    public final QueryColumn END_DATE = new QueryColumn(this, "end_date");
    public final QueryColumn FEATURED = new QueryColumn(this, "featured");
    public final QueryColumn SHOW_IMAGE = new QueryColumn(this, "show_image");
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");
    public final QueryColumn VERSION = new QueryColumn(this, "version");
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, DESCRIPTION, IMAGE, CATEGORY, STATUS, LIVE_DEMO, SOURCE_CODE, VISIT_URL, START_DATE, END_DATE, FEATURED, SHOW_IMAGE, SORT_ORDER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public ProjectsTableDef() {
        super("public", "projects");
    }

    private ProjectsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ProjectsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ProjectsTableDef("public", "projects", alias));
    }
}
