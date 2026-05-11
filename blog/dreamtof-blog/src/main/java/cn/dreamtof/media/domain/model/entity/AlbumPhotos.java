package cn.dreamtof.media.domain.model.entity;

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
 * 相册图片表 领域实体
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
@Schema(name="AlbumPhotos", description = "相册图片表 领域实体")
public class AlbumPhotos implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
     @Schema(description = "ID")
     private UUID id;
    /**
     * 所属相册 ID，对应 albums.id
     */
     @Schema(description = "所属相册 ID，对应 albums.id")
     private UUID albumId;
    /**
     * 图片文件名
     */
     @Schema(description = "图片文件名")
     private String filename;
    /**
     * 图片访问路径
     */
     @Schema(description = "图片访问路径")
     private String url;
    /**
     * 图片宽度
     */
     @Schema(description = "图片宽度")
     private Integer width;
    /**
     * 图片高度
     */
     @Schema(description = "图片高度")
     private Integer height;
    /**
     * 文件大小
     */
     @Schema(description = "文件大小")
     private Long size;
    /**
     * MIME 类型
     */
     @Schema(description = "MIME 类型")
     private String mimeType;
    /**
     * 是否为封面
     */
     @Schema(description = "是否为封面")
     private Boolean hasCover;
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