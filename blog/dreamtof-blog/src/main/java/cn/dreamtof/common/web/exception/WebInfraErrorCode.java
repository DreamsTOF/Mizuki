package cn.dreamtof.common.web.exception;

import cn.dreamtof.core.exception.CommonErrorCode;

/**
 * 用户中心业务错误码 (Infrastructure 级别)
 * 职责：User 模块的 Web 参数校验、HTTP 协议错误、数据库(DB)持久化错误
 */
public class WebInfraErrorCode extends CommonErrorCode {

    // ========================================================================
    // 1. Web 层与请求参数 (101xx - 102xx)
    // ========================================================================
    public static final WebInfraErrorCode CLIENT_ERROR = new WebInfraErrorCode(10001, "客户端请求异常");
    public static final WebInfraErrorCode PARAM_IS_NULL = new WebInfraErrorCode(10101, "必填参数为空");
    public static final WebInfraErrorCode PARAM_FORMAT_ERROR = new WebInfraErrorCode(10102, "参数格式错误");
    public static final WebInfraErrorCode PARAM_VALUE_INVALID = new WebInfraErrorCode(10103, "参数值不合法");
    
    public static final WebInfraErrorCode HTTP_METHOD_NOT_SUPPORTED = new WebInfraErrorCode(10202, "不支持的HTTP方法");
    public static final WebInfraErrorCode MEDIA_TYPE_NOT_SUPPORTED = new WebInfraErrorCode(10203, "不支持的Content-Type");

    // ========================================================================
    // 4. 数据库与资源持久化 (400xx - 404xx)
    // ========================================================================
    public static final WebInfraErrorCode DATABASE_ERROR = new WebInfraErrorCode(40000, "数据库服务异常");
    
    // 401xx: 数据库约束 (注: 40100/40101 保留给前端加密逻辑使用)
    public static final WebInfraErrorCode DUPLICATE_KEY = new WebInfraErrorCode(40110, "数据已存在(违反唯一约束)");
    public static final WebInfraErrorCode DATA_INTEGRITY_VIOLATION = new WebInfraErrorCode(40111, "违反数据完整性约束");
    public static final WebInfraErrorCode DATA_TOO_LONG = new WebInfraErrorCode(40113, "数据长度超出字段限制");

    // 403xx: 并发锁
    public static final WebInfraErrorCode OPTIMISTIC_LOCK_ERROR = new WebInfraErrorCode(40301, "数据已被其他用户修改(乐观锁失败)");
    public static final WebInfraErrorCode CANNOT_ACQUIRE_LOCK = new WebInfraErrorCode(40302, "无法获取资源锁，请稍后再试");

    // 404xx: 资源状态
    public static final WebInfraErrorCode USER_NOT_FOUND = new WebInfraErrorCode(40400, "请求的用户资源不存在");
    public static final WebInfraErrorCode DATA_NOT_FOUND = new WebInfraErrorCode(40402, "查询结果为空");

    protected WebInfraErrorCode(int code, String message) {
        super(code, message);
    }
}