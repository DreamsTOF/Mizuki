package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.api.vo.BannerVO;
import cn.dreamtof.system.application.service.BannersAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统/横幅广告")
@RestController
@RequestMapping("/system/banners")
@RequiredArgsConstructor
public class BannersController {

    private final BannersAppService appService;

    @PostMapping("save")
    public BaseResponse<BannerVO> save(
            @RequestParam("title") String title,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
            @RequestParam(value = "hasCarousel", required = false) Boolean hasCarousel) {
        return ResultUtils.success(appService.createBanner(title, imageUrl, deviceType,
                position, sortOrder, hasCarousel));
    }

    @PutMapping("update/{id}")
    public BaseResponse<BannerVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
        return ResultUtils.success(appService.updateBanner(id, title, imageUrl, deviceType,
                position, sortOrder));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteBanner(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<BannerVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<BannerVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("listByPosition")
    public BaseResponse<List<BannerVO>> listByPosition(@RequestParam("position") String position) {
        return ResultUtils.success(appService.listByPosition(position));
    }

    @GetMapping("listEnabled")
    public BaseResponse<List<BannerVO>> listEnabled() {
        return ResultUtils.success(appService.listEnabled());
    }

    @GetMapping("listCarousel")
    public BaseResponse<List<BannerVO>> listCarousel() {
        return ResultUtils.success(appService.listCarousel());
    }

    @PutMapping("toggleEnabled/{id}")
    public BaseResponse<BannerVO> toggleEnabled(
            @PathVariable UUID id,
            @RequestParam("enabled") boolean enabled) {
        return ResultUtils.success(appService.toggleEnabled(id, enabled));
    }

    @PutMapping("toggleCarousel/{id}")
    public BaseResponse<BannerVO> toggleCarousel(
            @PathVariable UUID id,
            @RequestParam("carousel") boolean carousel) {
        return ResultUtils.success(appService.toggleCarousel(id, carousel));
    }

    @PostMapping("page")
    public BaseResponse<PageResult<BannerVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageBanners(pageReq));
    }
}
