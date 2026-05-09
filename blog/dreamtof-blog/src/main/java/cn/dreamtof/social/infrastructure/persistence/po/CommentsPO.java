package cn.dreamtof.social.infrastructure.persistence.po;

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
 * 评论表 实体类
 *
 * @author dream
 * @since 2026-05-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "comments")
@Schema(name="comments",description = "评论表")
public class CommentsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 评论目标类型
     */
    @Column(value = "target_type")
    @Schema(description = "评论目标类型")
    private String targetType;
    /**
     * 评论目标 ID
     */
    @Column(value = "target_id")
    @Schema(description = "评论目标 ID")
    private UUID targetId;
    /**
     * 父评论 ID，对应 comments.id
     */
    @Column(value = "parent_id")
    @Schema(description = "父评论 ID，对应 comments.id")
    private UUID parentId;
    /**
     * 评论者昵称
     */
    @Column(value = "author_name")
    @Schema(description = "评论者昵称")
    private String authorName;
    /**
     * 评论者邮箱
     */
    @Column(value = "author_email")
    @Schema(description = "评论者邮箱")
    private String authorEmail;
    /**
     * 评论者网站
     */
    @Column(value = "author_url")
    @Schema(description = "评论者网站")
    private String authorUrl;
    /**
     * 评论者头像
     */
    @Column(value = "author_avatar")
    @Schema(description = "评论者头像")
    private String authorAvatar;
    /**
     * 评论内容
     */
    @Column(value = "content")
    @Schema(description = "评论内容")
    private String content;
    /**
     * 评论者 IP
     */
    @Column(value = "ip_address")
    @Schema(description = "评论者 IP")
    private String ipAddress;
    /**
     * 评论者 User-Agent
     */
    @Column(value = "user_agent")
    @Schema(description = "评论者 User-Agent")
    private String userAgent;
    /**
     * 是否审核通过
     */
    @Column(value = "has_approved")
    @Schema(description = "是否审核通过")
    private Boolean hasApproved;
    /**
     * 是否置顶
     */
    @Column(value = "has_pinned")
    @Schema(description = "是否置顶")
    private Boolean hasPinned;
    /**
     * 点赞数
     */
    @Column(value = "like_count")
    @Schema(description = "点赞数")
    private Integer likeCount;
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

    /** 审计显示: 评论目标类型 */
    @Schema(description = "审计显示: 评论目标类型")
    public static final String SHOW_TARGETTYPE = "targetType";

    /** 审计显示: 评论目标 ID */
    @Schema(description = "审计显示: 评论目标 ID")
    public static final String SHOW_TARGETID = "targetId";

    /** 审计显示: 父评论 ID，对应 comments.id */
    @Schema(description = "审计显示: 父评论 ID，对应 comments.id")
    public static final String SHOW_PARENTID = "parentId";

    /** 审计显示: 评论者昵称 */
    @Schema(description = "审计显示: 评论者昵称")
    public static final String SHOW_AUTHORNAME = "authorName";

    /** 审计显示: 评论者邮箱 */
    @Schema(description = "审计显示: 评论者邮箱")
    public static final String SHOW_AUTHOREMAIL = "authorEmail";

    /** 审计显示: 评论者网站 */
    @Schema(description = "审计显示: 评论者网站")
    public static final String SHOW_AUTHORURL = "authorUrl";

    /** 审计显示: 评论者头像 */
    @Schema(description = "审计显示: 评论者头像")
    public static final String SHOW_AUTHORAVATAR = "authorAvatar";

    /** 审计显示: 评论内容 */
    @Schema(description = "审计显示: 评论内容")
    public static final String SHOW_CONTENT = "content";

    /** 审计显示: 评论者 IP */
    @Schema(description = "审计显示: 评论者 IP")
    public static final String SHOW_IPADDRESS = "ipAddress";

    /** 审计显示: 评论者 User-Agent */
    @Schema(description = "审计显示: 评论者 User-Agent")
    public static final String SHOW_USERAGENT = "userAgent";

    /** 审计显示: 是否审核通过 */
    @Schema(description = "审计显示: 是否审核通过")
    public static final String SHOW_HASAPPROVED = "hasApproved";

    /** 审计显示: 是否置顶 */
    @Schema(description = "审计显示: 是否置顶")
    public static final String SHOW_HASPINNED = "hasPinned";

    /** 审计显示: 点赞数 */
    @Schema(description = "审计显示: 点赞数")
    public static final String SHOW_LIKECOUNT = "likeCount";

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
