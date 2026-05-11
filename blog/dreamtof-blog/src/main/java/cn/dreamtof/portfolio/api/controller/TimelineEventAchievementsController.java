package cn.dreamtof.portfolio.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventAchievements;
import cn.dreamtof.portfolio.application.service.TimelineEventAchievementsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * 时间线成就关联表 控制器
 * <p>
 * 子表实体，核心操作由 TimelineEventsController 驱动，
 * 本控制器仅提供基础查询能力。
 * </p>
 */
@Tag(name = "作品集/时间线成就关联表")
@RestController
@RequestMapping("/portfolio/timelineEventAchievements")
@RequiredArgsConstructor
public class TimelineEventAchievementsController {

    private final TimelineEventAchievementsAppService appService;

    @PostMapping("save")
    public BaseResponse<TimelineEventAchievements> save(@RequestBody TimelineEventAchievements entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @GetMapping("listByTimelineEventId/{timelineEventId}")
    public BaseResponse<List<TimelineEventAchievements>> listByTimelineEventId(@PathVariable UUID timelineEventId) {
        return ResultUtils.success(appService.listByTimelineEventId(timelineEventId));
    }
}
