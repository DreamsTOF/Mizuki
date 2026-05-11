package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.application.service.AnnouncementsAppService;
import cn.dreamtof.system.domain.model.entity.Announcements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统管理/公告表")
@RestController
@RequestMapping("/system/announcements")
@RequiredArgsConstructor
public class AnnouncementsController {

    private final AnnouncementsAppService appService;

    @PostMapping("save")
    public BaseResponse<Announcements> save(@RequestBody Announcements entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PutMapping("update")
    public BaseResponse<Announcements> update(@RequestBody Announcements entity) {
        return ResultUtils.success(appService.update(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<Announcements> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<Announcements>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<Announcements>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<Announcements>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("active")
    public BaseResponse<List<Announcements>> listActive() {
        return ResultUtils.success(appService.listActive());
    }
}
