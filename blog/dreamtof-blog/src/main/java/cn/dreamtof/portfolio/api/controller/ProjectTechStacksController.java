package cn.dreamtof.portfolio.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTechStacks;
import cn.dreamtof.portfolio.application.service.ProjectTechStacksAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * 项目技术栈关联表 控制器
 * <p>
 * 子表实体，核心操作由 ProjectsController 驱动，
 * 本控制器仅提供基础查询能力。
 * </p>
 */
@Tag(name = "作品集/项目技术栈关联表")
@RestController
@RequestMapping("/portfolio/projectTechStacks")
@RequiredArgsConstructor
public class ProjectTechStacksController {

    private final ProjectTechStacksAppService appService;

    @PostMapping("save")
    public BaseResponse<ProjectTechStacks> save(@RequestBody ProjectTechStacks entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<ProjectTechStacks> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("listByProjectId/{projectId}")
    public BaseResponse<List<ProjectTechStacks>> listByProjectId(@PathVariable UUID projectId) {
        return ResultUtils.success(appService.listByProjectId(projectId));
    }
}
