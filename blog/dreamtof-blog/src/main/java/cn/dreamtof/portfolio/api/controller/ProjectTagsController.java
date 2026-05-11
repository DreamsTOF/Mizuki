package cn.dreamtof.portfolio.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTags;
import cn.dreamtof.portfolio.application.service.ProjectTagsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * 项目标签关联表 控制器
 * <p>
 * 子表实体，核心操作由 ProjectsController 驱动，
 * 本控制器仅提供基础查询能力。
 * </p>
 */
@Tag(name = "作品集/项目标签关联表")
@RestController
@RequestMapping("/portfolio/projectTags")
@RequiredArgsConstructor
public class ProjectTagsController {

    private final ProjectTagsAppService appService;

    @PostMapping("save")
    public BaseResponse<ProjectTags> save(@RequestBody ProjectTags entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<ProjectTags> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("listByProjectId/{projectId}")
    public BaseResponse<List<ProjectTags>> listByProjectId(@PathVariable UUID projectId) {
        return ResultUtils.success(appService.listByProjectId(projectId));
    }
}
