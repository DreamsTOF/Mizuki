package cn.dreamtof.content.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 日记条目表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class DiaryEntriesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日记条目表
     */
    public static final DiaryEntriesTableDef DIARY_ENTRIES_PO = new DiaryEntriesTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 心情描述
     */
    public final QueryColumn MOOD = new QueryColumn(this, "mood");

    /**
     * 标签数组
     */
    public final QueryColumn TAGS = new QueryColumn(this, "tags");

    /**
     * 图片 URL 数组
     */
    public final QueryColumn IMAGES = new QueryColumn(this, "images");

    /**
     * 日记正文内容
     */
    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 地点信息
     */
    public final QueryColumn LOCATION = new QueryColumn(this, "location");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 日记日期时间
     */
    public final QueryColumn ENTRY_DATE = new QueryColumn(this, "entry_date");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CONTENT, ENTRY_DATE, IMAGES, LOCATION, MOOD, TAGS, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public DiaryEntriesTableDef() {
        super("public", "diary_entries");
    }

    private DiaryEntriesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public DiaryEntriesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new DiaryEntriesTableDef("public", "diary_entries", alias));
    }

}
