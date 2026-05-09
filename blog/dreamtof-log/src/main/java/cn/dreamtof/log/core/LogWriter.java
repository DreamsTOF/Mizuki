package cn.dreamtof.log.core;

import cn.dreamtof.log.config.LogProperties;
import cn.dreamtof.log.spi.LogListener;
import cn.dreamtof.log.spi.LogListenerLoader;
import cn.dreamtof.log.spi.LogListenerRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ⚙️ LogWriter: 全局日志总线 & 异步分发引擎 (纯粹版)
 * <p>
 * 【架构演进】：
 * 本类已完全剥离文本格式化、ANSI 着色、文件 I/O 以及控制台输出的具体逻辑。
 * 现已降级为纯粹的"事件总线 (Event Bus)"。
 *
 * 【核心职责】：
 * 1. 异步解耦：提供无锁队列接纳业务线程的日志事件，避免业务线程被阻塞。
 * 2. 背压保护：当队列满时，根据权重丢弃低级别日志，保护 JVM 免受 OOM 威胁。
 * 3. 高效分发：利用 JDK 21 虚拟线程后台轮询，将原始事件分发给所有注册的 LogListener。
 * 4. 动态路由：维护包级别的日志级别缓存，支持运行时动态更新配置。
 * 5. 双模式加载：支持 SPI 自动发现和注册表动态注册两种监听器加载模式。
 * </p>
 *
 * <h3>监听器加载模式</h3>
 * <ul>
 *   <li><b>SPI 模式（默认）</b>：通过 Java ServiceLoader 自动发现 LogListener 实现</li>
 *   <li><b>注册表模式</b>：用户通过代码调用 LogListenerRegistry.register() 动态注册监听器</li>
 * </ul>
 *
 * <h3>配置示例</h3>
 * <pre>
 * # SPI 模式（默认，无需配置）
 * dreamtof.log.level=INFO
 *
 * # 注册表模式（在代码中动态注册）
 * dreamtof.log.level=INFO
 * dreamtof.log.registry-enabled=true
 * 
 * // 代码中动态注册
 * LogListenerRegistry.getInstance().register(ConsoleLogListener.class, new ConsoleLogListener(properties));
 * </pre>
 */
public class LogWriter implements Runnable {

    /**
     * 监听器加载模式
     */
    public enum ListenerLoadMode {
        /** SPI 模式：通过 ServiceLoader 自动发现 */
        SPI,
        /** 注册表模式：用户通过代码动态注册 */
        REGISTRY
    }

    /** 异步日志事件缓冲区 */
    private final BlockingQueue<LogEvent> eventQueue;

    /** 引擎运行状态标志 */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /** * 🎧 监听器注册表 (核心重构点)
     * 采用 CopyOnWriteArrayList 保证在并发分发时的线程安全。
     * 所有具体的输出格式（JSON/纯文本）、输出地（Console/File/Kafka 等）均由 Listener 实现。
     */
    private static final List<LogListener> LISTENERS = new CopyOnWriteArrayList<>();

    /** 全局日志配置 */
    private static volatile LogProperties config = new LogProperties();

    /** 二级缓存架构：用于存放原始配置的包级别日志权限 */
    private static volatile Map<String, LogLevel> RAW_CONFIG_MAP = Map.of();

    /** * 计算结果缓存：避免每次打日志都去进行包名截取和层级计算
     * key: loggerName (如 cn.dreamtof.service.UserService)
     * value: 最终决议的日志级别
     */
    private static final ConcurrentHashMap<String, LogLevel> COMPUTED_CACHE = new ConcurrentHashMap<>();

    /** 引擎单例 */
    private static final LogWriter SINGLETON = new LogWriter();

    /** 当前监听器加载模式 */
    private static volatile ListenerLoadMode currentMode = ListenerLoadMode.SPI;

    /** 监听器是否已初始化 */
    private static volatile boolean listenersInitialized = false;

    static {
        try {
            /*
             * 🚀 启动后台分发线程
             * 使用虚拟线程（Virtual Thread）作为后台守护线程。
             * 它拥有极低的上下文切换成本，且不消耗平台线程池，非常适合处理 I/O 调度或队列轮询。
             */
            Thread.ofVirtual().name("Log-Bus-Dispatcher").start(SINGLETON);

            /*
             * 🧬 注册优雅停机钩子 (Graceful Shutdown)
             * 确保 JVM 退出时，能留出一定的时间排空积压在队列中的日志，防止丢失关键现场。
             */
            Runtime.getRuntime().addShutdownHook(new Thread(SINGLETON::stop));

            System.out.println("[LogSystem] LogWriter static initialization completed");
        } catch (Exception e) {
            // Fallback: 输出详细错误信息到 System.err，确保问题可见
            System.err.println("[CRITICAL] Failed to initialize LogWriter: " + e.getMessage());
            e.printStackTrace(System.err);

            // 不抛出异常，避免阻止类加载，但日志系统可能无法正常工作
            // 后续调用会通过 LISTENERS.isEmpty() 检测到问题
        }
    }

