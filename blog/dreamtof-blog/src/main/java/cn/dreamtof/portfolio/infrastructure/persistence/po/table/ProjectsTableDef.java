package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 项目表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class ProjectsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目表
     */
    public static final ProjectsTableDef PROJECTS_PO = new ProjectsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 封面图片路径
     */
    public final QueryColumn IMAGE = new QueryColumn(this, "image");

    /**
     * 项目标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 项目状态
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 结束日期
     */
    public final QueryColumn END_DATE = new QueryColumn(this, "end_date");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 项目类别
     */
    public final QueryColumn CATEGORY = new QueryColumn(this, "category");

    /**
     * 项目主页地址
     */
    public final QueryColumn VISIT_URL = new QueryColumn(this, "visit_url");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 排序权重
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 开始日期
     */
    public final QueryColumn START_DATE = new QueryColumn(this, "start_date");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 项目描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 是否精选
     */
    public final QueryColumn HAS_FEATURED = new QueryColumn(this, "has_featured");

    /**
     * 在线演示地址
     */
    public final QueryColumn LIVE_DEMO_URL = new QueryColumn(this, "live_demo_url");

    /**
     * 是否显示封面
     */
    public final QueryColumn HAS_SHOW_IMAGE = new QueryColumn(this, "has_show_image");

    /**
     * 源码仓库地址
     */
    public final QueryColumn SOURCE_CODE_URL = new QueryColumn(this, "source_code_url");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, DESCRIPTION, IMAGE, CATEGORY, STATUS, LIVE_DEMO_URL, SOURCE_CODE_URL, VISIT_URL, START_DATE, END_DATE, HAS_FEATURED, HAS_SHOW_IMAGE, SORT_ORDER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

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
