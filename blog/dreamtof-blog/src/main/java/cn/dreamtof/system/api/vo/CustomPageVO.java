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
@Schema(name = "CustomPageVO", description = "自定义页面响应对象")
public class CustomPageVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "页面唯一标识")
    private String pageKey;

    @Schema(description = "页面标题")
    private String title;

    @Schema(description = "页面内容")
    private String content;

    @Schema(description = "页面描述")
    private String description;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "是否允许评论")
    private Boolean hasCommentEnabled;

    @Schema(description = "是否启用")
    private Boolean hasEnabled;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
