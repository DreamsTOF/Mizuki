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
 * 文件上传记录表 实体类
 *
 * @author lyl
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "uploaded_files")
@Schema(name="uploaded_files",description = "文件上传记录表")
public class UploadedFilesPO implements Serializable, IdAudit, CreatedTimeAudit, UpdatedTimeAudit, VersionAudit{

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
     * 原始文件名
     */
    @Column(value = "original_name")
    @Schema(description = "原始文件名")
    private String originalName;
    /**
     * 存储文件名
     */
    @Column(value = "stored_name")
    @Schema(description = "存储文件名")
    private String storedName;
    /**
     * 存储路径
     */
    @Column(value = "storage_path")
    @Schema(description = "存储路径")
    private String storagePath;
    /**
     * 可访问 URL
     */
    @Column(value = "url")
    @Schema(description = "可访问 URL")
    private String url;
    /**
     * 目标目录类型
     */
    @Column(value = "folder")
    @Schema(description = "目标目录类型")
    private String folder;
    /**
     * 文件大小
     */
    @Column(value = "file_size")
    @Schema(description = "文件大小")
    private Long fileSize;
    /**
     * MIME 类型
     */
    @Column(value = "mime_type")
    @Schema(description = "MIME 类型")
    private String mimeType;
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
     * 缩略图 URL
     */
    @Column(value = "thumbnail_url")
    @Schema(description = "缩略图 URL")
    private String thumbnailUrl;
    /**
     * 额外元数据
     */
    @Column(value = "metadata", typeHandler = UniversalJsonTypeHandler.class)
    @Schema(description = "额外元数据")
    private String metadata;
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

    /** 审计显示: 原始文件名 */
    @Schema(description = "审计显示: 原始文件名")
    public static final String SHOW_ORIGINALNAME = "originalName";

    /** 审计显示: 存储文件名 */
    @Schema(description = "审计显示: 存储文件名")
    public static final String SHOW_STOREDNAME = "storedName";

    /** 审计显示: 存储路径 */
    @Schema(description = "审计显示: 存储路径")
    public static final String SHOW_STORAGEPATH = "storagePath";

    /** 审计显示: 可访问 URL */
    @Schema(description = "审计显示: 可访问 URL")
    public static final String SHOW_URL = "url";

    /** 审计显示: 目标目录类型 */
    @Schema(description = "审计显示: 目标目录类型")
    public static final String SHOW_FOLDER = "folder";

    /** 审计显示: 文件大小 */
    @Schema(description = "审计显示: 文件大小")
    public static final String SHOW_FILESIZE = "fileSize";

    /** 审计显示: MIME 类型 */
    @Schema(description = "审计显示: MIME 类型")
    public static final String SHOW_MIMETYPE = "mimeType";

    /** 审计显示: 图片宽度 */
    @Schema(description = "审计显示: 图片宽度")
    public static final String SHOW_WIDTH = "width";

    /** 审计显示: 图片高度 */
    @Schema(description = "审计显示: 图片高度")
    public static final String SHOW_HEIGHT = "height";

    /** 审计显示: 缩略图 URL */
    @Schema(description = "审计显示: 缩略图 URL")
    public static final String SHOW_THUMBNAILURL = "thumbnailUrl";

    /** 审计显示: 额外元数据 */
    @Schema(description = "审计显示: 额外元数据")
    public static final String SHOW_METADATA = "metadata";

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
