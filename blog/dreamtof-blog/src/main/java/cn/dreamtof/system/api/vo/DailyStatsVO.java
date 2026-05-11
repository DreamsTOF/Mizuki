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
@Schema(name = "DailyStatsVO", description = "每日统计响应对象")
public class DailyStatsVO {

    @Schema(description = "ID")
    private UUID id;
    @Schema(description = "统计日期")
    private OffsetDateTime statDate;
    @Schema(description = "页面浏览量")
    private Long pageViews;
    @Schema(description = "独立访客数")
    private Long uniqueVisitors;
    @Schema(description = "文章阅读量")
    private Long postReads;
    @Schema(description = "评论数")
    private Integer commentCount;
    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;
}
