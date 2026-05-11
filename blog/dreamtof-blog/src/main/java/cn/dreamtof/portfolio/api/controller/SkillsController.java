package cn.dreamtof.portfolio.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.portfolio.api.vo.SkillVO;
import cn.dreamtof.portfolio.application.service.SkillsAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "作品集/技能")
@RestController
@RequestMapping("/portfolio/skills")
@RequiredArgsConstructor
public class SkillsController {

    private final SkillsAppService appService;

    @PostMapping("save")
    public BaseResponse<SkillVO> save(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "experienceYears", required = false) Integer experienceYears,
            @RequestParam(value = "experienceMonths", required = false) Integer experienceMonths,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "projects", required = false) String projects,
            @RequestParam(value = "certifications", required = false) String certifications) {
        return ResultUtils.success(appService.createSkill(name, description, icon, category,
                level, experienceYears, experienceMonths, color, projects, certifications));
    }

    @PutMapping("update/{id}")
    public BaseResponse<SkillVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "experienceYears", required = false) Integer experienceYears,
            @RequestParam(value = "experienceMonths", required = false) Integer experienceMonths,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "projects", required = false) String projects,
            @RequestParam(value = "certifications", required = false) String certifications) {
        return ResultUtils.success(appService.updateSkill(id, name, description, icon, category,
                level, experienceYears, experienceMonths, color, projects, certifications));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteSkill(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<SkillVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<SkillVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("listByCategory")
    public BaseResponse<List<SkillVO>> listByCategory(@RequestParam("category") String category) {
        return ResultUtils.success(appService.listByCategory(category));
    }

    @GetMapping("categories")
    public BaseResponse<List<String>> listCategories() {
        return ResultUtils.success(appService.listCategories());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<SkillVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageSkills(pageReq));
    }
}
