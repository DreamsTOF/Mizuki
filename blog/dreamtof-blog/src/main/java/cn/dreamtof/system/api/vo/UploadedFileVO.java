package cn.dreamtof.system.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(name = "UploadedFileVO", description = "文件上传结果")
public class UploadedFileVO {

    @Schema(description = "文件记录ID")
    private UUID id;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "可访问URL")
    private String url;

    @Schema(description = "目录类型")
    private String folder;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "图片宽度")
    private Integer width;

    @Schema(description = "图片高度")
    private Integer height;

    @Schema(description = "缩略图URL")
    private String thumbnailUrl;
}
