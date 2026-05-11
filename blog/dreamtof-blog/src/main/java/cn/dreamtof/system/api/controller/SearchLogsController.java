package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.application.service.SearchLogsAppService;
import cn.dreamtof.system.domain.model.entity.SearchLogs;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统管理/搜索记录表")
@RestController
@RequestMapping("/system/searchLogs")
@RequiredArgsConstructor
public class SearchLogsController {

    private final SearchLogsAppService appService;

    @PostMapping("save")
    public BaseResponse<SearchLogs> save(@RequestBody SearchLogs entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<SearchLogs> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<SearchLogs>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<SearchLogs>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<SearchLogs>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("hotKeywords")
    public BaseResponse<List<Object[]>> getHotKeywords(
            @RequestParam(defaultValue = "10") int limit) {
        return ResultUtils.success(appService.getHotKeywords(limit));
    }
}
