package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 导航链接表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class NavLinksTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 导航链接表
     */
    public static final NavLinksTableDef NAV_LINKS_PO = new NavLinksTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 链接 URL
     */
    public final QueryColumn URL = new QueryColumn(this, "url");

    /**
     * 图标名称
     */
    public final QueryColumn ICON = new QueryColumn(this, "icon");

    /**
     * 链接名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 父链接 ID，对应 nav_links.id
     */
    public final QueryColumn PARENT_ID = new QueryColumn(this, "parent_id");

    /**
     * 链接位置
     */
    public final QueryColumn POSITION = new QueryColumn(this, "position");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 是否启用
     */
    public final QueryColumn HAS_ENABLED = new QueryColumn(this, "has_enabled");

    /**
     * 是否外部链接
     */
    public final QueryColumn HAS_EXTERNAL = new QueryColumn(this, "has_external");

    /**
     * 是否新窗口打开
     */
    public final QueryColumn HAS_NEW_WINDOW = new QueryColumn(this, "has_new_window");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, URL, ICON, HAS_EXTERNAL, HAS_NEW_WINDOW, PARENT_ID, POSITION, SORT_ORDER, HAS_ENABLED, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public NavLinksTableDef() {
        super("public", "nav_links");
    }

    private NavLinksTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public NavLinksTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new NavLinksTableDef("public", "nav_links", alias));
    }

}
