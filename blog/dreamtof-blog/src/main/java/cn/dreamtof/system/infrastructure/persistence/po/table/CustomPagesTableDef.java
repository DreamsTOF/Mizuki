package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 自定义页面表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class CustomPagesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自定义页面表
     */
    public static final CustomPagesTableDef CUSTOM_PAGES_PO = new CustomPagesTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 页面标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 页面内容
     */
    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    /**
     * 页面唯一标识
     */
    public final QueryColumn PAGE_KEY = new QueryColumn(this, "page_key");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 软删除时间戳
     */
    public final QueryColumn DELETED_AT = new QueryColumn(this, "deleted_at");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 封面图片
     */
    public final QueryColumn COVER_IMAGE = new QueryColumn(this, "cover_image");

    /**
     * 是否启用
     */
    public final QueryColumn HAS_ENABLED = new QueryColumn(this, "has_enabled");

    /**
     * 页面描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 是否允许评论
     */
    public final QueryColumn HAS_COMMENT_ENABLED = new QueryColumn(this, "has_comment_enabled");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PAGE_KEY, TITLE, CONTENT, DESCRIPTION, COVER_IMAGE, HAS_COMMENT_ENABLED, HAS_ENABLED, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public CustomPagesTableDef() {
        super("public", "custom_pages");
    }

    private CustomPagesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public CustomPagesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new CustomPagesTableDef("public", "custom_pages", alias));
    }

}
