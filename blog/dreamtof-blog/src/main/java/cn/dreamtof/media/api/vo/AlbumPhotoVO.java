package cn.dreamtof.media.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AlbumPhotoVO", description = "相册图片响应对象")
public class AlbumPhotoVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "所属相册ID")
    private UUID albumId;
    @Schema(description = "图片文件名")
    private String filename;
    @Schema(description = "图片访问路径")
    private String url;
    @Schema(description = "图片宽度")
    private Integer width;
    @Schema(description = "图片高度")
    private Integer height;
    @Schema(description = "文件大小")
    private Long size;
    @Schema(description = "MIME类型")
    private String mimeType;
    @Schema(description = "是否为封面")
    private Boolean hasCover;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
