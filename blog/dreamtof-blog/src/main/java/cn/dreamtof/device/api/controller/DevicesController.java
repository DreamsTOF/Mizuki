package cn.dreamtof.device.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.device.application.service.DevicesAppService;
import cn.dreamtof.device.domain.model.entity.Devices;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "设备/设备表")
@RestController
@RequestMapping("/device/devices")
@RequiredArgsConstructor
public class DevicesController {

    private final DevicesAppService appService;

    @PostMapping("save")
    public BaseResponse<Devices> save(@RequestBody Devices entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PutMapping("update")
    public BaseResponse<Devices> update(@RequestBody Devices entity) {
        return ResultUtils.success(appService.update(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<Devices> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<Devices>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<Devices>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<Devices>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("listByCategoryId/{categoryId}")
    public BaseResponse<List<Devices>> listByCategoryId(@PathVariable UUID categoryId) {
        return ResultUtils.success(appService.listByCategoryId(categoryId));
    }
}
