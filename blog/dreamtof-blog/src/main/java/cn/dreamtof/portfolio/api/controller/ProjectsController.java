package cn.dreamtof.portfolio.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.portfolio.api.vo.ProjectVO;
import cn.dreamtof.portfolio.application.service.ProjectsAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "作品集/项目")
@RestController
@RequestMapping("/portfolio/projects")
@RequiredArgsConstructor
public class ProjectsController {

    private final ProjectsAppService appService;

    @PostMapping("save")
    public BaseResponse<ProjectVO> save(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) String image,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "liveDemoUrl", required = false) String liveDemoUrl,
            @RequestParam(value = "sourceCodeUrl", required = false) String sourceCodeUrl,
            @RequestParam(value = "visitUrl", required = false) String visitUrl,
            @RequestParam(value = "startDate", required = false) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) OffsetDateTime endDate,
            @RequestParam(value = "hasFeatured", required = false) Boolean hasFeatured,
            @RequestParam(value = "hasShowImage", required = false) Boolean hasShowImage,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
            @RequestParam(value = "techStacks", required = false) List<String> techStacks,
            @RequestParam(value = "tags", required = false) List<String> tags) {
        return ResultUtils.success(appService.createProject(title, description, image, category,
                status, liveDemoUrl, sourceCodeUrl, visitUrl, startDate, endDate,
                hasFeatured, hasShowImage, sortOrder, techStacks, tags));
    }

    @PutMapping("update/{id}")
    public BaseResponse<ProjectVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) String image,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "liveDemoUrl", required = false) String liveDemoUrl,
            @RequestParam(value = "sourceCodeUrl", required = false) String sourceCodeUrl,
            @RequestParam(value = "visitUrl", required = false) String visitUrl,
            @RequestParam(value = "startDate", required = false) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) OffsetDateTime endDate,
            @RequestParam(value = "hasShowImage", required = false) Boolean hasShowImage,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
            @RequestParam(value = "techStacks", required = false) List<String> techStacks,
            @RequestParam(value = "tags", required = false) List<String> tags) {
        return ResultUtils.success(appService.updateProject(id, title, description, image, category,
                status, liveDemoUrl, sourceCodeUrl, visitUrl, startDate, endDate,
                hasShowImage, sortOrder, techStacks, tags));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteProject(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<ProjectVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<ProjectVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("listByCategory")
    public BaseResponse<List<ProjectVO>> listByCategory(@RequestParam("category") String category) {
        return ResultUtils.success(appService.listByCategory(category));
    }

    @GetMapping("categories")
    public BaseResponse<List<String>> listCategories() {
        return ResultUtils.success(appService.listCategories());
    }

    @PutMapping("toggleFeatured/{id}")
    public BaseResponse<ProjectVO> toggleFeatured(
            @PathVariable UUID id,
            @RequestParam("featured") boolean featured) {
        return ResultUtils.success(appService.toggleFeatured(id, featured));
    }

    @PostMapping("page")
    public BaseResponse<PageResult<ProjectVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageProjects(pageReq));
    }
}
