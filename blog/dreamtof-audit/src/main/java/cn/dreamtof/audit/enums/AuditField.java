package cn.dreamtof.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审计日志标准字段字典
 * <p>
 * 统一管理落库和传输时的 JSON Key，杜绝魔法字符串。
 * 附带 text 字段方便日志解析或前端动态渲染。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum AuditField {

    TRACE_ID("链路追踪ID", "traceId"),
    OPERATOR_ID("操作人ID", "operatorId"),
    OPERATOR_NAME("操作人名称", "operatorName"),
    MODULE("业务模块", "module"),
    ACTION("操作动作", "action"),
    COST_TIME("耗时(ms)", "costTime"),
    TIMESTAMP("时间戳", "timestamp"),
    ENTITY("实体名称", "entity"),
    ENTITY_ID("实体ID", "entityId"),
    OPERATION("操作类型", "operation"),
    DESCRIPTION("操作描述", "description"),
    CHANGES("变更详情", "changes");

    /**
     * 字段中文描述
     */
    private final String text;

    /**
     * 实际存储/传输的字段名 (JSON Key)
     */
    private final String value;
}