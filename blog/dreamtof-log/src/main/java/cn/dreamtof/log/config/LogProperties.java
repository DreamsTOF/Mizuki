package cn.dreamtof.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🛠 日志系统 YAML 配置映射类
 * <p>
 * 注意：此类必须保留在底层 SDK 包中，作为底层 LogWriter 的配置契约。
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "dreamtof.log")
public class LogProperties {

    /** [总开关] 是否启用自定义日志增强逻辑 */
    private boolean enabled = true;

    /** 输出格式：text (带颜色文本), json (结构化 JSON) */
    private String output = "text";

    // --- 输出渠道控制 ---
    /** 是否开启控制台输出 */
    private boolean consoleEnabled = true;
    
    /** 是否开启文件输出 */
    private boolean fileEnabled = false;
    
    /** 日志文件存放路径 */
    private String filePath = "./logs/app.log";

    // --- 级别控制 ---
    /** 全局默认日志级别 */
    private String level = "INFO";
    
    /** 细粒度包级别控制 (Key: 包名, Value: 级别) */
    private Map<String, String> levels = new HashMap<>();

    // --- 插件化控制 (核心增强) ---
    /** * 监听器排除列表 (黑名单)。
     * 填写 Listener 的简单类名，如 "ConsoleLogListener" 或 "FlexClickHouseLogListener"。
     * 被列入此名单的 Listener 将不会被挂载到全局总线上。
     */
    private List<String> excludeListeners = new ArrayList<>();

    // --- 高级生产特性 ---
    /** * 队列容量：默认 8192
     * 异步引擎的缓冲区大小，过大会占用过多堆内存。
     */
    private int queueCapacity = 8192;

    /** * [背压丢弃阈值]：默认 INFO
     * 当队列满时，级别低于此级别的日志将直接丢弃。
     * 建议设置为 INFO，确保 ERROR 等核心日志在高负载时不丢失。
     */
    private String discardThreshold = "INFO";

    /** * 后台线程轮询间隔 (ms)：默认 100
     * 较低的间隔能提高实时性，但会消耗微量 CPU；较高的间隔能合并 I/O。
     */
    private long pollIntervalMs = 100;

    /** * 优雅停机最大等待时间 (s)：默认 3
     * 在应用关闭时，给异步队列留出排空任务的时间。
     */
    private int stopTimeoutSeconds = 3;

    /** * [滚动策略] 是否开启按天滚动
     * 开启后，存量日志会在每日零点重命名为 app.log.yyyy-MM-dd
     */
    private boolean rollingEnabled = true;

    // --- 监听器加载模式控制 (SPI vs 注册表) ---
    /**
     * 是否启用注册表模式
     * <p>
     * - false（默认）：使用 SPI 模式，通过 ServiceLoader 自动发现 LogListener 实现
     * - true：使用注册表模式，用户通过代码调用 LogListenerRegistry.register() 动态注册监听器
     * </p>
     * <p>
     * 注册表模式示例：
     * <pre>
     * LogListenerRegistry.getInstance().register(ConsoleLogListener.class, new ConsoleLogListener(properties));
     * LogListenerRegistry.getInstance().register(FlexClickHouseLogListener.class, new FlexClickHouseLogListener(mapper));
     * </pre>
     * </p>
     */
    private boolean registryEnabled = false;

    /**
     * 判断是否启用注册表模式
     * @return true 表示使用注册表模式，false 表示使用 SPI 模式
     */
    public boolean isRegistryModeEnabled() {
        return registryEnabled;
    }
}