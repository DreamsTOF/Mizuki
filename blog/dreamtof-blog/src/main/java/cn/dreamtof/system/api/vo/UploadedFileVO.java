package cn.dreamtof.system.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UploadedFileVO", description = "文件上传响应对象")
public class UploadedFileVO {

    @Schema(description = "文件记录 ID")
    private UUID id;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "可访问 URL")
    private String url;

    @Schema(description = "目标目录")
    private String folder;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "图片宽度")
    private Integer width;

    @Schema(description = "图片高度")
    private Integer height;

    @Schema(description = "缩略图 URL")
    private String thumbnailUrl;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
