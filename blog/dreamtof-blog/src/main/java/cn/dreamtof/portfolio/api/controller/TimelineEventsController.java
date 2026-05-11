package cn.dreamtof.portfolio.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.portfolio.api.vo.TimelineEventVO;
import cn.dreamtof.portfolio.application.service.TimelineEventsAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "作品集/时间线")
@RestController
@RequestMapping("/portfolio/timelineEvents")
@RequiredArgsConstructor
public class TimelineEventsController {

    private final TimelineEventsAppService appService;

    @PostMapping("save")
    public BaseResponse<TimelineEventVO> save(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "startDate", required = false) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) OffsetDateTime endDate,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "organization", required = false) String organization,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "hasFeatured", required = false) Boolean hasFeatured) {
        return ResultUtils.success(appService.createEvent(title, description, eventType, icon, color,
                startDate, endDate, location, organization, position, hasFeatured,
                null, null, null));
    }

    @PutMapping("update/{id}")
    public BaseResponse<TimelineEventVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "startDate", required = false) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) OffsetDateTime endDate,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "organization", required = false) String organization,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "hasFeatured", required = false) Boolean hasFeatured) {
        return ResultUtils.success(appService.updateEvent(id, title, description, eventType, icon, color,
                startDate, endDate, location, organization, position, hasFeatured,
                null, null, null));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteEvent(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<TimelineEventVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<TimelineEventVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("listByEventType")
    public BaseResponse<List<TimelineEventVO>> listByEventType(@RequestParam("eventType") String eventType) {
        return ResultUtils.success(appService.listByEventType(eventType));
    }

    @GetMapping("eventTypes")
    public BaseResponse<List<String>> listEventTypes() {
        return ResultUtils.success(appService.listEventTypes());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<TimelineEventVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageEvents(pageReq));
    }
}
