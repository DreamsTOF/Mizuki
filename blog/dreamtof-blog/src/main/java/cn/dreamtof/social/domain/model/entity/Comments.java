package cn.dreamtof.social.domain.model.entity;

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
 * 评论表 领域实体
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
@Schema(name="Comments", description = "评论表 领域实体")
public class Comments implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 评论目标类型
     */
     @Schema(description = "评论目标类型")
     private String targetType;
    /**
     * 评论目标 ID
     */
     @Schema(description = "评论目标 ID")
     private UUID targetId;
    /**
     * 父评论 ID，对应 comments.id
     */
     @Schema(description = "父评论 ID，对应 comments.id")
     private UUID parentId;
    /**
     * 评论者昵称
     */
     @Schema(description = "评论者昵称")
     private String authorName;
    /**
     * 评论者邮箱
     */
     @Schema(description = "评论者邮箱")
     private String authorEmail;
    /**
     * 评论者网站
     */
     @Schema(description = "评论者网站")
     private String authorUrl;
    /**
     * 评论者头像
     */
     @Schema(description = "评论者头像")
     private String authorAvatar;
    /**
     * 评论内容
     */
     @Schema(description = "评论内容")
     private String content;
    /**
     * 评论者 IP
     */
     @Schema(description = "评论者 IP")
     private String ipAddress;
    /**
     * 评论者 User-Agent
     */
     @Schema(description = "评论者 User-Agent")
     private String userAgent;
    /**
     * 是否审核通过
     */
     @Schema(description = "是否审核通过")
     private Boolean hasApproved;
    /**
     * 是否置顶
     */
     @Schema(description = "是否置顶")
     private Boolean hasPinned;
    /**
     * 点赞数
     */
     @Schema(description = "点赞数")
     private Integer likeCount;
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