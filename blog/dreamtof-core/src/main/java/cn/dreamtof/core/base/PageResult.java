package cn.dreamtof.core.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 领域层通用分页结果
 * <p>
 * 职责：作为领域层 Repository 接口的分页返回类型，
 * 屏蔽底层框架（MyBatis-Flex/JPA）的分页实现细节。
 * </p>
 *
 * @author dream
 * @since 2026-04-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页号
     */
    private long pageNum;

    /**
     * 每页大小
     */
    private long pageSize;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 创建空分页结果
     */
    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(Collections.emptyList());
        result.setTotal(0);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages(0);
        return result;
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(List<T> records, long total, long pages, long pageNum, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records != null ? records : Collections.emptyList());
        result.setTotal(total);
        result.setPages(pages); // 直接设置，不再进行 Math.ceil 计算
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return pageNum < pages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return pageNum > 1;
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }
}
