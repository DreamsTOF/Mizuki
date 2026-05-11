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
@Schema(name = "PostVO", description = "文章列表响应对象")
public class PostVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "文章标题")
    private String title;
    @Schema(description = "URL 友好标识符")
    private String slug;
    @Schema(description = "自动生成的摘要")
    private String excerpt;
    @Schema(description = "文章描述/SEO 摘要")
    private String description;
    @Schema(description = "作者名")
    private String author;
    @Schema(description = "分类名称")
    private String category;
    @Schema(description = "语言代码")
    private String lang;
    @Schema(description = "是否为草稿状态")
    private Boolean draft;
    @Schema(description = "是否置顶")
    private Boolean pinned;
    @Schema(description = "置顶优先级")
    private Integer priority;
    @Schema(description = "是否加密")
    private Boolean encrypted;
    @Schema(description = "密码提示信息")
    private String passwordHint;
    @Schema(description = "封面图片路径")
    private String image;
    @Schema(description = "是否启用评论")
    private Boolean comment;
    @Schema(description = "发布日期时间")
    private OffsetDateTime published;
    @Schema(description = "浏览次数")
    private Long viewCount;
    @Schema(description = "字数统计")
    private Integer wordCount;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
