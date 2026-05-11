package cn.dreamtof.media.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.media.application.service.MusicTracksAppService;
import cn.dreamtof.media.domain.model.entity.MusicTracks;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "媒体/音乐曲目表")
@RestController
@RequestMapping("/media/musicTracks")
@RequiredArgsConstructor
public class MusicTracksController {

    private final MusicTracksAppService appService;

    @PostMapping("save")
    public BaseResponse<MusicTracks> save(@RequestBody MusicTracks entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PutMapping("update")
    public BaseResponse<MusicTracks> update(@RequestBody MusicTracks entity) {
        return ResultUtils.success(appService.update(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<MusicTracks> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<MusicTracks>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<MusicTracks>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<MusicTracks>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("listByPlaylistId/{playlistId}")
    public BaseResponse<List<MusicTracks>> listByPlaylistId(@PathVariable UUID playlistId) {
        return ResultUtils.success(appService.listByPlaylistId(playlistId));
    }
}
