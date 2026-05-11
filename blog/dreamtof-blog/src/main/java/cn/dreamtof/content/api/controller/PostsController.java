package cn.dreamtof.content.api.controller;

import cn.dreamtof.content.api.vo.PostDetailVO;
import cn.dreamtof.content.api.vo.PostVO;
import cn.dreamtof.content.application.service.PostsAppService;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "内容管理/文章")
@RestController
@RequestMapping("/content/posts")
@RequiredArgsConstructor
public class PostsController {

    private final PostsAppService appService;

    @PostMapping("create")
    public BaseResponse<PostDetailVO> create(
            @RequestParam("title") String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "draft", required = false) Boolean draft,
            @RequestParam(value = "image", required = false) String image,
            @RequestParam(value = "comment", required = false) Boolean comment,
            @RequestParam(value = "licenseName", required = false) String licenseName,
            @RequestParam(value = "licenseUrl", required = false) String licenseUrl,
            @RequestParam(value = "sourceLink", required = false) String sourceLink,
            @RequestParam(value = "alias", required = false) String alias,
            @RequestParam(value = "permalink", required = false) String permalink,
            @RequestParam(value = "passwordHint", required = false) String passwordHint,
            @RequestParam(value = "tagNames", required = false) List<String> tagNames) {
        return ResultUtils.success(appService.createPost(title, content, slug, description, author,
                category, lang, draft, image, comment, licenseName,
                licenseUrl, sourceLink, alias, permalink, passwordHint, tagNames));
    }

    @PutMapping("update/{id}")
    public BaseResponse<PostDetailVO> update(
            @PathVariable UUID id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "slug", required = false) String slug,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "image", required = false) String image,
            @RequestParam(value = "comment", required = false) Boolean comment,
            @RequestParam(value = "licenseName", required = false) String licenseName,
            @RequestParam(value = "licenseUrl", required = false) String licenseUrl,
            @RequestParam(value = "sourceLink", required = false) String sourceLink,
            @RequestParam(value = "alias", required = false) String alias,
            @RequestParam(value = "permalink", required = false) String permalink,
            @RequestParam(value = "passwordHint", required = false) String passwordHint,
            @RequestParam(value = "tagNames", required = false) List<String> tagNames) {
        return ResultUtils.success(appService.updatePost(id, title, content, slug, description, author,
                category, lang, image, comment, licenseName, licenseUrl,
                sourceLink, alias, permalink, passwordHint, tagNames));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.deletePost(id));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<PostDetailVO> getById(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("getBySlug")
    public BaseResponse<PostDetailVO> getBySlug(@RequestParam("slug") String slug) {
        return ResultUtils.success(appService.getBySlug(slug));
    }

    @PutMapping("publish/{id}")
    public BaseResponse<PostDetailVO> publishDraft(@PathVariable UUID id) {
        return ResultUtils.success(appService.publishDraft(id));
    }

    @PutMapping("unpublish/{id}")
    public BaseResponse<PostDetailVO> unpublish(@PathVariable UUID id) {
        return ResultUtils.success(appService.unpublish(id));
    }

    @PutMapping("togglePin/{id}")
    public BaseResponse<PostDetailVO> togglePin(
            @PathVariable UUID id,
            @RequestParam("pinned") boolean pinned,
            @RequestParam(value = "priority", required = false) Integer priority) {
        return ResultUtils.success(appService.togglePin(id, pinned, priority));
    }

    @PutMapping("incrementView/{id}")
    public BaseResponse<Long> incrementViewCount(@PathVariable UUID id) {
        return ResultUtils.success(appService.incrementViewCount(id));
    }

    @PostMapping("validatePassword/{id}")
    public BaseResponse<Boolean> validatePassword(
            @PathVariable UUID id,
            @RequestParam("password") String password) {
        return ResultUtils.success(appService.validatePassword(id, password));
    }

    @PostMapping("page")
    public BaseResponse<PageResult<PostVO>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.pagePosts(pageReq));
    }

    @GetMapping("search")
    public BaseResponse<PageResult<PostVO>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        PageReq pageReq = new PageReq();
        pageReq.setPageNum(pageNum);
        pageReq.setPageSize(pageSize);
        return ResultUtils.success(appService.searchPosts(keyword, pageReq));
    }
}
