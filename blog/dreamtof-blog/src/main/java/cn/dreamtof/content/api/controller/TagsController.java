package cn.dreamtof.content.api.controller;

import cn.dreamtof.content.api.vo.TagVO;
import cn.dreamtof.content.application.service.TagsAppService;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "内容管理/标签")
@RestController
@RequestMapping("/content/tags")
@RequiredArgsConstructor
public class TagsController {

    private final TagsAppService appService;

    @PostMapping("create")
    public BaseResponse<TagVO> create(
            @RequestParam("name") String name,
            @RequestParam(value = "slug", required = false) String slug) {
        return ResultUtils.success(appService.createTag(name, slug));
    }

    @PutMapping("update/{id}")
    public BaseResponse<TagVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "slug", required = false) String slug) {
        return ResultUtils.success(appService.updateTag(id, name, slug));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteTag(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<TagVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getById(id));
    }

    @GetMapping("getBySlug")
    public BaseResponse<TagVO> getBySlug(@RequestParam("slug") String slug) {
        return ResultUtils.success(appService.getBySlug(slug));
    }

    @GetMapping("list")
    public BaseResponse<List<TagVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<TagVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageTags(pageReq));
    }
}
