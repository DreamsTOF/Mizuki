package cn.dreamtof.portfolio.domain.model.entity;

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
 * 时间线事件表 领域实体
 * <p>
 * 职责：核心业务逻辑、领域行为校验、审计数据持有。
 * </p>
 *
 * @author dream
 * @since 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@Schema(name="TimelineEvents", description = "时间线事件表 领域实体")
public class TimelineEvents implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 事件标题
     */
     @Schema(description = "事件标题")
     private String title;
    /**
     * 事件描述
     */
     @Schema(description = "事件描述")
     private String description;
    /**
     * 事件类型
     */
     @Schema(description = "事件类型")
     private String eventType;
    /**
     * 图标标识符
     */
     @Schema(description = "图标标识符")
     private String icon;
    /**
     * 颜色值
     */
     @Schema(description = "颜色值")
     private String color;
    /**
     * 开始日期
     */
     @Schema(description = "开始日期")
     private OffsetDateTime startDate;
    /**
     * 结束日期
     */
     @Schema(description = "结束日期")
     private OffsetDateTime endDate;
    /**
     * 地点
     */
     @Schema(description = "地点")
     private String location;
    /**
     * 所属机构
     */
     @Schema(description = "所属机构")
     private String organization;
    /**
     * 职位/角色
     */
     @Schema(description = "职位/角色")
     private String position;
    /**
     * 是否重点展示
     */
     @Schema(description = "是否重点展示")
     private Boolean hasFeatured;
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