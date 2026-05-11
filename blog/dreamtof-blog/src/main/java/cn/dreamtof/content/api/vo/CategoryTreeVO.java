package cn.dreamtof.content.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategoryTreeVO", description = "分类树响应对象")
public class CategoryTreeVO {

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
    @Schema(description = "子分类列表")
    private List<CategoryTreeVO> children;
}
