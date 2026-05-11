package cn.dreamtof.content.api.controller;

import cn.dreamtof.content.api.vo.CategoryTreeVO;
import cn.dreamtof.content.api.vo.CategoryVO;
import cn.dreamtof.content.application.service.CategoriesAppService;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "内容管理/分类")
@RestController
@RequestMapping("/content/categories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesAppService appService;

    @PostMapping("create")
    public BaseResponse<CategoryVO> create(
            @RequestParam("name") String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "parentId", required = false) UUID parentId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "coverImage", required = false) String coverImage,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
            @RequestParam(value = "hasEnabled", required = false) Boolean hasEnabled) {
        return ResultUtils.success(appService.createCategory(name, slug, parentId, description,
                icon, coverImage, sortOrder, hasEnabled));
    }

    @PutMapping("update/{id}")
    public BaseResponse<CategoryVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "parentId", required = false) UUID parentId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "coverImage", required = false) String coverImage,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
            @RequestParam(value = "hasEnabled", required = false) Boolean hasEnabled) {
        return ResultUtils.success(appService.updateCategory(id, name, slug, parentId, description,
                icon, coverImage, sortOrder, hasEnabled));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteCategory(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<CategoryVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getById(id));
    }

    @GetMapping("getBySlug")
    public BaseResponse<CategoryVO> getBySlug(@RequestParam("slug") String slug) {
        return ResultUtils.success(appService.getBySlug(slug));
    }

    @GetMapping("list")
    public BaseResponse<List<CategoryVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("tree")
    public BaseResponse<List<CategoryTreeVO>> tree() {
        return ResultUtils.success(appService.buildCategoryTree());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<CategoryVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageCategories(pageReq));
    }
}
