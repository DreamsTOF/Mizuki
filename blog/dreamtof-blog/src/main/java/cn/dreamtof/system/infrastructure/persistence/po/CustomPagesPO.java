package cn.dreamtof.system.infrastructure.persistence.po;

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
 * 自定义页面表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "custom_pages")
@Schema(name="custom_pages",description = "自定义页面表")
public class CustomPagesPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 页面唯一标识
     */
    @Column(value = "page_key")
    @Schema(description = "页面唯一标识")
    private String pageKey;
    /**
     * 页面标题
     */
    @Column(value = "title")
    @Schema(description = "页面标题")
    private String title;
    /**
     * 页面内容
     */
    @Column(value = "content")
    @Schema(description = "页面内容")
    private String content;
    /**
     * 页面描述
     */
    @Column(value = "description")
    @Schema(description = "页面描述")
    private String description;
    /**
     * 封面图片
     */
    @Column(value = "cover_image")
    @Schema(description = "封面图片")
    private String coverImage;
    /**
     * 是否允许评论
     */
    @Column(value = "has_comment_enabled")
    @Schema(description = "是否允许评论")
    private Boolean hasCommentEnabled;
    /**
     * 是否启用
     */
    @Column(value = "has_enabled")
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
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

    /** 审计显示: 页面唯一标识 */
    @Schema(description = "审计显示: 页面唯一标识")
    public static final String SHOW_PAGEKEY = "pageKey";

    /** 审计显示: 页面标题 */
    @Schema(description = "审计显示: 页面标题")
    public static final String SHOW_TITLE = "title";

    /** 审计显示: 页面内容 */
    @Schema(description = "审计显示: 页面内容")
    public static final String SHOW_CONTENT = "content";

    /** 审计显示: 页面描述 */
    @Schema(description = "审计显示: 页面描述")
    public static final String SHOW_DESCRIPTION = "description";

    /** 审计显示: 封面图片 */
    @Schema(description = "审计显示: 封面图片")
    public static final String SHOW_COVERIMAGE = "coverImage";

    /** 审计显示: 是否允许评论 */
    @Schema(description = "审计显示: 是否允许评论")
    public static final String SHOW_HASCOMMENTENABLED = "hasCommentEnabled";

    /** 审计显示: 是否启用 */
    @Schema(description = "审计显示: 是否启用")
    public static final String SHOW_HASENABLED = "hasEnabled";

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
