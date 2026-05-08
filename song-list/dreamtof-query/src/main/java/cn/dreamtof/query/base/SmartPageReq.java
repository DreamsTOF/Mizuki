package cn.dreamtof.query.base;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.query.core.FlexSmartQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 查询引擎专用的分页请求增强类
 * 继承自 Core 包的纯净 PageReq，通过给 Getter 打注解与引擎发生化学反应
 */
public class SmartPageReq extends PageReq {

    @FlexSmartQuery.PageNo
    @Override
    public int getPageNum() { return super.getPageNum(); }

    @FlexSmartQuery.PageSize
    @Override
    public int getPageSize() { return super.getPageSize(); }

    /**
     * 前端传来的排序请求
     * 例如：[{field: "createTime", order: "ASCEND"}]
     */
    @FlexSmartQuery.SortList
    private List<SortItem> sorters;

    /**
     * 内部类：排序项
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SortItem {
        private String field;       // 排序字段名（建议对应数据库列名或实体属性名）
        private SortOrder order;    // 之前修好的那个 SortOrder 枚举
    }
    /**
     * 获取最终生效的排序规则
     */
    public List<SortItem> fetchEffectiveSorters() {
        // 1. 完全没传，直接给默认值
        if (sorters == null || sorters.isEmpty()) {
            return Collections.singletonList(new SortItem("updateTime", SortOrder.DESCEND));
        }

        // 2. 检查前端是否已经显式指定了 createTime
        boolean hasExplicitTime = sorters.stream()
                .anyMatch(item -> "updateTime".equalsIgnoreCase(item.getField()));

        if (hasExplicitTime) {
            // 如果前端传了 createTime (不管是 ASC 还是 DESC)，以前端为准，直接返回
            return sorters;
        }
        // 3. 前端传了其他字段（如“身高”），但没传“时间”
        // 我们在最后追加一个时间降序，保证身高相同时的排序稳定性
        List<SortItem> result = new ArrayList<>(sorters);
        result.add(new SortItem("updateTime", SortOrder.DESCEND));
        return result;
    }
}