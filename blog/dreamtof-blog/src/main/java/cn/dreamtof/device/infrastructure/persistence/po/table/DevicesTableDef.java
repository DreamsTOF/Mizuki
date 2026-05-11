package cn.dreamtof.device.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 设备表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class DevicesTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备表
     */
    public static final DevicesTableDef DEVICES_PO = new DevicesTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 设备外部链接
     */
    public final QueryColumn LINK = new QueryColumn(this, "link");

    /**
     * 设备名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 设备图片
     */
    public final QueryColumn IMAGE = new QueryColumn(this, "image");

    /**
     * 设备规格参数
     */
    public final QueryColumn SPECS = new QueryColumn(this, "specs");

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
     * 所属分类 ID，对应 device_categories.id
     */
    public final QueryColumn CATEGORY_ID = new QueryColumn(this, "category_id");

    /**
     * 设备描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CATEGORY_ID, NAME, IMAGE, SPECS, DESCRIPTION, LINK, SORT_ORDER, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public DevicesTableDef() {
        super("public", "devices");
    }

    private DevicesTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public DevicesTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new DevicesTableDef("public", "devices", alias));
    }

}
