package cn.dreamtof.audit.cache;

import cn.dreamtof.core.utils.FastBeanMeta;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🧩 审计专用元数据适配器
 * <p>
 * 为什么要有这个类：
 * 1. 职责解耦：FastBeanMeta 不应该知道 AuditMetaCache 的存在。
 * 2. 性能封装：将“如何运行” (FastBeanMeta) 和“怎么展示” (AuditMetaCache) 预先缝合在一起。
 * 3. 简化业务：切面在对比差异时，直接从 AuditField 拿 label 和 value，无需任何额外逻辑。
 * </p>
 */
@Getter
public class AuditBeanMeta {

    private final Class<?> targetClass;
    private final String classLabel;
    private final List<AuditField> fields;

    /** 专门存储适配后的审计元数据 */
    private static final ClassValue<AuditBeanMeta> CACHE = new ClassValue<>() {
        @Override
        protected AuditBeanMeta computeValue(Class<?> type) {
            return new AuditBeanMeta(type);
        }
    };

    public static AuditBeanMeta of(Class<?> clazz) {
        return CACHE.get(clazz);
    }

    private AuditBeanMeta(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.classLabel = AuditMetaCache.getDisplayName(targetClass);
        
        // 核心结合点：
        // 1. 从通用的 FastBeanMeta 拿到所有高性能访问器
        FastBeanMeta fastMeta = FastBeanMeta.of(targetClass);
        
        // 2. 遍历并包装为审计专用对象，完成 label 的映射
        this.fields = fastMeta.getWriteableAccessors().stream()
                .map(acc -> new AuditField(
                        AuditMetaCache.getDisplayName(acc.field()), 
                        acc
                ))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * 审计字段单元
     * @param label 中文名 (如: 账号名称)
     * @param accessor 执行引擎 (包含 getter/setter Lambda)
     */
    public record AuditField(
            String label,
            FastBeanMeta.FieldAccessor accessor
    ) {
        /** 便捷方法：获取对象中该字段的值 */
        public Object getValue(Object target) {
            return accessor.getter().apply(target);
        }
        
        /** 获取字段名 (英文) */
        public String getFieldName() {
            return accessor.fieldName();
        }
    }
}