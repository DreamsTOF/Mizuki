package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 时间线技能关联表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class TimelineEventSkillsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 时间线技能关联表
     */
    public static final TimelineEventSkillsTableDef TIMELINE_EVENT_SKILLS_PO = new TimelineEventSkillsTableDef();

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
     * 技能名称
     */
    public final QueryColumn SKILL_NAME = new QueryColumn(this, "skill_name");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TIMELINE_EVENT_ID, SKILL_NAME, VERSION, CREATED_AT};

    public TimelineEventSkillsTableDef() {
        super("public", "timeline_event_skills");
    }

    private TimelineEventSkillsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public TimelineEventSkillsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new TimelineEventSkillsTableDef("public", "timeline_event_skills", alias));
    }

}
