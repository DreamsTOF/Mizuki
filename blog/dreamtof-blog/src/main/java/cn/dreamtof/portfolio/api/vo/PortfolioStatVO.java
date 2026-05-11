package cn.dreamtof.portfolio.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PortfolioStatVO", description = "作品集统计响应对象")
public class PortfolioStatVO {

    @Schema(description = "项目总数")
    private long projectCount;
    @Schema(description = "精选项目数")
    private long featuredProjectCount;
    @Schema(description = "技能总数")
    private long skillCount;
    @Schema(description = "时间线事件总数")
    private long timelineEventCount;
}
