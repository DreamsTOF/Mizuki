package cn.dreamtof.log.spi;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * LogListener SPI 加载器
 * <p>
 * 通过 Java SPI 机制自动发现和加载 LogListener 实现。
 * 当用户未在配置文件中显式配置注册表时，默认使用此加载器。
 * </p>
 *
 * <h3>SPI 配置文件</h3>
 * <p>
 * 在模块的 resources/META-INF/services/ 目录下创建文件：
 * {@code cn.dreamtof.log.spi.LogListener}
 * </p>
 *
 * <h3>配置文件内容示例</h3>
 * <pre>
 * cn.dreamtof.log.application.listener.ConsoleLogListener
 * cn.dreamtof.log.application.listener.FlexClickHouseLogListener
 * </pre>
 */
@Component
public  class LogListenerLoader {

    private static volatile boolean loaded = false;

    private static volatile boolean enabled = true;

    private LogListenerLoader() {
    }

    /**
     * 通过 SPI 加载所有 LogListener 实现
     * @return 加载到的监听器列表
     */
    public static List<LogListener> loadListeners() {
        List<LogListener> listeners = new ArrayList<>();

        if (!enabled) {
            return listeners;
        }

        ServiceLoader<LogListener> loader = ServiceLoader.load(LogListener.class);
        for (LogListener listener : loader) {
            listeners.add(listener);
        }

        loaded = true;
        return listeners;
    }

    /**
     * 判断是否已加载过
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * 判断 SPI 模式是否启用
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置 SPI 模式的启用状态
     * 当启用注册表模式时，应禁用 SPI 模式
     */
    public static void setEnabled(boolean enabled) {
        LogListenerLoader.enabled = enabled;
    }

    /**
     * 重置加载状态（用于测试或重新加载）
     */
    public static void reset() {
        loaded = false;
    }
}
