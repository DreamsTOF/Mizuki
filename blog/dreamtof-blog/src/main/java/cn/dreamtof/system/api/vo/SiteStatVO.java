package cn.dreamtof.system.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "SiteStatVO", description = "站点统计响应对象")
public class SiteStatVO {

    @Schema(description = "总页面浏览量")
    private long totalPageViews;
    @Schema(description = "今日页面浏览量")
    private long todayPageViews;
    @Schema(description = "总文章数")
    private long totalPosts;
    @Schema(description = "总评论数")
    private long totalComments;
    @Schema(description = "总友链数")
    private long totalFriends;
}
