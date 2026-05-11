package cn.dreamtof.system.domain.model.enums;

import cn.dreamtof.core.exception.ErrorCode;

public class FileErrorCode extends ErrorCode {

    public static final FileErrorCode FILE_EMPTY = new FileErrorCode(20100, "上传文件不能为空");
    public static final FileErrorCode FILE_EXTENSION_DENIED = new FileErrorCode(20101, "文件格式不允许");
    public static final FileErrorCode FILE_SIZE_EXCEEDED = new FileErrorCode(20102, "文件大小超出限制");
    public static final FileErrorCode FILE_MIME_MISMATCH = new FileErrorCode(20103, "MIME类型与扩展名不匹配");
    public static final FileErrorCode FILE_MAGIC_MISMATCH = new FileErrorCode(20104, "文件内容与声明类型不符");
    public static final FileErrorCode FILE_UPLOAD_FAILED = new FileErrorCode(20105, "文件上传失败");
    public static final FileErrorCode FILE_DELETE_FAILED = new FileErrorCode(20106, "文件删除失败");
    public static final FileErrorCode FOLDER_INVALID = new FileErrorCode(20107, "无效的目标目录");
    public static final FileErrorCode FILE_RECORD_NOT_FOUND = new FileErrorCode(20108, "文件记录不存在");

    protected FileErrorCode(int code, String message) {
        super(code, message);
    }
}
