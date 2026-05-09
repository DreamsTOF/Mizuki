package cn.dreamtof.core.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursorResult<T> {
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 下一页的游标 (通常是最后一条数据的 ID 或时间戳)
     */
    private Serializable nextCursor;
    
    /**
     * 是否还有下一页
     */
    private boolean hasNext;
}