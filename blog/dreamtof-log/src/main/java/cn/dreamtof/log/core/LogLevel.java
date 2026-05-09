package cn.dreamtof.log.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 📊 日志级别枚举 (双字段增强版)
 *
 */
@Getter
@AllArgsConstructor
public enum LogLevel {
    TRACE(0, "TRACE"),
    DEBUG(10, "DEBUG"),
    INFO(20, "INFO"),
    WARN(30, "WARN"),
    ERROR(40, "ERROR"),
    FATAL(50, "FATAL"); // 预留最高优先级

    /** 逻辑权重：用于级别比对和过滤 */
    private final int weight;

    /** 显示标签：用于日志文本输出或前端展示，确保可读性一致 */
    private final String label;

    public static LogLevel fromString(String level) {
        try {
            return valueOf(level.toUpperCase());
        } catch (Exception e) {
            return INFO;
        }
    }
}