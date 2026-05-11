package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.application.service.DailyStatsAppService;
import cn.dreamtof.system.domain.model.entity.DailyStats;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "系统管理/每日统计汇总表")
@RestController
@RequestMapping("/system/dailyStats")
@RequiredArgsConstructor
public class DailyStatsController {

    private final DailyStatsAppService appService;

    @PostMapping("save")
    public BaseResponse<DailyStats> save(@RequestBody DailyStats entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PutMapping("update")
    public BaseResponse<DailyStats> update(@RequestBody DailyStats entity) {
        return ResultUtils.success(appService.update(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<DailyStats> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<DailyStats>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<DailyStats>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<DailyStats>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("range")
    public BaseResponse<List<DailyStats>> listByDateRange(
            @RequestParam OffsetDateTime startDate,
            @RequestParam OffsetDateTime endDate) {
        return ResultUtils.success(appService.listByDateRange(startDate, endDate));
    }
}
