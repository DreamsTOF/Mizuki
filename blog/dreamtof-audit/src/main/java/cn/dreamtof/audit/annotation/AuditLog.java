package cn.dreamtof.audit.annotation;



import cn.dreamtof.audit.enums.AuditOption;

import java.lang.annotation.*;

/**
 * 审计日志注解 (Audit Log Annotation) - V2.0
 * <p>
 * 升级说明：
 * 1. 数值类型默认值改为 -1，表示"继承全局配置"。
 * 2. Boolean 类型改为 AuditOption 枚举，支持"继承全局配置"。
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 业务模块 (留空自动继承 Controller Tag 或 微服务名)
     */
    String module() default "";

    /**
     * 业务动作 (留空自动继承 Controller Operation)
     */
    String action() default "";

    /**
     * 单实体模式 (兼容旧代码)
     */
    Class<?> entity() default Object.class;

    /**
     * 多实体模式 (推荐，支持一次操作修改多张表)
     */
    Class<?>[] entities() default {};


    /**
     * 简略模式 (Partial Mode)
     * DEFAULT: 跟随全局配置 (默认)
     */
    AuditOption partial() default AuditOption.DEFAULT;

    /**
     * 跳过空字段 (Skip Null Fields)
     * DEFAULT: 跟随全局配置 (默认)
     */
    AuditOption skipNull() default AuditOption.TRUE;

    /**
     * 包含系统字段 (Include System Fields)
     * DEFAULT: 跟随全局配置 (默认)
     */
    AuditOption systemFields() default AuditOption.FALSE;

    /**
     * 是否同步回填数据库最新快照 (Refresh Mode)
     * TRUE: 事务结束前强制 SELECT 一次数据库，获取最精确的 newState (防触发器、防幻读)
     * FALSE: 仅使用内存拦截到的对象作为 newState (性能最高)
     * DEFAULT: 跟随全局配置 (默认建议为 TRUE)
     */
    AuditOption refresh() default AuditOption.FALSE;
}
