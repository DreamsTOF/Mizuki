package cn.dreamtof.core.base;


import cn.dreamtof.core.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用响应类
 *
 * @param <T>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> implements Serializable {

    private boolean success;

    private int code;

    private T data;

    private String message;

    private long timestamp;

    public BaseResponse(int code, T data, String message, boolean success) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.success = success;
        this.timestamp = System.currentTimeMillis();
    }

    public BaseResponse(int code, T data, boolean success) {
        this(code, data, "", success);
    }

    public BaseResponse(IErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(),false);
    }
}
