package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 项目标签关联表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class ProjectTagsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目标签关联表
     */
    public static final ProjectTagsTableDef PROJECT_TAGS_PO = new ProjectTagsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 标签名称
     */
    public final QueryColumn TAG_NAME = new QueryColumn(this, "tag_name");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 关联创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 关联的项目 ID，对应 projects.id
     */
    public final QueryColumn PROJECT_ID = new QueryColumn(this, "project_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PROJECT_ID, TAG_NAME, VERSION, CREATED_AT};

    public ProjectTagsTableDef() {
        super("public", "project_tags");
    }

    private ProjectTagsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ProjectTagsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ProjectTagsTableDef("public", "project_tags", alias));
    }

}
