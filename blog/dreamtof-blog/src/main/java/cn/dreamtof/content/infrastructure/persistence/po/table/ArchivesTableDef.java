package cn.dreamtof.content.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 文章归档索引表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class ArchivesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章归档索引表
     */
    public static final ArchivesTableDef ARCHIVES_PO = new ArchivesTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 归档年份
     */
    public final QueryColumn YEAR = new QueryColumn(this, "year");

    /**
     * 归档月份
     */
    public final QueryColumn MONTH = new QueryColumn(this, "month");

    /**
     * 该年月文章 ID 列表
     */
    public final QueryColumn POST_IDS = new QueryColumn(this, "post_ids");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 该年月文章数量
     */
    public final QueryColumn POST_COUNT = new QueryColumn(this, "post_count");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, YEAR, MONTH, POST_COUNT, POST_IDS, VERSION, CREATED_AT, UPDATED_AT};

    public ArchivesTableDef() {
        super("public", "archives");
    }

    private ArchivesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ArchivesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ArchivesTableDef("public", "archives", alias));
    }

}
