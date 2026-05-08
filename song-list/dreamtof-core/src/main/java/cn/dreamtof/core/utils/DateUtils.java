package cn.dreamtof.core.utils;


import cn.dreamtof.core.exception.Asserts;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 全局统一时间工具类
 * <p>
 * 强制使用 java.time API，时区固定为 Asia/Shanghai。
 * 配合 JsonUtils 使用，确保前后端、日志、数据库时间一致。
 * </p>
 */
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 默认日期时间格式
     */
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 统一时区：东八区
     */
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * 格式化 LocalDateTime 为字符串 (yyyy-MM-dd HH:mm:ss)
     *
     * @param dateTime 时间对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return DATETIME_FORMATTER.format(dateTime);
    }

    /**
     * 解析字符串为 LocalDateTime (yyyy-MM-dd HH:mm:ss)
     *
     * @param dateStr 时间字符串
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parse(String dateStr) {
        Asserts.notBlank(dateStr, "时间字符串不能为空");
        return LocalDateTime.parse(dateStr, DATETIME_FORMATTER);
    }

    /**
     * 兼容性工具：Date 转 LocalDateTime
     * <p>
     * 仅用于对接旧版三方库，项目内部业务禁止使用 Date。
     * </p>
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime();
    }

    /**
     * 兼容性工具：LocalDateTime 转 Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(DEFAULT_ZONE).toInstant());
    }

    /**
     * 获取当前系统时间 (已锁定上海时区)
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DEFAULT_ZONE);
    }

    public static OffsetDateTime offsetNow() {
        return OffsetDateTime.now(DEFAULT_ZONE);
    }
}