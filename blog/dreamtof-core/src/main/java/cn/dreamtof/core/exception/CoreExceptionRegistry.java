package cn.dreamtof.core.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 核心异常注册表 - 处理 JDK 运行时异常
 */
public class CoreExceptionRegistry extends BaseExceptionRegistry {

    public CoreExceptionRegistry() {
        // 注册基础 Runtime 异常
        register(NullPointerException.class, CommonErrorCode.SYSTEM_ERROR);
        register(IllegalArgumentException.class, CommonErrorCode.PARAM_ERROR);
        register(IndexOutOfBoundsException.class, CommonErrorCode.SYSTEM_ERROR);
        register(ClassCastException.class, CommonErrorCode.SYSTEM_ERROR);
        
        // 注册 JSON 相关 (如果 JSON 库在 Core 引用)
        register(JsonProcessingException.class, CommonErrorCode.JSON_PARSE_ERROR);
    }
}