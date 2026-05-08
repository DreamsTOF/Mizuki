package cn.dreamtof.core.exception;

/**
 * 通用基础错误码 (Core 级别)
 * 职责：全局通用的技术型异常和业务型异常
 */
public class CommonErrorCode extends ErrorCode {

    // 0. 成功状态
    public static final CommonErrorCode SUCCESS = new CommonErrorCode(0, "操作成功");

    // 100xx: 基础系统错误
    public static final CommonErrorCode PARAM_ERROR = new CommonErrorCode(10000, "参数校验失败");
    public static final CommonErrorCode SYSTEM_ERROR = new CommonErrorCode(99999, "系统内部异常");

    // 101xx: 通用业务错误
    public static final CommonErrorCode RESOURCE_NOT_FOUND = new CommonErrorCode(10100, "资源不存在");
    public static final CommonErrorCode RESOURCE_ALREADY_EXISTS = new CommonErrorCode(10101, "资源已存在");
    public static final CommonErrorCode PERMISSION_DENIED = new CommonErrorCode(10102, "无权操作");
    public static final CommonErrorCode VERSION_CONFLICT = new CommonErrorCode(10103, "数据已被修改，请刷新后重试");
    public static final CommonErrorCode OPERATION_FAILED = new CommonErrorCode(10104, "操作失败");

    // 102xx: 认证授权错误
    public static final CommonErrorCode UNAUTHORIZED = new CommonErrorCode(10200, "未登录或登录已过期");
    public static final CommonErrorCode FORBIDDEN = new CommonErrorCode(10201, "无权访问");
    public static final CommonErrorCode PASSWORD_ERROR = new CommonErrorCode(10202, "密码错误");

    // 103xx: JSON 解析细分 (Jackson/Gson)
    public static final CommonErrorCode JSON_PARSE_ERROR = new CommonErrorCode(10300, "JSON 语法错误，无法解析");
    public static final CommonErrorCode JSON_MAPPING_ERROR = new CommonErrorCode(10301, "JSON 结构与对象不匹配");
    public static final CommonErrorCode JSON_FORMAT_INVALID = new CommonErrorCode(10304, "JSON 字段格式转换失败");

    // 104xx: 通用文件操作 (本地 IO 相关)
    public static final CommonErrorCode FILE_READ_PERMISSION_DENIED = new CommonErrorCode(10409, "文件读取权限不足");
    public static final CommonErrorCode FILE_WRITE_PERMISSION_DENIED = new CommonErrorCode(10410, "文件写入权限不足");
    public static final CommonErrorCode FILE_CORRUPTED = new CommonErrorCode(10412, "文件内容已损坏");
    public static final CommonErrorCode FILE_NOT_FOUND_LOCAL = new CommonErrorCode(90201, "底层文件系统未找到指定文件");

    protected CommonErrorCode(int code, String message) {
        super(code, message);
    }
}