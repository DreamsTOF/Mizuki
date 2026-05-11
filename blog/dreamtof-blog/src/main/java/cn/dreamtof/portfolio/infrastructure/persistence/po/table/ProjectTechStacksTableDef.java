package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 项目技术栈关联表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class ProjectTechStacksTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目技术栈关联表
     */
    public static final ProjectTechStacksTableDef PROJECT_TECH_STACKS_PO = new ProjectTechStacksTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 技术名称
     */
    public final QueryColumn TECH_NAME = new QueryColumn(this, "tech_name");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PROJECT_ID, TECH_NAME, VERSION, CREATED_AT};

    public ProjectTechStacksTableDef() {
        super("public", "project_tech_stacks");
    }

    private ProjectTechStacksTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ProjectTechStacksTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ProjectTechStacksTableDef("public", "project_tech_stacks", alias));
    }

}
