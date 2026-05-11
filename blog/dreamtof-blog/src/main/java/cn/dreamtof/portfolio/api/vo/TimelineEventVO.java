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
@Schema(name = "TimelineEventVO", description = "时间线事件响应对象")
public class TimelineEventVO {

    @Schema(description = "ID")
    private UUID id;

    @Schema(description = "事件标题")
    private String title;

    @Schema(description = "事件描述")
    private String description;

    @Schema(description = "事件类型")
    private String type;

    @Schema(description = "图标标识符")
    private String icon;

    @Schema(description = "颜色值")
    private String color;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "所属机构")
    private String organization;

    @Schema(description = "职位/角色")
    private String position;

    @Schema(description = "是否重点展示")
    private Boolean featured;

    @Schema(description = "关联技能名称列表")
    private List<String> skills;

    @Schema(description = "关联成就列表")
    private List<String> achievements;

    @Schema(description = "关联链接列表")
    private List<TimelineLinkVO> links;

    @Schema(description = "创建时间")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    private OffsetDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "TimelineLinkVO", description = "时间线链接响应对象")
    public static class TimelineLinkVO {
        @Schema(description = "链接名称")
        private String name;
        @Schema(description = "链接地址")
        private String url;
        @Schema(description = "链接类型")
        private String type;
    }
}
