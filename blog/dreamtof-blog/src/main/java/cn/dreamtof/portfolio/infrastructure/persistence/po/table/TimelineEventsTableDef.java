package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 时间线事件表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class TimelineEventsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 时间线事件表
     */
    public static final TimelineEventsTableDef TIMELINE_EVENTS_PO = new TimelineEventsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 图标标识符
     */
    public final QueryColumn ICON = new QueryColumn(this, "icon");

    /**
     * 颜色值
     */
    public final QueryColumn COLOR = new QueryColumn(this, "color");

    /**
     * 事件标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 结束日期
     */
    public final QueryColumn END_DATE = new QueryColumn(this, "end_date");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 地点
     */
    public final QueryColumn LOCATION = new QueryColumn(this, "location");

    /**
     * 职位/角色
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
     * 事件类型
     */
    public final QueryColumn EVENT_TYPE = new QueryColumn(this, "event_type");

    /**
     * 开始日期
     */
    public final QueryColumn START_DATE = new QueryColumn(this, "start_date");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 事件描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 是否重点展示
     */
    public final QueryColumn HAS_FEATURED = new QueryColumn(this, "has_featured");

    /**
     * 所属机构
     */
    public final QueryColumn ORGANIZATION = new QueryColumn(this, "organization");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, DESCRIPTION, EVENT_TYPE, ICON, COLOR, START_DATE, END_DATE, LOCATION, ORGANIZATION, POSITION, HAS_FEATURED, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public TimelineEventsTableDef() {
        super("public", "timeline_events");
    }

    private TimelineEventsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public TimelineEventsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new TimelineEventsTableDef("public", "timeline_events", alias));
    }

}
