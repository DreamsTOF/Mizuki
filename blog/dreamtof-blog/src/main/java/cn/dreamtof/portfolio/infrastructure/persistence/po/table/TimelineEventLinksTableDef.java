package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 时间线链接关联表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class TimelineEventLinksTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 时间线链接关联表
     */
    public static final TimelineEventLinksTableDef TIMELINE_EVENT_LINKS_PO = new TimelineEventLinksTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 链接地址
     */
    public final QueryColumn URL = new QueryColumn(this, "url");

    /**
     * 链接名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 链接类型
     */
    public final QueryColumn LINK_TYPE = new QueryColumn(this, "link_type");

    /**
     * 关联创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TIMELINE_EVENT_ID, NAME, URL, LINK_TYPE, VERSION, CREATED_AT};

    public TimelineEventLinksTableDef() {
        super("public", "timeline_event_links");
    }

    private TimelineEventLinksTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public TimelineEventLinksTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new TimelineEventLinksTableDef("public", "timeline_event_links", alias));
    }

}
