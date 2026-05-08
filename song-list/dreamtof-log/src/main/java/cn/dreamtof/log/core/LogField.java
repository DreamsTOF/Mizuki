package cn.dreamtof.log.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 基础日志 JSON 字段字典
 * <p>
 * 统一管控 LogWriter 输出到控制台/文件/总线时的基础 JSON Key。
 * 下游系统 (如 ELK、LogListener) 解析时请直接引用此枚举，杜绝魔法字符串。
 * </p>
 */
@Getter
@AllArgsConstructor
public enum LogField {

    TIME("打印时间", "time"),
    LEVEL("日志级别", "level"),
    MARKER("路由标记", "marker"),
    LOGGER("类名/名称", "logger"),
    MSG("日志内容", "msg"),
    TRACE_ID("链路追踪ID", "traceId"),
    USER("操作人信息", "user"),
    STACK("异常堆栈", "stack");

    private final String text;
    private final String value;
}