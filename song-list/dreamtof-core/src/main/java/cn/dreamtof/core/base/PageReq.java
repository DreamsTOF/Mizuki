package cn.dreamtof.core.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.validation.Schema;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分页查询请求基类
 */
@Data
public class PageReq {
    /**
     * 当前页号（从1开始）
     */
    private int pageNum = 1;
    /**
     * 页面大小（每页记录数，建议1-100）
     */
    private int pageSize = 10;
}
