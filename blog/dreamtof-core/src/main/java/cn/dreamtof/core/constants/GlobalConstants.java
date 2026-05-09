package cn.dreamtof.core.constants;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 全局通用常量
 * 只存放全系统共用的、与业务无关的基础常量
 */
public interface GlobalConstants {

    /**
     * 默认字符集
     */
    String UTF8 = "UTF-8";

    /**
     * 链路追踪 ID Key (用于日志、HTTP Header)
     */
    String TRACE_ID = "trace_id";

    // --- 配置常量 ---
    String DEVICE_HEADER = "X-Device-Id";
    String TRACE_HEADER = "X-Trace-Id";

    /**
     * 默认时区
     */
    String DEFAULT_TIMEZONE = "GMT+8";

    /**
     * 通用日期格式
     */
    String DATE_FORMAT = "yyyy-MM-dd";
    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 新增：带毫秒的格式（日志常用）
    String DATETIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 全局统一的标准时间格式化器 (yyyy-MM-dd HH:mm:ss)
     * 强制绑定 GMT+8，防止 Docker 容器时区漂移
     */
    DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT)
            .withZone(ZoneId.of(DEFAULT_TIMEZONE));

    /**
     * 全局统一的带毫秒时间格式化器 (yyyy-MM-dd HH:mm:ss.SSS)
     */
    DateTimeFormatter DATETIME_MS_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_MS_FORMAT)
            .withZone(ZoneId.of(DEFAULT_TIMEZONE));

    /**
     * 逻辑删除标识 (0: 未删除, 1: 已删除)
     */
    int NOT_DELETED = 0;
    int DELETED = 1;
}
