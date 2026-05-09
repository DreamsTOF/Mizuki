package cn.dreamtof.system.api.request;

import cn.dreamtof.core.base.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UploadedFilesPageReq", description = "文件上传记录分页查询")
public class UploadedFilesPageReq extends PageReq {

    @Schema(description = "按目录类型筛选")
    private String folder;
}
