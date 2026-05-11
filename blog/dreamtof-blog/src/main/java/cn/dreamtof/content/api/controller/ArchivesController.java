package cn.dreamtof.content.api.controller;

import cn.dreamtof.content.api.vo.ArchiveVO;
import cn.dreamtof.content.application.service.ArchivesAppService;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "内容管理/归档")
@RestController
@RequestMapping("/content/archives")
@RequiredArgsConstructor
public class ArchivesController {

    private final ArchivesAppService appService;

    @PostMapping("rebuild")
    public BaseResponse<Boolean> rebuild() {
        appService.rebuildArchives();
        return ResultUtils.success(true);
    }

    @GetMapping("listByYear")
    public BaseResponse<List<ArchiveVO>> listByYear(@RequestParam("year") Integer year) {
        return ResultUtils.success(appService.listByYear(year));
    }

    @GetMapping("list")
    public BaseResponse<List<ArchiveVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }
}
