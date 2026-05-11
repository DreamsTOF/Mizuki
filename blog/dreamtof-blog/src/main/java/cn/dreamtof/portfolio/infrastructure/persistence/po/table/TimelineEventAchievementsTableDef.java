package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 时间线成就关联表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class TimelineEventAchievementsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 时间线成就关联表
     */
    public static final TimelineEventAchievementsTableDef TIMELINE_EVENT_ACHIEVEMENTS_PO = new TimelineEventAchievementsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 关联创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 成就描述
     */
    public final QueryColumn ACHIEVEMENT = new QueryColumn(this, "achievement");

    /**
     * 关联的事件 ID，对应 timeline_events.id
     */
    public final QueryColumn TIMELINE_EVENT_ID = new QueryColumn(this, "timeline_event_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TIMELINE_EVENT_ID, ACHIEVEMENT, SORT_ORDER, VERSION, CREATED_AT};

    public TimelineEventAchievementsTableDef() {
        super("public", "timeline_event_achievements");
    }

    private TimelineEventAchievementsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public TimelineEventAchievementsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new TimelineEventAchievementsTableDef("public", "timeline_event_achievements", alias));
    }

}
