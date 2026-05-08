package cn.dreamtof.core.exception;

/**
 * 错误码标准接口
 * 解决 DDD 架构下枚举膨胀的问题：允许每个模块定义自己的 ErrorCode 枚举
 */
public interface IErrorCode {
    int getCode();
    String getMessage();

    /**
     * 获取枚举名称，方便日志记录
     * 默认实现：如果是枚举则返回枚举名，否则返回未知
     */
    default String getName() {
        return this instanceof Enum ? ((Enum<?>) this).name() : "UNKNOWN_ERROR";
    }
}
