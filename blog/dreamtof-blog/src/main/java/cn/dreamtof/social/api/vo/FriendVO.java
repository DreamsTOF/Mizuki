package cn.dreamtof.social.api.vo;

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
@Schema(name = "FriendVO", description = "友链响应对象")
public class FriendVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "站点名称")
    private String title;

    @Schema(description = "头像/Logo图片URL")
    private String imgurl;

    @Schema(description = "站点描述")
    private String desc;

    @Schema(description = "网站链接")
    private String siteurl;

    @Schema(description = "关联标签列表")
    private List<String> tags;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "是否启用")
    private Boolean hasActive;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;
}
