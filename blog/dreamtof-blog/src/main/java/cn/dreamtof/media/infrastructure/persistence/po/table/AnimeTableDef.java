package cn.dreamtof.media.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 番剧表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class AnimeTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 番剧表
     */
    public static final AnimeTableDef ANIME_PO = new AnimeTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 番剧链接
     */
    public final QueryColumn LINK = new QueryColumn(this, "link");

    /**
     * 年份
     */
    public final QueryColumn YEAR = new QueryColumn(this, "year");

    /**
     * 封面图片
     */
    public final QueryColumn COVER = new QueryColumn(this, "cover");

    /**
     * 类型/流派数组
     */
    public final QueryColumn GENRE = new QueryColumn(this, "genre");

    /**
     * 番剧标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 评分（0-10）
     */
    public final QueryColumn RATING = new QueryColumn(this, "rating");

    /**
     * 观看状态
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 制作公司
     */
    public final QueryColumn STUDIO = new QueryColumn(this, "studio");

    /**
     * 结束观看日期
     */
    public final QueryColumn END_DATE = new QueryColumn(this, "end_date");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 集数信息
     */
    public final QueryColumn EPISODES = new QueryColumn(this, "episodes");

    /**
     * 当前观看进度
     */
    public final QueryColumn PROGRESS = new QueryColumn(this, "progress");

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
     * 开始观看日期
     */
    public final QueryColumn START_DATE = new QueryColumn(this, "start_date");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 番剧描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 总集数
     */
    public final QueryColumn TOTAL_EPISODES = new QueryColumn(this, "total_episodes");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, STATUS, RATING, COVER, DESCRIPTION, EPISODES, YEAR, GENRE, STUDIO, LINK, PROGRESS, TOTAL_EPISODES, START_DATE, END_DATE, SORT_ORDER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public AnimeTableDef() {
        super("public", "anime");
    }

    private AnimeTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public AnimeTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new AnimeTableDef("public", "anime", alias));
    }

}
