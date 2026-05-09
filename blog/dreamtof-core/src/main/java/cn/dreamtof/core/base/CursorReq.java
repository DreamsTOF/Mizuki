package cn.dreamtof.core.base;

import lombok.Data;

import javax.xml.validation.Schema;
import java.io.Serializable;

/**
 * 游标分页请求基类
 */
@Data
public class CursorReq {

    /**
     * 游标（上一页最后一条记录的ID）
     */
    private Serializable cursor;

    /**
     * 每页记录数
     */
    private int limit = 20;

    /**
     * 下一页游标（由后端返回，供下次查询使用）
     */
    private Serializable next;
}