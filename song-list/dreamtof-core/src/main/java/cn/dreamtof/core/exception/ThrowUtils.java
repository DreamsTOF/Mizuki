package cn.dreamtof.core.exception;


public class ThrowUtils {

    /**
     * 条件成立则抛出异常
     *
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, IErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, IErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }

    /**
     * 条件成立则抛异常（带原始异常链）
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param cause     原始异常
     */
    public static void throwIf(boolean condition, IErrorCode errorCode, Throwable cause) {
        throwIf(condition, new BusinessException(errorCode, cause));
    }

    /**
     * 条件成立则抛异常（带错误信息和原始异常链）
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public static void throwIf(boolean condition, IErrorCode errorCode, String message, Throwable cause) {
        throwIf(condition, new BusinessException(errorCode, message, cause));
    }
}
