package cn.dreamtof.portfolio.infrastructure.persistence.po;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.annotation.Column;
import cn.dreamtof.core.base.CreatedTimeAudit;
import cn.dreamtof.core.base.UpdatedTimeAudit;
import cn.dreamtof.core.base.VersionAudit;
import cn.dreamtof.core.base.CreatedByAudit;
import cn.dreamtof.core.base.UpdatedByAudit;
import cn.dreamtof.core.base.IdAudit;
import cn.dreamtof.common.persistence.handler.UniversalJsonTypeHandler;
import cn.dreamtof.common.persistence.handler.UUIDTypeHandler;
import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.time.OffsetDateTime;





      

/**
 * 技能表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "skills")
@Schema(name="skills",description = "技能表")
public class SkillsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id(keyType=KeyType.Generator, value="uuidV7")
    @Column(value = "id",typeHandler = UUIDTypeHandler.class)
    @Schema(description = "ID")
    private UUID id;
    /**
     * 技能名称
     */
    @Column(value = "name")
    @Schema(description = "技能名称")
    private String name;
    /**
     * 技能描述
     */
    @Column(value = "description")
    @Schema(description = "技能描述")
    private String description;
    /**
     * Iconify 图标名称
     */
    @Column(value = "icon")
    @Schema(description = "Iconify 图标名称")
    private String icon;
    /**
     * 技能分类
     */
    @Column(value = "category")
    @Schema(description = "技能分类")
    private String category;
    /**
     * 技能等级
     */
    @Column(value = "level")
    @Schema(description = "技能等级")
    private String level;
    /**
     * 经验年数
     */
    @Column(value = "experience_years")
    @Schema(description = "经验年数")
    private Integer experienceYears;
    /**
     * 经验月数
     */
    @Column(value = "experience_months")
    @Schema(description = "经验月数")
    private Integer experienceMonths;
    /**
     * 主题色
     */
    @Column(value = "color")
    @Schema(description = "主题色")
    private String color;
    /**
     * 关联项目 ID 列表
     */
    @Column(value = "projects", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "关联项目 ID 列表")
    private String projects;
    /**
     * 认证证书列表
     */
    @Column(value = "certifications", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "认证证书列表")
    private String certifications;
    /**
     * 乐观锁版本号
     */
    @Column(value = "version")
    @Schema(description = "乐观锁版本号")
    private Integer version;
    /**
     * 创建时间
     */
    @Column(value = "created_at")
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    /**
     * 最后更新时间
     */
    @Column(value = "updated_at")
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;
    /**
     * 软删除时间戳
     */
    @Column(value = "deleted_at", isLogicDelete = true)
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    /** 审计显示: ID */
    @Schema(description = "审计显示: ID")
    public static final String SHOW_ID = "id";

    /** 审计显示: 技能名称 */
    @Schema(description = "审计显示: 技能名称")
    public static final String SHOW_NAME = "name";

    /** 审计显示: 技能描述 */
    @Schema(description = "审计显示: 技能描述")
    public static final String SHOW_DESCRIPTION = "description";

    /** 审计显示: Iconify 图标名称 */
    @Schema(description = "审计显示: Iconify 图标名称")
    public static final String SHOW_ICON = "icon";

    /** 审计显示: 技能分类 */
    @Schema(description = "审计显示: 技能分类")
    public static final String SHOW_CATEGORY = "category";

    /** 审计显示: 技能等级 */
    @Schema(description = "审计显示: 技能等级")
    public static final String SHOW_LEVEL = "level";

    /** 审计显示: 经验年数 */
    @Schema(description = "审计显示: 经验年数")
    public static final String SHOW_EXPERIENCEYEARS = "experienceYears";

    /** 审计显示: 经验月数 */
    @Schema(description = "审计显示: 经验月数")
    public static final String SHOW_EXPERIENCEMONTHS = "experienceMonths";

    /** 审计显示: 主题色 */
    @Schema(description = "审计显示: 主题色")
    public static final String SHOW_COLOR = "color";

    /** 审计显示: 关联项目 ID 列表 */
    @Schema(description = "审计显示: 关联项目 ID 列表")
    public static final String SHOW_PROJECTS = "projects";

    /** 审计显示: 认证证书列表 */
    @Schema(description = "审计显示: 认证证书列表")
    public static final String SHOW_CERTIFICATIONS = "certifications";

    /** 审计显示: 乐观锁版本号 */
    @Schema(description = "审计显示: 乐观锁版本号")
    public static final String SHOW_VERSION = "version";

    /** 审计显示: 创建时间 */
    @Schema(description = "审计显示: 创建时间")
    public static final String SHOW_CREATEDAT = "createdAt";

    /** 审计显示: 最后更新时间 */
    @Schema(description = "审计显示: 最后更新时间")
    public static final String SHOW_UPDATEDAT = "updatedAt";

    /** 审计显示: 软删除时间戳 */
    @Schema(description = "审计显示: 软删除时间戳")
    public static final String SHOW_DELETEDAT = "deletedAt";

}
