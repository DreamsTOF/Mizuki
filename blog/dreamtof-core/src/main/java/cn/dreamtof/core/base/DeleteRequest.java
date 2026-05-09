package cn.dreamtof.core.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除请求包装类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private String id;

    @Serial
    private static final long serialVersionUID = 1L;
}
