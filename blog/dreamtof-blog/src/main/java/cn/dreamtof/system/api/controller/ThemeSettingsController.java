package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.api.vo.ThemeSettingVO;
import cn.dreamtof.system.application.service.ThemeSettingsAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统/主题设置")
@RestController
@RequestMapping("/system/themeSettings")
@RequiredArgsConstructor
public class ThemeSettingsController {

    private final ThemeSettingsAppService appService;

    @PostMapping("save")
    public BaseResponse<ThemeSettingVO> save(
            @RequestParam("settingKey") String settingKey,
            @RequestParam(value = "settingValue", required = false) String settingValue,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "hasUserCustomizable", required = false) Boolean hasUserCustomizable) {
        return ResultUtils.success(appService.createSetting(settingKey, settingValue, description, hasUserCustomizable));
    }

    @PutMapping("update/{id}")
    public BaseResponse<ThemeSettingVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "settingValue", required = false) String settingValue,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "hasUserCustomizable", required = false) Boolean hasUserCustomizable) {
        return ResultUtils.success(appService.updateSetting(id, settingValue, description, hasUserCustomizable));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteSetting(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<ThemeSettingVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("getByKey")
    public BaseResponse<ThemeSettingVO> getByKey(@RequestParam("settingKey") String settingKey) {
        return ResultUtils.success(appService.getByKey(settingKey));
    }

    @GetMapping("list")
    public BaseResponse<List<ThemeSettingVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("listUserCustomizable")
    public BaseResponse<List<ThemeSettingVO>> listUserCustomizable() {
        return ResultUtils.success(appService.listUserCustomizable());
    }

    @PutMapping("batchUpdate")
    public BaseResponse<Boolean> batchUpdate(@RequestBody List<ThemeSettingVO> settings) {
        appService.batchUpdate(settings);
        return ResultUtils.success(true);
    }
}
