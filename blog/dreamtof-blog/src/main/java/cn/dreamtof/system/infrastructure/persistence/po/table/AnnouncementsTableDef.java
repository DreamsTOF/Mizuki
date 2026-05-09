package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 公告表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class AnnouncementsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 公告表
     */
    public static final AnnouncementsTableDef ANNOUNCEMENTS_PO = new AnnouncementsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 公告标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 公告内容
     */
    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    /**
     * 结束展示时间
     */
    public final QueryColumn END_TIME = new QueryColumn(this, "end_time");

    /**
     * 链接 URL
     */
    public final QueryColumn LINK_URL = new QueryColumn(this, "link_url");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 链接文本
     */
    public final QueryColumn LINK_TEXT = new QueryColumn(this, "link_text");

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
     * 开始展示时间
     */
    public final QueryColumn START_TIME = new QueryColumn(this, "start_time");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 是否启用
     */
    public final QueryColumn HAS_ENABLED = new QueryColumn(this, "has_enabled");

    /**
     * 是否允许关闭
     */
    public final QueryColumn HAS_CLOSABLE = new QueryColumn(this, "has_closable");

    /**
     * 是否外部链接
     */
    public final QueryColumn HAS_EXTERNAL_LINK = new QueryColumn(this, "has_external_link");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, CONTENT, LINK_TEXT, LINK_URL, HAS_EXTERNAL_LINK, HAS_CLOSABLE, HAS_ENABLED, START_TIME, END_TIME, SORT_ORDER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public AnnouncementsTableDef() {
        super("public", "announcements");
    }

    private AnnouncementsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public AnnouncementsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new AnnouncementsTableDef("public", "announcements", alias));
    }

}
