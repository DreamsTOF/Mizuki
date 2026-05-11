package cn.dreamtof.portfolio.infrastructure.persistence.po.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 技能表 表定义层。
 *
 * @author lyl
 * @since 2026-05-09
 */
public class SkillsTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 技能表
     */
    public static final SkillsTableDef SKILLS_PO = new SkillsTableDef();

    /**
     * ID
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * Iconify 图标名称
     */
    public final QueryColumn ICON = new QueryColumn(this, "icon");

    /**
     * 技能名称
     */
    public final QueryColumn NAME = new QueryColumn(this, "name");

    /**
     * 主题色
     */
    public final QueryColumn COLOR = new QueryColumn(this, "color");

    /**
     * 技能等级
     */
    public final QueryColumn LEVEL = new QueryColumn(this, "level");

    /**
     * 乐观锁版本号
     */
    public final QueryColumn VERSION = new QueryColumn(this, "version");

    /**
     * 技能分类
     */
    public final QueryColumn CATEGORY = new QueryColumn(this, "category");

    /**
     * 关联项目 ID 列表
     */
    public final QueryColumn PROJECTS = new QueryColumn(this, "projects");

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
     * 技能描述
     */
    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    /**
     * 认证证书列表
     */
    public final QueryColumn CERTIFICATIONS = new QueryColumn(this, "certifications");

    /**
     * 经验年数
     */
    public final QueryColumn EXPERIENCE_YEARS = new QueryColumn(this, "experience_years");

    /**
     * 经验月数
     */
    public final QueryColumn EXPERIENCE_MONTHS = new QueryColumn(this, "experience_months");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, DESCRIPTION, ICON, CATEGORY, LEVEL, EXPERIENCE_YEARS, EXPERIENCE_MONTHS, COLOR, PROJECTS, CERTIFICATIONS, VERSION, CREATED_AT, UPDATED_AT, DELETED_AT};

    public SkillsTableDef() {
        super("public", "skills");
    }

    private SkillsTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public SkillsTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new SkillsTableDef("public", "skills", alias));
    }

}
