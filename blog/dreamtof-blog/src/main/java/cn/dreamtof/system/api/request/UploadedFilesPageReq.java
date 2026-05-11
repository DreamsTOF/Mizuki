package cn.dreamtof.system.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.EqualsAndHashCode;

/**
 * 文件上传记录表 分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UploadedFilesPageReq", description = "文件上传记录表分页查询请求")
public class UploadedFilesPageReq extends PageReq {

    @Schema(description = "目标目录类型")
    private String folder;
}