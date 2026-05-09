package cn.dreamtof.device.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 设备分类表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class DeviceCategoriesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备分类表
     */
    public static final DeviceCategoriesTableDef DEVICE_CATEGORIES_PO = new DeviceCategoriesTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 分类名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

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
     * 排序顺序
     */
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, SORT_ORDER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public DeviceCategoriesTableDef() {
        super("public", "device_categories");
    }

    private DeviceCategoriesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public DeviceCategoriesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new DeviceCategoriesTableDef("public", "device_categories", alias));
    }

}
