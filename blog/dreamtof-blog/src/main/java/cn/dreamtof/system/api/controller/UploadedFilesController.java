package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.api.request.UploadedFilesCursorReq;
import cn.dreamtof.system.api.request.UploadedFilesPageReq;
import cn.dreamtof.system.api.vo.UploadedFileVO;
import cn.dreamtof.system.application.service.UploadedFilesAppService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "系统管理/文件上传")
@RestController
@RequestMapping("/system/uploadedFiles")
@RequiredArgsConstructor
public class UploadedFilesController {

    private final UploadedFilesAppService appService;

    @PostMapping("upload")
    public BaseResponse<UploadedFileVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) {
        return ResultUtils.success(appService.upload(file, folder));
    }

    @PostMapping("batchUpload")
    public BaseResponse<List<UploadedFileVO>> batchUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("folder") String folder) {
        return ResultUtils.success(appService.batchUpload(files, folder));
    }

    @DeleteMapping("remove/{id}")
    public BaseResponse<Boolean> removeById(@PathVariable UUID id) {
        return ResultUtils.success(appService.removeById(id));
    }

    @DeleteMapping("removeByIds")
    public BaseResponse<Boolean> removeByIds(@RequestBody List<UUID> ids) {
        return ResultUtils.success(appService.removeByIds(ids));
    }

    @GetMapping("detail/{id}")
    public BaseResponse<UploadedFileVO> getInfo(@PathVariable UUID id) {
        return ResultUtils.success(appService.getDetail(id));
    }

    @PostMapping("pageByFolder")
    public BaseResponse<PageResult<UploadedFileVO>> pageByFolder(@RequestBody UploadedFilesPageReq pageRequest) {
        return ResultUtils.success(appService.pageByFolder(pageRequest.getFolder(), pageRequest.getPageNum(), pageRequest.getPageSize()));
    }

    @PostMapping("seek")
    public BaseResponse<CursorResult<UploadedFileVO>> seek(@RequestBody UploadedFilesCursorReq cursorReq) {
        UUID cursor = cursorReq.getCursor() != null ? (UUID) cursorReq.getCursor() : null;
        return ResultUtils.success(appService.seek(cursor, cursorReq.getLimit()));
    }
}
