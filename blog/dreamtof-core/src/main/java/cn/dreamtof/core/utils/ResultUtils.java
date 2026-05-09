package cn.dreamtof.core.utils;


import cn.dreamtof.core.base.BaseResponse;

/**
 * 快速构造响应结果的工具类
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, null,true);
    }

    /**
     * 成功（无数据）
     *
     * @param <T> 数据类型
     * @return 响应
     */
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(0, null, null, true);
    }

}
