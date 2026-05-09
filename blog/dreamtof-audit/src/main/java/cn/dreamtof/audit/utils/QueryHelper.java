package cn.dreamtof.audit.utils;

import com.mybatisflex.core.mybatis.Mappers;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 审计查询引擎 (单主键优化版)
 */
public class QueryHelper {

    public static List<Object> executeBatchSelect(Class<?> clazz, List<Serializable> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<Object> resultList = new ArrayList<>();
        final int BATCH_SIZE = 500;

        for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
            // 提取当前批次的有效 ID（过滤掉可能存在的 null）
            List<Serializable> batchIds = ids.subList(i, Math.min(i + BATCH_SIZE, ids.size()))
                    .stream()
                    .filter(Objects::nonNull)
                    .toList();

            // 【绝命拦截回归】：如果过滤后这批 ID 全是空的，绝对不许调 Mappers
            if (batchIds.isEmpty()) {
                continue;
            }

            List<?> entities = Mappers.ofEntityClass(clazz).selectListByIds(batchIds);
            if (entities != null) {
                resultList.addAll(entities);
            }
        }
        return resultList;
    }
}