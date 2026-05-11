package cn.dreamtof.media.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.media.application.service.MusicPlaylistsAppService;
import cn.dreamtof.media.domain.model.entity.MusicPlaylists;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "媒体/音乐播放列表表")
@RestController
@RequestMapping("/media/musicPlaylists")
@RequiredArgsConstructor
public class MusicPlaylistsController {

    private final MusicPlaylistsAppService appService;

    @PostMapping("save")
    public BaseResponse<MusicPlaylists> save(@RequestBody MusicPlaylists entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PutMapping("update")
    public BaseResponse<MusicPlaylists> update(@RequestBody MusicPlaylists entity) {
        return ResultUtils.success(appService.update(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<MusicPlaylists> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<MusicPlaylists>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<MusicPlaylists>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<MusicPlaylists>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("listEnabled")
    public BaseResponse<List<MusicPlaylists>> listEnabled() {
        return ResultUtils.success(appService.listEnabled());
    }
}
