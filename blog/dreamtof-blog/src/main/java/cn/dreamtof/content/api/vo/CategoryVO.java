package cn.dreamtof.content.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategoryVO", description = "分类响应对象")
public class CategoryVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "URL 友好的分类标识")
    private String slug;
    @Schema(description = "分类描述")
    private String description;
    @Schema(description = "父分类 ID")
    private UUID parentId;
    @Schema(description = "分类图标")
    private String icon;
    @Schema(description = "分类封面图片")
    private String coverImage;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
