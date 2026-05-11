package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.api.request.SiteConfigsCursorReq;
import cn.dreamtof.system.api.request.SiteConfigsPageReq;
import cn.dreamtof.system.api.vo.SiteConfigVO;
import cn.dreamtof.system.application.service.SiteConfigsAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "系统管理/站点配置")
@RestController
@RequestMapping("/system/siteConfigs")
@RequiredArgsConstructor
public class SiteConfigsController {

    private final SiteConfigsAppService appService;

    @GetMapping("getByKey")
    public BaseResponse<SiteConfigVO> getByKey(@RequestParam("key") String configKey) {
        return ResultUtils.success(appService.getByKey(configKey));
    }

    @GetMapping("getValue")
    public BaseResponse<String> getValue(@RequestParam("key") String configKey) {
        return ResultUtils.success(appService.getValue(configKey));
    }

    @GetMapping("listByGroup")
    public BaseResponse<List<SiteConfigVO>> listByGroup(@RequestParam("prefix") String groupPrefix) {
        return ResultUtils.success(appService.listByGroup(groupPrefix));
    }

    @GetMapping("listAll")
    public BaseResponse<List<SiteConfigVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("allAsMap")
    public BaseResponse<Map<String, String>> getAllAsMap() {
        return ResultUtils.success(appService.getAllAsMap());
    }

    @PostMapping("create")
    public BaseResponse<SiteConfigVO> create(@RequestParam("key") String configKey,
                                             @RequestParam("value") String configValue,
                                             @RequestParam(value = "description", required = false) String description) {
        return ResultUtils.success(appService.create(configKey, configValue, description));
    }

    @PutMapping("updateByKey")
    public BaseResponse<SiteConfigVO> updateByKey(@RequestParam("key") String configKey,
                                                  @RequestParam("value") String configValue) {
        return ResultUtils.success(appService.updateByKey(configKey, configValue));
    }

    @PostMapping("batchUpdate")
    public BaseResponse<Boolean> batchUpdate(@RequestBody Map<String, String> configMap) {
        appService.batchUpdate(configMap);
        return ResultUtils.success(true);
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PostMapping("page")
    public BaseResponse<PageResult<SiteConfigVO>> page(@RequestBody SiteConfigsPageReq pageRequest) {
        PageReq pageReq = new PageReq();
        pageReq.setPageNum(pageRequest.getPageNum());
        pageReq.setPageSize(pageRequest.getPageSize());
        return ResultUtils.success(appService.page(pageReq));
    }

    @PostMapping("seek")
    public BaseResponse<CursorResult<SiteConfigVO>> seek(@RequestBody SiteConfigsCursorReq cursorReq) {
        UUID cursor = cursorReq.getCursor() != null ? (UUID) cursorReq.getCursor() : null;
        return ResultUtils.success(appService.seek(cursor, cursorReq.getLimit()));
    }
}
