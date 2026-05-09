package cn.dreamtof.system.api.controller;

import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.utils.ResultUtils;
import cn.dreamtof.system.api.request.UploadedFilesPageReq;
import cn.dreamtof.system.api.vo.UploadedFileVO;
import cn.dreamtof.system.application.service.UploadedFilesAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/system/uploaded-files")
@RequiredArgsConstructor
@Tag(name = "文件上传管理")
public class UploadedFilesController {

    private final UploadedFilesAppService uploadedFilesAppService;

    @PostMapping("/upload")
    @Operation(summary = "单文件上传")
    public BaseResponse<UploadedFileVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) {
        return ResultUtils.success(uploadedFilesAppService.upload(file, folder));
    }

    @PostMapping("/upload-batch")
    @Operation(summary = "批量文件上传")
    public BaseResponse<List<UploadedFileVO>> uploadBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("folder") String folder) {
        return ResultUtils.success(uploadedFilesAppService.uploadBatch(files, folder));
    }

    @DeleteMapping("/remove/{id}")
    @Operation(summary = "按ID删除文件")
    public BaseResponse<Void> remove(@PathVariable UUID id) {
        uploadedFilesAppService.deleteById(id);
        return ResultUtils.success();
    }

    @DeleteMapping("/remove-by-url")
    @Operation(summary = "按URL删除文件")
    public BaseResponse<Void> removeByUrl(@RequestParam("url") String url) {
        uploadedFilesAppService.deleteByUrl(url);
        return ResultUtils.success();
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "文件详情")
    public BaseResponse<UploadedFileVO> detail(@PathVariable UUID id) {
        return ResultUtils.success(uploadedFilesAppService.getDetail(id));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询文件列表")
    public BaseResponse<PageResult<UploadedFileVO>> page(@RequestBody UploadedFilesPageReq pageReq) {
        return ResultUtils.success(uploadedFilesAppService.pageByFolder(pageReq));
    }
}
