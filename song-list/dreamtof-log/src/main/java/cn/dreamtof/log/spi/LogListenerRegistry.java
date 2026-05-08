package cn.dreamtof.log.spi;

import cn.dreamtof.log.core.LogWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LogListener 动态注册表
 * <p>
 * 用于运行时动态注册和管理 LogListener 实例。
 * 当用户在配置文件中启用了注册表模式时，使用此类进行动态注册。
 * </p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要精确控制哪些监听器被加载</li>
 *   <li>需要禁用 SPI 自动发现机制</li>
 *   <li>需要在运行时动态添加/移除监听器</li>
 * </ul>
 *
 * <h3>配置示例 (YAML)</h3>
 * <pre>
 * dreamtof:
 *   log:
 *     registry-enabled: true
 * </pre>
 *
 * <h3>代码示例</h3>
 * <pre>
 * // 注册监听器
 * LogListenerRegistry.getInstance().register(ConsoleLogListener.class, new ConsoleLogListener(properties));
 * LogListenerRegistry.getInstance().register(FlexClickHouseLogListener.class, new FlexClickHouseLogListener(mapper));
 *
 * // 注销监听器
 * LogListenerRegistry.getInstance().unregister(ConsoleLogListener.class);
 *
 * // 获取监听器
 * ConsoleLogListener console = LogListenerRegistry.getInstance().get(ConsoleLogListener.class);
 * </pre>
 */
@Component
public class LogListenerRegistry {

    private static final LogListenerRegistry INSTANCE = new LogListenerRegistry();

    private final ConcurrentMap<Class<? extends LogListener>, LogListener> listenerMap = new ConcurrentHashMap<>();

    private volatile boolean enabled = false;

    private LogListenerRegistry() {
    }

    public static LogListenerRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 注册监听器
     * <p>
     * 注册后会自动同步到 LogWriter 事件总线。
     * </p>
     *
     * @param listenerClass 监听器类
     * @param listener      监听器实例
     * @param <T>           监听器类型
     */
    public <T extends LogListener> void register(Class<T> listenerClass, T listener) {
        LogListener previous = listenerMap.put(listenerClass, listener);

        // 如果之前存在同类监听器，先从 LogWriter 移除
        if (previous != null) {
            LogWriter.unregisterListener(previous);
        }

        // 注册到 LogWriter 事件总线
        LogWriter.registerListener(listener);

        System.out.println("[LogListenerRegistry] Registered listener: " + listenerClass.getSimpleName());
    }

    /**
     * 注销监听器
     * <p>
     * 注销后会自动从 LogWriter 事件总线移除。
     * </p>
     *
     * @param listenerClass 监听器类
     * @param <T>           监听器类型
     */
    public <T extends LogListener> void unregister(Class<T> listenerClass) {
        LogListener listener = listenerMap.remove(listenerClass);
        if (listener != null) {
            LogWriter.unregisterListener(listener);
            System.out.println("[LogListenerRegistry] Unregistered listener: " + listenerClass.getSimpleName());
        }
    }

    /**
     * 获取指定类型的监听器
     *
     * @param listenerClass 监听器类
     * @param <T>           监听器类型
     * @return 监听器实例，不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T extends LogListener> T get(Class<T> listenerClass) {
        return (T) listenerMap.get(listenerClass);
    }

    /**
     * 获取所有已注册的监听器
     *
     * @return 监听器列表（不可修改）
     */
    public List<LogListener> getListeners() {
        return Collections.unmodifiableList(new ArrayList<>(listenerMap.values()));
    }

    /**
     * 获取所有已注册的监听器类
     *
     * @return 监听器类集合
     */
    public Set<Class<? extends LogListener>> getListenerClasses() {
        return Collections.unmodifiableSet(listenerMap.keySet());
    }

    /**
     * 清空所有已注册的监听器
     */
    public void clear() {
        for (LogListener listener : listenerMap.values()) {
            LogWriter.unregisterListener(listener);
        }
        listenerMap.clear();
        System.out.println("[LogListenerRegistry] Cleared all listeners");
    }

    /**
     * 获取已注册监听器的数量
     */
    public int size() {
        return listenerMap.size();
    }

    /**
     * 判断注册表模式是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置注册表模式的启用状态
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 检查指定类型的监听器是否已注册
     *
     * @param listenerClass 监听器类
     */
    public boolean contains(Class<? extends LogListener> listenerClass) {
        return listenerMap.containsKey(listenerClass);
    }
}
