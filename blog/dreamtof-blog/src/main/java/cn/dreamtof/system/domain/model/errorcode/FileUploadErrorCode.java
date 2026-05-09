package cn.dreamtof.system.domain.model.errorcode;

import cn.dreamtof.core.exception.ErrorCode;

public class FileUploadErrorCode extends ErrorCode {

    public static final FileUploadErrorCode INVALID_FOLDER = new FileUploadErrorCode(60100, "无效的上传目录");
    public static final FileUploadErrorCode INVALID_FILE_FORMAT = new FileUploadErrorCode(60101, "文件格式不被允许");
    public static final FileUploadErrorCode FILE_SIZE_EXCEEDED = new FileUploadErrorCode(60102, "文件大小超出限制");
    public static final FileUploadErrorCode MIME_MISMATCH = new FileUploadErrorCode(60103, "文件MIME类型与扩展名不匹配");
    public static final FileUploadErrorCode UPLOAD_FAILED = new FileUploadErrorCode(60104, "文件上传失败");
    public static final FileUploadErrorCode FILE_NOT_FOUND = new FileUploadErrorCode(60105, "文件记录不存在");
    public static final FileUploadErrorCode DELETE_FAILED = new FileUploadErrorCode(60106, "文件删除失败");

    protected FileUploadErrorCode(int code, String message) {
        super(code, message);
    }
}
