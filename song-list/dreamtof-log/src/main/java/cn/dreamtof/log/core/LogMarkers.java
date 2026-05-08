package cn.dreamtof.log.core;

import org.slf4j.Marker;
import org.slf4j.helpers.BasicMarkerFactory;

/**
 * 预定义 Marker 常量类
 * <p>
 * 使用 SLF4J 标准 Marker 机制实现日志分类，替代原有的自定义日志等级体系。
 * </p>
 */
public final class LogMarkers {

    private static final BasicMarkerFactory FACTORY = new BasicMarkerFactory();

    private LogMarkers() {}

    private static Marker marker(String name) {
        return FACTORY.getMarker(name);
    }

    public static final Marker AUDIT = marker("AUDIT");
    public static final Marker SECURITY = marker("SECURITY");
    public static final Marker PERFORMANCE = marker("PERFORMANCE");
    public static final Marker BUSINESS = marker("BUSINESS");
    public static final Marker BUSINESS_AUDIT = marker("BUSINESS_AUDIT");

    public static final Marker CRITICAL = marker("CRITICAL");
    public static final Marker FATAL = marker("FATAL");
    public static final Marker NOTICE = marker("NOTICE");

    public static final Marker DATABASE = marker("DATABASE");
    public static final Marker NETWORK = marker("NETWORK");
    public static final Marker CACHE = marker("CACHE");
    public static final Marker EXTERNAL_API = marker("EXTERNAL_API");

    public static final Marker SENSITIVE = marker("SENSITIVE");
    public static final Marker COMPLIANCE = marker("COMPLIANCE");
}
