package cn.dreamtof.media.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.media.application.service.AlbumPhotosAppService;
import cn.dreamtof.media.domain.model.entity.AlbumPhotos;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "媒体/相册图片表")
@RestController
@RequestMapping("/media/albumPhotos")
@RequiredArgsConstructor
public class AlbumPhotosController {

    private final AlbumPhotosAppService appService;

    @PostMapping("save")
    public BaseResponse<AlbumPhotos> save(@RequestBody AlbumPhotos entity) {
        return ResultUtils.success(appService.create(entity));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @PutMapping("update")
    public BaseResponse<AlbumPhotos> update(@RequestBody AlbumPhotos entity) {
        return ResultUtils.success(appService.update(entity));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<AlbumPhotos> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @GetMapping("list")
    public BaseResponse<List<AlbumPhotos>> listAll() {
        return ResultUtils.success(appService.listAll());
    }

    @PostMapping("page")
    public BaseResponse<PageResult<AlbumPhotos>> page(@RequestBody PageReq pageReq) {
        return ResultUtils.success(appService.page(pageReq));
    }

    @GetMapping("seek")
    public BaseResponse<CursorResult<AlbumPhotos>> seek(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResultUtils.success(appService.seek(cursor, limit));
    }

    @GetMapping("listByAlbumId/{albumId}")
    public BaseResponse<List<AlbumPhotos>> listByAlbumId(@PathVariable UUID albumId) {
        return ResultUtils.success(appService.listByAlbumId(albumId));
    }
}
