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
 * 友链表 实体类
 *
 * @author dream
 * @since 2026-05-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "friends")
@Schema(name="friends",description = "友链表")
public class FriendsPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 友链网站标题
     */
    @Column(value = "title")
    @Schema(description = "友链网站标题")
    private String title;
    /**
     * 友链网站描述
     */
    @Column(value = "description")
    @Schema(description = "友链网站描述")
    private String description;
    /**
     * 网站链接
     */
    @Column(value = "siteurl")
    @Schema(description = "网站链接")
    private String siteurl;
    /**
     * 头像/Logo 图片 URL
     */
    @Column(value = "imgurl")
    @Schema(description = "头像/Logo 图片 URL")
    private String imgurl;
    /**
     * 图片类型：0=外链，1=本地
     */
    @Column(value = "img_type")
    @Schema(description = "图片类型：0=外链，1=本地")
    private Integer imgType;
    /**
     * 本地存储路径
     */
    @Column(value = "img_storage_path")
    @Schema(description = "本地存储路径")
    private String imgStoragePath;
    /**
     * 排序顺序
     */
    @Column(value = "sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    /**
     * 是否启用
     */
    @Column(value = "has_active")
    @Schema(description = "是否启用")
    private Boolean hasActive;
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

    /** 审计显示: 友链网站标题 */
    @Schema(description = "审计显示: 友链网站标题")
    public static final String SHOW_TITLE = "title";

    /** 审计显示: 友链网站描述 */
    @Schema(description = "审计显示: 友链网站描述")
    public static final String SHOW_DESCRIPTION = "description";

    /** 审计显示: 网站链接 */
    @Schema(description = "审计显示: 网站链接")
    public static final String SHOW_SITEURL = "siteurl";

    /** 审计显示: 头像/Logo 图片 URL */
    @Schema(description = "审计显示: 头像/Logo 图片 URL")
    public static final String SHOW_IMGURL = "imgurl";

    /** 审计显示: 图片类型：0=外链，1=本地 */
    @Schema(description = "审计显示: 图片类型：0=外链，1=本地")
    public static final String SHOW_IMGTYPE = "imgType";

    /** 审计显示: 本地存储路径 */
    @Schema(description = "审计显示: 本地存储路径")
    public static final String SHOW_IMGSTORAGEPATH = "imgStoragePath";

    /** 审计显示: 排序顺序 */
    @Schema(description = "审计显示: 排序顺序")
    public static final String SHOW_SORTORDER = "sortOrder";

    /** 审计显示: 是否启用 */
    @Schema(description = "审计显示: 是否启用")
    public static final String SHOW_HASACTIVE = "hasActive";

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
