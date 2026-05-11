package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.api.vo.NavLinkVO;
import cn.dreamtof.system.application.service.NavLinksAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统/导航链接")
@RestController
@RequestMapping("/system/navLinks")
@RequiredArgsConstructor
public class NavLinksController {

    private final NavLinksAppService appService;

    @PostMapping("save")
    public BaseResponse<NavLinkVO> save(
            @RequestParam("name") String name,
            @RequestParam("url") String url,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "hasExternal", required = false) Boolean hasExternal,
            @RequestParam(value = "hasNewWindow", required = false) Boolean hasNewWindow,
            @RequestParam(value = "parentId", required = false) UUID parentId,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
        return ResultUtils.success(appService.createNavLink(name, url, icon, hasExternal,
                hasNewWindow, parentId, position, sortOrder));
    }

    @PutMapping("update/{id}")
    public BaseResponse<NavLinkVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "hasExternal", required = false) Boolean hasExternal,
            @RequestParam(value = "hasNewWindow", required = false) Boolean hasNewWindow,
            @RequestParam(value = "parentId", required = false) UUID parentId,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {
        return ResultUtils.success(appService.updateNavLink(id, name, url, icon, hasExternal,
                hasNewWindow, parentId, position, sortOrder));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteNavLink(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<NavLinkVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<NavLinkVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("listByPosition")
    public BaseResponse<List<NavLinkVO>> listByPosition(@RequestParam("position") String position) {
        return ResultUtils.success(appService.listByPosition(position));
    }

    @GetMapping("tree")
    public BaseResponse<List<NavLinkVO>> listTree() {
        return ResultUtils.success(appService.listTree());
    }

    @GetMapping("treeByPosition")
    public BaseResponse<List<NavLinkVO>> listTreeByPosition(@RequestParam("position") String position) {
        return ResultUtils.success(appService.listTreeByPosition(position));
    }

    @PutMapping("toggleEnabled/{id}")
    public BaseResponse<NavLinkVO> toggleEnabled(
            @PathVariable UUID id,
            @RequestParam("enabled") boolean enabled) {
        return ResultUtils.success(appService.toggleEnabled(id, enabled));
    }

    @PostMapping("page")
    public BaseResponse<PageResult<NavLinkVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageNavLinks(pageReq));
    }
}
