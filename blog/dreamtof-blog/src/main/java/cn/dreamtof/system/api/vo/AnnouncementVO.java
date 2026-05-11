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
@Schema(name = "AnnouncementVO", description = "公告响应对象")
public class AnnouncementVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "公告标题")
    private String title;
    @Schema(description = "公告内容")
    private String content;
    @Schema(description = "链接文本")
    private String linkText;
    @Schema(description = "链接URL")
    private String linkUrl;
    @Schema(description = "是否外部链接")
    private Boolean hasExternalLink;
    @Schema(description = "是否允许关闭")
    private Boolean hasClosable;
    @Schema(description = "是否启用")
    private Boolean hasEnabled;
    @Schema(description = "开始展示时间")
    private OffsetDateTime startTime;
    @Schema(description = "结束展示时间")
    private OffsetDateTime endTime;
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
