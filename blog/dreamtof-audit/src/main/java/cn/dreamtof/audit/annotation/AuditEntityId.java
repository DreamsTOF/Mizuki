package cn.dreamtof.audit.annotation;

import java.lang.annotation.*;

/**
 * 标记 DTO 中代表实体 ID 的字段，供审计切面精准抓取
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditEntityId {
    // 对应哪个实体类的 ID？（如果 DTO 里的 ID 对应的就是 @AuditLog 中的 entities，可不填）
    Class<?> target() default void.class;
}