package cn.dreamtof.content.api.controller;

import cn.dreamtof.content.api.vo.DiaryVO;
import cn.dreamtof.content.application.service.DiaryEntriesAppService;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Tag(name = "内容管理/日记")
@RestController
@RequestMapping("/content/diaries")
@RequiredArgsConstructor
public class DiaryEntriesController {

    private final DiaryEntriesAppService appService;

    @PostMapping("create")
    public BaseResponse<DiaryVO> create(
            @RequestParam("content") String content,
            @RequestParam(value = "entryDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime entryDate,
            @RequestParam(value = "images", required = false) String images,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "mood", required = false) String mood,
            @RequestParam(value = "tags", required = false) String tags) {
        return ResultUtils.success(appService.createDiary(content, entryDate, images, location, mood, tags));
    }

    @PutMapping("update/{id}")
    public BaseResponse<DiaryVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "entryDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime entryDate,
            @RequestParam(value = "images", required = false) String images,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "mood", required = false) String mood,
            @RequestParam(value = "tags", required = false) String tags) {
        return ResultUtils.success(appService.updateDiary(id, content, entryDate, images, location, mood, tags));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deleteDiary(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<DiaryVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getById(id));
    }

    @GetMapping("listByDateRange")
    public BaseResponse<List<DiaryVO>> listByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        return ResultUtils.success(appService.listByDateRange(startDate, endDate));
    }

    @PostMapping("page")
    public BaseResponse<PageResult<DiaryVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pageDiaries(pageReq));
    }
}
