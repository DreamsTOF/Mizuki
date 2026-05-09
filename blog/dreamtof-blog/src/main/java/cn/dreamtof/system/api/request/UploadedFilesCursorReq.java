package cn.dreamtof.system.api.request;

import cn.dreamtof.core.base.CursorReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UploadedFilesCursorReq", description = "文件上传记录游标分页查询")
public class UploadedFilesCursorReq extends CursorReq {

    @Schema(description = "按目录类型筛选")
    private String folder;
}
