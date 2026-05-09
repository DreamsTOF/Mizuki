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
 * 时间线技能关联表 领域实体
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
@Schema(name="TimelineEventSkillsPO", description = "时间线技能关联表 领域实体")
public class TimelineEventSkillsPO implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 关联的事件 ID，对应 timeline_events.id
     */
     @Schema(description = "关联的事件 ID，对应 timeline_events.id")
     private UUID timelineEventId;
    /**
     * 技能名称
     */
     @Schema(description = "技能名称")
     private String skillName;
    /**
     * 乐观锁版本号
     */
     @Schema(description = "乐观锁版本号")
     private Integer version;
    /**
     * 关联创建时间
     */
     @Schema(description = "关联创建时间")
     private OffsetDateTime createdAt;

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