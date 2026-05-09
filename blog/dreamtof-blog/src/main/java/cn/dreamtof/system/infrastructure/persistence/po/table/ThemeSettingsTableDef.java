package cn.dreamtof.system.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 主题设置表 表定义层。
 *
 * @author dream
 * @since 2026-05-08
 */
public class ThemeSettingsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主题设置表
     */
    public static final ThemeSettingsTableDef THEME_SETTINGS_PO = new ThemeSettingsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

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
     * 设置项键名
     */
    public final QueryColumn SETTING_KEY = new QueryColumn(this, "setting_key");

    /**
     * 设置项说明
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 设置项值
     */
    public final QueryColumn SETTING_VALUE = new QueryColumn(this, "setting_value");

    /**
     * 是否允许用户自定义
     */
    public final QueryColumn HAS_USER_CUSTOMIZABLE = new QueryColumn(this, "has_user_customizable");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, SETTING_KEY, SETTING_VALUE, DESCRIPTION, HAS_USER_CUSTOMIZABLE, VERSION, CREATED_AT, UPDATED_AT};

    public ThemeSettingsTableDef() {
        super("public", "theme_settings");
    }

    private ThemeSettingsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public ThemeSettingsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new ThemeSettingsTableDef("public", "theme_settings", alias));
    }

}
