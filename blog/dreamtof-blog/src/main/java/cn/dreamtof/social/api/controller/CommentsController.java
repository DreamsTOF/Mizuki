package cn.dreamtof.social.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.social.application.service.CommentsAppService;
import cn.dreamtof.social.domain.model.entity.Comments;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "社交/评论表")
@RestController
@RequestMapping("/social/comments")
@RequiredArgsConstructor
public class CommentsController {

    private final CommentsAppService appService;

    @PostMapping("save")
    public BaseResponse<Comments> save(@RequestBody Comments entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PutMapping("update")
    public BaseResponse<Comments> update(@RequestBody Comments entity) {
        return ResultUtils.success(appService.update(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<Comments> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<Comments>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<Comments>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<Comments>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("listByTargetId/{targetId}")
    public BaseResponse<List<Comments>> listByTargetId(@PathVariable UUID targetId) {
        return ResultUtils.success(appService.listByTargetId(targetId));
    }

    @GetMapping("listPending")
    public BaseResponse<List<Comments>> listPending() {
        return ResultUtils.success(appService.listPending());
    }

    @GetMapping("countByTargetId/{targetId}")
    public BaseResponse<Long> countByTargetId(@PathVariable UUID targetId) {
        return ResultUtils.success(appService.countByTargetId(targetId));
    }
}
