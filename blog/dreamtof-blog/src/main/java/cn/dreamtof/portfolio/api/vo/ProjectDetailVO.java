package cn.dreamtof.portfolio.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ProjectDetailVO", description = "项目详情响应对象")
public class ProjectDetailVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "项目标题")
    private String title;
    @Schema(description = "项目描述")
    private String description;
    @Schema(description = "封面图片路径")
    private String image;
    @Schema(description = "项目类别")
    private String category;
    @Schema(description = "项目状态")
    private String status;
    @Schema(description = "在线演示地址")
    private String liveDemoUrl;
    @Schema(description = "源码仓库地址")
    private String sourceCodeUrl;
    @Schema(description = "项目主页地址")
    private String visitUrl;
    @Schema(description = "开始日期")
    private OffsetDateTime startDate;
    @Schema(description = "结束日期")
    private OffsetDateTime endDate;
    @Schema(description = "是否精选")
    private Boolean hasFeatured;
    @Schema(description = "是否显示封面")
    private Boolean hasShowImage;
    @Schema(description = "排序权重")
    private Integer sortOrder;
    @Schema(description = "技术栈列表")
    private List<String> techStacks;
    @Schema(description = "标签列表")
    private List<String> tags;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
