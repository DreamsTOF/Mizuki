package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 横幅图片表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class BannersTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 横幅图片表
     */
    public static final BannersTableDef BANNERS_PO = new BannersTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 图片标题
     */
    public final QueryColumn TITLE = new QueryColumn(this, "title");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 图片 URL
     */
    public final QueryColumn IMAGE_URL = new QueryColumn(this, "image_url");

    /**
     * 展示位置
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
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 适用设备类型
     */
    public final QueryColumn DEVICE_TYPE = new QueryColumn(this, "device_type");

    /**
     * 是否启用
     */
    public final QueryColumn HAS_ENABLED = new QueryColumn(this, "has_enabled");

    /**
     * 是否轮播
     */
    public final QueryColumn HAS_CAROUSEL = new QueryColumn(this, "has_carousel");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TITLE, IMAGE_URL, DEVICE_TYPE, POSITION, SORT_ORDER, HAS_CAROUSEL, HAS_ENABLED, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public BannersTableDef() {
        super("public", "banners");
    }

    private BannersTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public BannersTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new BannersTableDef("public", "banners", alias));
    }

}