    private LogWriter() {
        // 根据配置初始化队列容量，决定最大能抗住多少瞬时突发日志量
        this.eventQueue = new LinkedBlockingQueue<>(config.getQueueCapacity());
    }

    /**
     * 🚀 投递日志 (生产者入口)
     * 【调用时机】：业务线程调用的唯一入口。
     *
     * @param event 原始未格式化的日志事件
     */
    public static void enqueue(LogEvent event) {
        // offer 是一种非阻塞尝试，如果队列已满会立即返回 false，而不是像 put 一样阻塞业务线程
        boolean success = SINGLETON.eventQueue.offer(event);

        if (!success) {
            // 🛑 触发背压 (Backpressure) 丢弃策略
            LogLevel threshold = LogLevel.fromString(config.getDiscardThreshold());

            // 如果日志重要程度大于等于丢弃阈值，记录内部错误以示警告
            if (event.getLevel().getWeight() >= threshold.getWeight()) {
                System.err.println("[LogSystem] Queue Full! Dropping high priority log: " + event.getLevel().getLabel());
            }
            // 否则直接静默丢弃，保护主业务可用性
        }
    }

    /**
     * 🔄 核心调度循环 (消费者引擎)
     * 由虚拟线程执行，不断从队列中提取事件并分发。
     */
    @Override
    public void run() {
        // 只要引擎还在运行，或者队列里还有没处理完的日志，就继续轮询
        while (running.get() || !eventQueue.isEmpty()) {
            try {
                // poll 带有超时机制，避免无休止的阻塞，便于优雅停机
                LogEvent event = eventQueue.poll(config.getPollIntervalMs(), TimeUnit.MILLISECONDS);
                if (event != null) {
                    dispatch(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running.set(false);
            } catch (Exception e) {
                // 拦截不可预知的引擎异常，防止分发线程崩溃导致整个日志系统瘫痪
                System.err.println("[LogSystem Critical] Dispatch engine error: " + e.getMessage());
            }
        }
    }

    /**
     * 📡 事件分发器 (核心重构点)
     * 彻底剥离了具体的格式化和写入逻辑。
     * 只做纯粹的广播：将 event 扔给所有注册的 Listener。
     *
     * @param event 日志事件
     */
    private void dispatch(LogEvent event) {
        if (LISTENERS.isEmpty()) {
            return;
        }

        // 遍历所有监听器，各显神通（比如 ConsoleListener 会去渲染颜色，FileListener 会去写磁盘）
        for (LogListener listener : LISTENERS) {
            try {
                listener.onLog(event);
            } catch (Exception e) {
                // 隔离 Listener 级别的异常，防止某个写文件失败导致控制台也不输出了
                System.err.println("[LogSystem] Listener execute failed for [" + listener.getClass().getSimpleName() + "]: " + e.getMessage());
            }
        }
    }

    /**
     * 🛑 优雅停机
     * 尝试在关闭前将队列里的残余日志消费干净。
     */
    private void stop() {
        running.set(false);
        int maxRetries = config.getStopTimeoutSeconds() * 10;
        int count = 0;

        // 循环等待队列排空，或者达到最大超时时间
        while (!eventQueue.isEmpty() && count++ < maxRetries) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * 🔄 动态更新配置
     * 可以在应用运行期间，无需重启，动态调整包日志级别或队列参数。
     *
     * @param newConfig 新的配置对象
     */
    public static void updateConfig(LogProperties newConfig) {
        config = newConfig;

        // 1. 重建原始层级配置表
        Map<String, LogLevel> newRawMap = new ConcurrentHashMap<>();
        if (newConfig.getLevels() != null) {
            newConfig.getLevels().forEach((pkg, level) ->
                    newRawMap.put(pkg, LogLevel.fromString(level))
            );
        }
        RAW_CONFIG_MAP = newRawMap;

        // 2. 清空计算缓存，迫使下一次打日志时重新决议级别
        COMPUTED_CACHE.clear();

        // 3. 根据配置初始化监听器（仅首次）
        if (!listenersInitialized) {
            initializeListeners(newConfig);
        }
    }

    /**
     * 🎯 初始化监听器
     * 根据配置决定使用 SPI 模式还是注册表模式加载监听器。
     * 两种模式互斥，启用了注册表模式则 SPI 不再工作。
     *
     * @param newConfig 日志配置
     */
    private static synchronized void initializeListeners(LogProperties newConfig) {
        if (listenersInitialized) {
            return;
        }

        // 判断是否启用注册表模式
        if (newConfig.isRegistryModeEnabled()) {
            currentMode = ListenerLoadMode.REGISTRY;
            LogListenerLoader.setEnabled(false);
            LogListenerRegistry.getInstance().setEnabled(true);

            System.out.println("[LogSystem] Registry mode enabled. Waiting for dynamic registration via LogListenerRegistry.register()");
        } else {
            currentMode = ListenerLoadMode.SPI;
            LogListenerLoader.setEnabled(true);
            LogListenerRegistry.getInstance().setEnabled(false);

            // 通过 SPI 加载监听器
            List<LogListener> spiListeners = LogListenerLoader.loadListeners();
            for (LogListener listener : spiListeners) {
                registerListener(listener);
            }

            System.out.println(String.format(
                    "[LogSystem] SPI mode enabled. Loaded %d listeners via ServiceLoader.",
                    spiListeners.size()
            ));
        }

        listenersInitialized = true;
    }

    /**
     * 📊 获取当前监听器加载模式
     */
    public static ListenerLoadMode getCurrentMode() {
        return currentMode;
    }

    /**
     * 📊 判断监听器是否已初始化
     */
    public static boolean isListenersInitialized() {
        return listenersInitialized;
    }

    /**
     * 🔄 重置监听器初始化状态（仅用于测试）
     */
    public static void resetListenersInitialization() {
        listenersInitialized = false;
        LISTENERS.clear();
        LogListenerRegistry.getInstance().clear();
        LogListenerRegistry.getInstance().setEnabled(false);
        LogListenerLoader.setEnabled(true);
        LogListenerLoader.reset();
        currentMode = ListenerLoadMode.SPI;
    }

    /**
     * 🔍 获取决议后的日志级别 (带缓存提升性能)
     * * @param loggerName 包名或类名
     * @return 最终生效的 LogLevel
     */
    public static LogLevel getResolvedLevel(String loggerName) {
        // 一级加速：直接从缓存获取，O(1) 复杂度
        LogLevel cached = COMPUTED_CACHE.get(loggerName);
        if (cached != null) {
            return cached;
        }

        // 二级兜底：缓存未命中，进行树形递归计算
        LogLevel resolved = computeLevel(loggerName);
        COMPUTED_CACHE.put(loggerName, resolved);
        return resolved;
    }

    /**
     * 🧠 树形日志级别推断算法
     * 模拟 Logback/Log4j 的继承特性。
     * 例如查询 "cn.dreamtof.user.service":
     * 1. 找 cn.dreamtof.user.service -> 未命中
     * 2. 找 cn.dreamtof.user -> 未命中
     * 3. 找 cn.dreamtof -> 命中! 返回其配置的级别
     */
    private static LogLevel computeLevel(String loggerName) {
        String current = loggerName;
        while (current.contains(".")) {
            LogLevel level = RAW_CONFIG_MAP.get(current);
            if (level != null) return level;
            // 不断截掉最后一段，向上溯源
            current = current.substring(0, current.lastIndexOf("."));
        }

        // 查找最顶层的包配置
        LogLevel rootLevel = RAW_CONFIG_MAP.get(current);
        if (rootLevel != null) return rootLevel;

        // 全局兜底级别
        return LogLevel.fromString(config.getLevel());
    }

    /**
     * 🎧 注册日志监听器
     * 通过外部 SPI 机制或 Spring 自动装配调用此方法，将处理终端接入总线。
     */
    public static void registerListener(LogListener listener) {
        if (!LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    /**
     * 🎧 注销日志监听器
     * 从事件总线中移除指定的监听器。
     *
     * @param listener 要移除的监听器实例
     */
    public static void unregisterListener(LogListener listener) {
        LISTENERS.remove(listener);
    }

    /**
     * 📊 获取已注册监听器的数量
     */
    public static int getListenerCount() {
        return LISTENERS.size();
    }

    /**
     * 🔍 检查指定类名的监听器是否已注册
     * @param listenerClassName 监听器类的全限定名
     */
    public static boolean hasListenerOfClass(String listenerClassName) {
        for (LogListener listener : LISTENERS) {
            if (listener.getClass().getName().equals(listenerClassName)) {
                return true;
            }
        }
        return false;
    }
}