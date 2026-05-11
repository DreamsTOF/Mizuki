package cn.dreamtof.social.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.social.application.service.FriendTagLinksAppService;
import cn.dreamtof.social.domain.model.entity.FriendTagLinks;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "社交/友链-标签关联表")
@RestController
@RequestMapping("/social/friendTagLinks")
@RequiredArgsConstructor
public class FriendTagLinksController {

    private final FriendTagLinksAppService appService;

    @PostMapping("save")
    public BaseResponse<FriendTagLinks> save(@RequestBody FriendTagLinks entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @GetMapping("listByFriendId/{friendId}")
    public BaseResponse<List<FriendTagLinks>> listByFriendId(@PathVariable UUID friendId) {
        return ResultUtils.success(appService.listByFriendId(friendId));
    }

    @GetMapping("listByTagId/{tagId}")
    public BaseResponse<List<FriendTagLinks>> listByTagId(@PathVariable UUID tagId) {
        return ResultUtils.success(appService.listByTagId(tagId));
    }

    @DeleteMapping("unlink/{friendId}/{tagId}")
    public BaseResponse<Boolean> unlink(@PathVariable UUID friendId, @PathVariable UUID tagId) {
        return ResultUtils.success(appService.removeByFriendIdAndTagId(friendId, tagId));
    }
}
