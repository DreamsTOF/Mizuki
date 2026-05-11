package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 搜索记录表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class SearchLogsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 搜索记录表
     */
    public static final SearchLogsTableDef SEARCH_LOGS_PO = new SearchLogsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 搜索关键词
     */
    public final QueryColumn KEYWORD = new QueryColumn(this, "keyword");

    /**
     * 搜索者 IP
     */
    public final QueryColumn IP_ADDRESS = new QueryColumn(this, "ip_address");

    /**
     * 搜索者 User-Agent
     */
    public final QueryColumn USER_AGENT = new QueryColumn(this, "user_agent");

    /**
     * 搜索时间
     */
    public final QueryColumn SEARCHED_AT = new QueryColumn(this, "searched_at");

    /**
     * 搜索结果数量
     */
    public final QueryColumn RESULT_COUNT = new QueryColumn(this, "result_count");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, KEYWORD, RESULT_COUNT, IP_ADDRESS, USER_AGENT, SEARCHED_AT};

    public SearchLogsTableDef() {
        super("public", "search_logs");
    }

    private SearchLogsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SearchLogsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SearchLogsTableDef("public", "search_logs", alias));
    }

}
