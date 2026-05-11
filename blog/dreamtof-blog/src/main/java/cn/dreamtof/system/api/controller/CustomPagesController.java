package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.api.request.CustomPagesCursorReq;
import cn.dreamtof.system.api.request.CustomPagesPageReq;
import cn.dreamtof.system.api.vo.CustomPageVO;
import cn.dreamtof.system.application.service.CustomPagesAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统管理/自定义页面")
@RestController
@RequestMapping("/system/customPages")
@RequiredArgsConstructor
public class CustomPagesController {

    private final CustomPagesAppService appService;

    @PostMapping("create")
    public BaseResponse<CustomPageVO> create(@RequestParam("pageKey") String pageKey,
                                             @RequestParam("title") String title,
                                             @RequestParam(value = "content", required = false) String content,
                                             @RequestParam(value = "description", required = false) String description,
                                             @RequestParam(value = "coverImage", required = false) String coverImage,
                                             @RequestParam(value = "hasCommentEnabled", required = false) Boolean hasCommentEnabled,
                                             @RequestParam(value = "hasEnabled", required = false) Boolean hasEnabled) {
        return ResultUtils.success(appService.create(pageKey, title, content, description, coverImage, hasCommentEnabled, hasEnabled));
    }

    @PutMapping("update/{id}")
    public BaseResponse<CustomPageVO> update(@PathVariable UUID id,
                                             @RequestParam(value = "title", required = false) String title,
                                             @RequestParam(value = "content", required = false) String content,
                                             @RequestParam(value = "description", required = false) String description,
                                             @RequestParam(value = "coverImage", required = false) String coverImage,
                                             @RequestParam(value = "hasCommentEnabled", required = false) Boolean hasCommentEnabled,
                                             @RequestParam(value = "hasEnabled", required = false) Boolean hasEnabled) {
        return ResultUtils.success(appService.update(id, title, content, description, coverImage, hasCommentEnabled, hasEnabled));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<CustomPageVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getById(id));
    }

    @GetMapping("getByPageKey")
    public BaseResponse<CustomPageVO> getByPageKey(@RequestParam("pageKey") String pageKey) {
        return ResultUtils.success(appService.getByPageKey(pageKey));
    }

    @GetMapping("list")
    public BaseResponse<List<CustomPageVO>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @GetMapping("listEnabled")
    public BaseResponse<List<CustomPageVO>> listEnabled() {
        return ResultUtils.success(appService.listEnabled());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<CustomPageVO>> page(@RequestBody CustomPagesPageReq pageRequest) {
        PageReq pageReq = new PageReq();
        pageReq.setPageNum(pageRequest.getPageNum());
        pageReq.setPageSize(pageRequest.getPageSize());
        return ResultUtils.success(appService.page(pageReq));
    }

    @PostMapping("seek")
    public BaseResponse<CursorResult<CustomPageVO>> seek(@RequestBody CustomPagesCursorReq cursorReq) {
        UUID cursor = cursorReq.getCursor() != null ? (UUID) cursorReq.getCursor() : null;
        return ResultUtils.success(appService.seek(cursor, cursorReq.getLimit()));
    }
}
