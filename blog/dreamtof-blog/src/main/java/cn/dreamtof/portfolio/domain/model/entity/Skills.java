package cn.dreamtof.portfolio.domain.model.entity;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name = "Skills", description = "技能表 领域实体")
public class Skills implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_LEVEL = 5;
    private static final int MIN_LEVEL = 1;

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "技能名称")
    private String name;
    @Schema(description = "技能描述")
    private String description;
    @Schema(description = "Iconify 图标名称")
    private String icon;
    @Schema(description = "技能分类")
    private String category;
    @Schema(description = "技能等级")
    private String level;
    @Schema(description = "经验年数")
    private Integer experienceYears;
    @Schema(description = "经验月数")
    private Integer experienceMonths;
    @Schema(description = "主题色")
    private String color;
    @Schema(description = "关联项目 ID 列表")
    private String projects;
    @Schema(description = "认证证书列表")
    private String certifications;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "最后更新时间")
    private OffsetDateTime updatedAt;
    @Schema(description = "软删除时间戳")
    private OffsetDateTime deletedAt;

    // ==========================================
    // 静态工厂方法
    // ==========================================

    public static Skills create(String name, String description, String icon,
                                String category, String level,
                                Integer experienceYears, Integer experienceMonths,
                                String color, String projects, String certifications) {
        Asserts.notBlank(name, "技能名称不能为空");
        Skills entity = new Skills();
        entity.name = name;
        entity.description = description;
        entity.icon = icon;
        entity.category = category;
        entity.level = level;
        entity.experienceYears = experienceYears;
        entity.experienceMonths = experienceMonths;
        entity.color = color;
        entity.projects = projects;
        entity.certifications = certifications;
        return entity;
    }

    // ==========================================
    // 领域行为
    // ==========================================

    public void update(String name, String description, String icon,
                       String category, String level,
                       Integer experienceYears, Integer experienceMonths,
                       String color, String projects, String certifications) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (icon != null) {
            this.icon = icon;
        }
        if (category != null) {
            this.category = category;
        }
        if (level != null) {
            this.level = level;
        }
        if (experienceYears != null) {
            this.experienceYears = experienceYears;
        }
        if (experienceMonths != null) {
            this.experienceMonths = experienceMonths;
        }
        if (color != null) {
            this.color = color;
        }
        if (projects != null) {
            this.projects = projects;
        }
        if (certifications != null) {
            this.certifications = certifications;
        }
    }

    public void markDeleted() {
        Asserts.isTrue(this.deletedAt == null, "技能已被删除");
        this.deletedAt = DateUtils.offsetNow();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
