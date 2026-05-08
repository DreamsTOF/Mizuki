package cn.dreamtof.audit.utils;

import cn.dreamtof.core.utils.FastBeanMeta;
import cn.dreamtof.core.utils.FastReflectUtils;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

import static cn.dreamtof.log.core.LogMarkers.AUDIT;

/**
 * 👑 审计 ID 提取引擎 (极速版)
 * <p>
 * 优化点：使用 FastReflectUtils 取代原生 Field.get，内部通过 Lambda 访问器实现零反射损耗取值。
 */
@Slf4j
public class IdExtractor {

    /**
     * 自动从实体对象中提取主键 ID
     * * @param entity 实体对象
     * @return 序列化后的 ID 值
     */
    public static Serializable extractId(Object entity) {
        if (entity == null) return null;
        Class<?> clazz = entity.getClass();

        // 1. 获取 MyBatis-Flex 表元数据
        TableInfo tableInfo = TableInfoFactory.ofEntityClass(clazz);

        // 2. 优先从 MyBatis-Flex 元数据获取单主键字段名
        if (tableInfo != null && !tableInfo.getPrimaryKeyList().isEmpty()) {
            Object pkInfo = tableInfo.getPrimaryKeyList().getFirst();
            String prop = getPropertyFromColumnInfo(pkInfo);
            return extractValue(entity, prop);
        }

        // 3. 兜底：尝试寻找名为 "id" 的通用字段
        return extractValue(entity, "id");
    }

    /**
     * 利用 FastReflectUtils 高性能提取指定属性值
     * * @param entity 目标对象
     * @param propName 属性名
     * @return ID 值
     */
    private static Serializable extractValue(Object entity, String propName) {
        try {
            // getPropertyValue 内部使用了 FastBeanMeta 的 Lambda 访问器
            Object val = FastReflectUtils.getPropertyValue(entity, propName);
            return val instanceof Serializable s ? s : null;
        } catch (Exception e) {
            log.warn(AUDIT, "[自动审计] 字段提取失败: 实体={}, 字段={}, 原因={}",
                    entity.getClass().getSimpleName(), propName, e.getMessage());
        }
        return null;
    }

    /**
     * 从 MyBatis ColumnInfo 中高性能提取 property 字段值
     * * @param columnInfo MyBatis 的列信息对象
     * @return 对应的属性名
     */
    private static String getPropertyFromColumnInfo(Object columnInfo) {
        try {
            // 直接利用 LambdaMetafactory 访问 MyBatis 元数据内部的 property 字段
            Object property = FastBeanMeta.of(columnInfo.getClass())
                    .getAccessor("property")
                    .getter()
                    .apply(columnInfo);
            return property != null ? property.toString() : "id";
        } catch (Exception e) {
            log.warn(AUDIT, "[自动审计] 从元数据获取属性名失败，默认使用 'id'");
            return "id";
        }
    }
}