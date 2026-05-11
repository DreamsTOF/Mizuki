package cn.dreamtof.media.infrastructure.persistence.po;

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
 * 相册图片表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "album_photos")
@Schema(name="album_photos",description = "相册图片表")
public class AlbumPhotosPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 所属相册 ID，对应 albums.id
     */
    @Column(value = "album_id")
    @Schema(description = "所属相册 ID，对应 albums.id")
    private UUID albumId;
    /**
     * 图片文件名
     */
    @Column(value = "filename")
    @Schema(description = "图片文件名")
    private String filename;
    /**
     * 图片访问路径
     */
    @Column(value = "url")
    @Schema(description = "图片访问路径")
    private String url;
    /**
     * 图片宽度
     */
    @Column(value = "width")
    @Schema(description = "图片宽度")
    private Integer width;
    /**
     * 图片高度
     */
    @Column(value = "height")
    @Schema(description = "图片高度")
    private Integer height;
    /**
     * 文件大小
     */
    @Column(value = "size")
    @Schema(description = "文件大小")
    private Long size;
    /**
     * MIME 类型
     */
    @Column(value = "mime_type")
    @Schema(description = "MIME 类型")
    private String mimeType;
    /**
     * 是否为封面
     */
    @Column(value = "has_cover")
    @Schema(description = "是否为封面")
    private Boolean hasCover;
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

    /** 审计显示: 所属相册 ID，对应 albums.id */
    @Schema(description = "审计显示: 所属相册 ID，对应 albums.id")
    public static final String SHOW_ALBUMID = "albumId";

    /** 审计显示: 图片文件名 */
    @Schema(description = "审计显示: 图片文件名")
    public static final String SHOW_FILENAME = "filename";

    /** 审计显示: 图片访问路径 */
    @Schema(description = "审计显示: 图片访问路径")
    public static final String SHOW_URL = "url";

    /** 审计显示: 图片宽度 */
    @Schema(description = "审计显示: 图片宽度")
    public static final String SHOW_WIDTH = "width";

    /** 审计显示: 图片高度 */
    @Schema(description = "审计显示: 图片高度")
    public static final String SHOW_HEIGHT = "height";

    /** 审计显示: 文件大小 */
    @Schema(description = "审计显示: 文件大小")
    public static final String SHOW_SIZE = "size";

    /** 审计显示: MIME 类型 */
    @Schema(description = "审计显示: MIME 类型")
    public static final String SHOW_MIMETYPE = "mimeType";

    /** 审计显示: 是否为封面 */
    @Schema(description = "审计显示: 是否为封面")
    public static final String SHOW_HASCOVER = "hasCover";

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
