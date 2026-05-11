package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 页面访问统计表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class PageViewsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页面访问统计表
     */
    public static final PageViewsTableDef PAGE_VIEWS_PO = new PageViewsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 来源页面
     */
    public final QueryColumn REFERER = new QueryColumn(this, "referer");

    /**
     * 访问页面路径
     */
    public final QueryColumn PAGE_PATH = new QueryColumn(this, "page_path");

    /**
     * 页面类型
     */
    public final QueryColumn PAGE_TYPE = new QueryColumn(this, "page_type");

    /**
     * 关联的文章/页面 ID
     */
    public final QueryColumn TARGET_ID = new QueryColumn(this, "target_id");

    /**
     * 访问者 IP
     */
    public final QueryColumn IP_ADDRESS = new QueryColumn(this, "ip_address");

    /**
     * 访问者 User-Agent
     */
    public final QueryColumn USER_AGENT = new QueryColumn(this, "user_agent");

    /**
     * 访问时间
     */
    public final QueryColumn VISITED_AT = new QueryColumn(this, "visited_at");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PAGE_PATH, PAGE_TYPE, TARGET_ID, IP_ADDRESS, USER_AGENT, REFERER, VISITED_AT};

    public PageViewsTableDef() {
        super("public", "page_views");
    }

    private PageViewsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public PageViewsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new PageViewsTableDef("public", "page_views", alias));
    }

}
