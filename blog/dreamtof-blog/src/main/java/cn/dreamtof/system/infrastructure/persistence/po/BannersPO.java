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
 * 横幅图片表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "banners")
@Schema(name="banners",description = "横幅图片表")
public class BannersPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 图片标题
     */
    @Column(value = "title")
    @Schema(description = "图片标题")
    private String title;
    /**
     * 图片 URL
     */
    @Column(value = "image_url")
    @Schema(description = "图片 URL")
    private String imageUrl;
    /**
     * 适用设备类型
     */
    @Column(value = "device_type")
    @Schema(description = "适用设备类型")
    private String deviceType;
    /**
     * 展示位置
     */
    @Column(value = "position")
    @Schema(description = "展示位置")
    private String position;
    /**
     * 排序顺序
     */
    @Column(value = "sort_order")
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    /**
     * 是否轮播
     */
    @Column(value = "has_carousel")
    @Schema(description = "是否轮播")
    private Boolean hasCarousel;
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

    /** 审计显示: 图片标题 */
    @Schema(description = "审计显示: 图片标题")
    public static final String SHOW_TITLE = "title";

    /** 审计显示: 图片 URL */
    @Schema(description = "审计显示: 图片 URL")
    public static final String SHOW_IMAGEURL = "imageUrl";

    /** 审计显示: 适用设备类型 */
    @Schema(description = "审计显示: 适用设备类型")
    public static final String SHOW_DEVICETYPE = "deviceType";

    /** 审计显示: 展示位置 */
    @Schema(description = "审计显示: 展示位置")
    public static final String SHOW_POSITION = "position";

    /** 审计显示: 排序顺序 */
    @Schema(description = "审计显示: 排序顺序")
    public static final String SHOW_SORTORDER = "sortOrder";

    /** 审计显示: 是否轮播 */
    @Schema(description = "审计显示: 是否轮播")
    public static final String SHOW_HASCAROUSEL = "hasCarousel";

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
