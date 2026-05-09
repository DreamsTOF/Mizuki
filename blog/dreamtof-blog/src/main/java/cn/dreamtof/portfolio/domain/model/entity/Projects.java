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
 * 项目表 领域实体
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
@Schema(name="Projects", description = "项目表 领域实体")
public class Projects implements Serializable, IdAudit{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 项目标题
     */
     @Schema(description = "项目标题")
     private String title;
    /**
     * 项目描述
     */
     @Schema(description = "项目描述")
     private String description;
    /**
     * 封面图片路径
     */
     @Schema(description = "封面图片路径")
     private String image;
    /**
     * 项目类别
     */
     @Schema(description = "项目类别")
     private String category;
    /**
     * 项目状态
     */
     @Schema(description = "项目状态")
     private String status;
    /**
     * 在线演示地址
     */
     @Schema(description = "在线演示地址")
     private String liveDemoUrl;
    /**
     * 源码仓库地址
     */
     @Schema(description = "源码仓库地址")
     private String sourceCodeUrl;
    /**
     * 项目主页地址
     */
     @Schema(description = "项目主页地址")
     private String visitUrl;
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
     * 是否精选
     */
     @Schema(description = "是否精选")
     private Boolean hasFeatured;
    /**
     * 是否显示封面
     */
     @Schema(description = "是否显示封面")
     private Boolean hasShowImage;
    /**
     * 排序权重
     */
     @Schema(description = "排序权重")
     private Integer sortOrder;
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