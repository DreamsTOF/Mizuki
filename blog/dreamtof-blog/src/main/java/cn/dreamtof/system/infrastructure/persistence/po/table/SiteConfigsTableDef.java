package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 站点配置表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class SiteConfigsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 站点配置表
     */
    public static final SiteConfigsTableDef SITE_CONFIGS_PO = new SiteConfigsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 配置项键名
     */
    public final QueryColumn CONFIG_KEY = new QueryColumn(this, "config_key");

    /**
     * 创建时间
     */
    public final QueryColumn CREATED_AT = new QueryColumn(this, "created_at");

    /**
     * 最后更新时间
     */
    public final QueryColumn UPDATED_AT = new QueryColumn(this, "updated_at");

    /**
     * 配置项值
     */
    public final QueryColumn CONFIG_VALUE = new QueryColumn(this, "config_value");

    /**
     * 配置项说明
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CONFIG_KEY, CONFIG_VALUE, DESCRIPTION, VERSION, CREATED_AT, UPDATED_AT};

    public SiteConfigsTableDef() {
        super("public", "site_configs");
    }

    private SiteConfigsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SiteConfigsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SiteConfigsTableDef("public", "site_configs", alias));
    }

}
