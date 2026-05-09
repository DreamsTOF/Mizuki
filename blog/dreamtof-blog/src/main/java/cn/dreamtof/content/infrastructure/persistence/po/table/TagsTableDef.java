package cn.dreamtof.content.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 标签表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class TagsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签表
     */
    public static final TagsTableDef TAGS_PO = new TagsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 标签名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * URL 友好的标签标识
     */
    public final QueryColumn SLUG = new QueryColumn(this, "slug");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, SLUG, VERSION, CREATED_AT, UPDATED_AT};

    public TagsTableDef() {
        super("public", "tags");
    }

    private TagsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public TagsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new TagsTableDef("public", "tags", alias));
    }

}
