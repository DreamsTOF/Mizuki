package cn.dreamtof.content.api.controller;

import cn.dreamtof.content.application.service.PostTagsAppService;
import cn.dreamtof.content.domain.model.entity.PostTags;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "内容管理/文章标签关联")
@RestController
@RequestMapping("/content/postTags")
@RequiredArgsConstructor
public class PostTagsController {

    private final PostTagsAppService appService;

    @PostMapping("page")
    public BaseResponse<PageResult<PostTags>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pagePostTags(pageReq));
    }

    @GetMapping("listByPost/{postId}")
    public BaseResponse<List<PostTags>> listByPostId(@PathVariable UUID postId) {
        return ResultUtils.success(appService.listByPostId(postId));
    }

    @GetMapping("listByTag/{tagId}")
    public BaseResponse<List<PostTags>> listByTagId(@PathVariable UUID tagId) {
        return ResultUtils.success(appService.listByTagId(tagId));
    }
}
