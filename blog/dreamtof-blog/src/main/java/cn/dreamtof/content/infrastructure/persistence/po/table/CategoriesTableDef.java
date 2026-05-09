package cn.dreamtof.content.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 文章分类表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class CategoriesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章分类表
     */
    public static final CategoriesTableDef CATEGORIES_PO = new CategoriesTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 分类图标
     */
    public final QueryColumn ICON = new QueryColumn(this, "icon");

    /**
     * 分类名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * URL 友好的分类标识
     */
    public final QueryColumn SLUG = new QueryColumn(this, "slug");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 父分类 ID，对应 categories.id
     */
    public final QueryColumn PARENT_ID = new QueryColumn(this, "parent_id");

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
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 分类封面图片
     */
    public final QueryColumn COVER_IMAGE = new QueryColumn(this, "cover_image");

    /**
     * 是否启用
     */
    public final QueryColumn HAS_ENABLED = new QueryColumn(this, "has_enabled");

    /**
     * 分类描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, SLUG, DESCRIPTION, PARENT_ID, ICON, COVER_IMAGE, SORT_ORDER, HAS_ENABLED, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public CategoriesTableDef() {
        super("public", "categories");
    }

    private CategoriesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public CategoriesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new CategoriesTableDef("public", "categories", alias));
    }

}
