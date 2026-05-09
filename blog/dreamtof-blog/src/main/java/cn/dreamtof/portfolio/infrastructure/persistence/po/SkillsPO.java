package cn.dreamtof.portfolio.infrastructure.persistence.po;

import cn.dreamtof.core.base.CreateTimeAudit;
import cn.dreamtof.core.base.UpdateTimeAudit;
import cn.dreamtof.core.base.VersionAudit;

import java.util.UUID;
import java.io.Serializable;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.*;
import lombok.EqualsAndHashCode;
import java.time.OffsetDateTime;



/**
 * 技能表 领域实体
 * <p>
 * 职责：核心业务逻辑、领域行为校验、审计数据持有。
 * </p>
 *
 * @author dream
 * @since 2026-05-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name="SkillsPO", description = "技能表 领域实体")
public class SkillsPO implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 技能名称
     */
     @Schema(description = "技能名称")
     private String name;
    /**
     * 技能描述
     */
     @Schema(description = "技能描述")
     private String description;
    /**
     * Iconify 图标名称
     */
     @Schema(description = "Iconify 图标名称")
     private String icon;
    /**
     * 技能分类
     */
     @Schema(description = "技能分类")
     private String category;
    /**
     * 技能等级
     */
     @Schema(description = "技能等级")
     private String level;
    /**
     * 经验年数
     */
     @Schema(description = "经验年数")
     private Integer experienceYears;
    /**
     * 经验月数
     */
     @Schema(description = "经验月数")
     private Integer experienceMonths;
    /**
     * 主题色
     */
     @Schema(description = "主题色")
     private String color;
    /**
     * 关联项目 ID 列表
     */
     @Schema(description = "关联项目 ID 列表")
     private String projects;
    /**
     * 认证证书列表
     */
     @Schema(description = "认证证书列表")
     private String certifications;
    /**
     * 乐观锁版本号
     */
     @Schema(description = "乐观锁版本号")
     private Integer version;
    /**
     * 创建时间
     */
     @Schema(description = "创建时间")
     private OffsetDateTime createdAt;
    /**
     * 最后更新时间
     */
     @Schema(description = "最后更新时间")
     private OffsetDateTime updatedAt;
    /**
     * 软删除时间戳
     */
     @Schema(description = "软删除时间戳")
     private OffsetDateTime deletedAt;

    // ==========================================
    // 🚀 领域行为 (Domain Logic)
    // ==========================================

    /**
     * 初始化业务逻辑
     */
    public void init() {
        // 在此处编写创建时的默认值或初始校验逻辑
    }

    /**
     * 业务校验：示例（如权限判断）
     */
    public boolean canBeManagedBy(Object userId) {
        // 利用实体已有的审计字段进行逻辑判断
        return true;
    }
}