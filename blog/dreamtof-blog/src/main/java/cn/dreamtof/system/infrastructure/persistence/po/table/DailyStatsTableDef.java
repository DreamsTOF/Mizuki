package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 每日统计汇总表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class DailyStatsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 每日统计汇总表
     */
    public static final DailyStatsTableDef DAILY_STATS_PO = new DailyStatsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 统计日期
     */
    public final QueryColumn STAT_DATE = new QueryColumn(this, "stat_date");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 页面浏览量（PV）
     */
    public final QueryColumn PAGE_VIEWS = new QueryColumn(this, "page_views");

    /**
     * 文章阅读量
     */
    public final QueryColumn POST_READS = new QueryColumn(this, "post_reads");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 评论数
     */
    public final QueryColumn COMMENT_COUNT = new QueryColumn(this, "comment_count");

    /**
     * 独立访客数（UV）
     */
    public final QueryColumn UNIQUE_VISITORS = new QueryColumn(this, "unique_visitors");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, STAT_DATE, PAGE_VIEWS, UNIQUE_VISITORS, POST_READS, COMMENT_COUNT, CREATED_AT, UPDATED_AT};

    public DailyStatsTableDef() {
        super("public", "daily_stats");
    }

    private DailyStatsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public DailyStatsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new DailyStatsTableDef("public", "daily_stats", alias));
    }

}
