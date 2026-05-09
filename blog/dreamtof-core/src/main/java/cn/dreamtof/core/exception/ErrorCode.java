package cn.dreamtof.core.exception;

import lombok.Getter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 错误码基类 - 支持继承与全局注册
 */
@Getter
public abstract class ErrorCode implements IErrorCode {
    
    private final int code;
    private final String message;

    // 全局错误码注册表，用于支持 getByCode 功能
    private static final Map<Integer, ErrorCode> REGISTRY = new ConcurrentHashMap<>();

    protected ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
        
        // 自动检查重复并注册
        if (REGISTRY.containsKey(code)) {
            throw new IllegalStateException("禁止注册相同错误码: " + code + " [" + message + "]");
        }
        REGISTRY.put(code, this);
    }

    /**
     * 根据编码获取对应的错误对象
     */
    public static ErrorCode getByCode(int code) {
        return REGISTRY.get(code);
    }
}