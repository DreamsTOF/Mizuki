//package cn.dreamtof.infrastructure.config;
//
//import cn.dreamtof.log.config.LogProperties;
//import cn.dreamtof.log.core.LogWriter;
//import cn.dreamtof.log.spi.LogListener;
//import cn.dreamtof.log.application.listener.ConsoleLogListener;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//
///**
// * 🚀 日志系统业务层自动配置装配站
// * <p>
// * 核心改进：
// * 1. 移至业务公共包，由业务层主动控制装配逻辑。
// * 2. 采用 List&lt;LogListener&gt; 集合注入，实现真正的开闭原则 (OCP)，支持热插拔。
// * 3. 引入 excludeListeners 黑名单机制，可通过 YAML 灵活剔除不需要的输出端。
// * 4. 支持双模式加载：SPI 模式（默认）和注册表模式（动态注册）。
// * </p>
// *
// * <h3>监听器加载模式</h3>
// * <ul>
// *   <li><b>SPI 模式（默认）</b>：通过 Java ServiceLoader 自动发现 LogListener 实现，
// *       Spring 容器中的 LogListener Bean 也会被自动收集并注册</li>
// *   <li><b>注册表模式</b>：用户通过代码调用 LogListenerRegistry.register() 动态注册监听器，
// *       此时 SPI 模式被禁用，Spring 容器中的 LogListener Bean 也不会被自动注册</li>
// * </ul>
// *
// * <h3>注册表模式使用示例</h3>
// * <pre>
// * // 配置文件
// * dreamtof.log.registry-enabled=true
// *
// * // 代码中动态注册
// * LogListenerRegistry.getInstance().register(ConsoleLogListener.class, new ConsoleLogListener(properties));
// * LogListenerRegistry.getInstance().register(FlexClickHouseLogListener.class, new FlexClickHouseLogListener(mapper));
// * </pre>
// */
//@Configuration
//@EnableConfigurationProperties(LogProperties.class)
//@ConditionalOnProperty(prefix = "dreamtof.log", name = "enabled", havingValue = "true", matchIfMissing = true)
//public class LogAutoConfiguration {
//
//    private final LogProperties properties;
//    private final List<LogListener> allListeners;
//
//    /**
//     * 依赖注入：ObjectProvider 会自动从 Spring 容器中抓取所有实现了 LogListener 的 Bean
//     */
//    public LogAutoConfiguration(LogProperties properties,
//                                ObjectProvider<List<LogListener>> listenersProvider) {
//        this.properties = properties;
//        this.allListeners = listenersProvider.getIfAvailable(List::of);
//    }
//
//    /**
//     * 将控制台监听器声明为 Bean 纳入 Spring 管理。
//     * 同时受 dreamtof.log.console-enabled 属性控制。
//     *
//     * 注意：在注册表模式下，此 Bean 不会被自动注册到 LogWriter，
//     * 需要用户通过代码调用 LogListenerRegistry.register() 进行注册。
//     */
//    @Bean
//    @ConditionalOnProperty(prefix = "dreamtof.log", name = "console-enabled", havingValue = "true", matchIfMissing = true)
//    public ConsoleLogListener consoleLogListener(LogProperties properties) {
//        return new ConsoleLogListener(properties);
//    }
//
//    @PostConstruct
//    public void init() {
//        // 1. 同步配置到核心引擎 LogWriter（会自动初始化监听器）
//        LogWriter.updateConfig(properties);
//
//        // 2. 根据当前模式决定是否处理 Spring 容器中的监听器
//        if (properties.isRegistryModeEnabled()) {
//            // 注册表模式：等待用户通过代码动态注册监听器
//            System.out.println("[LogSystem] Registry mode active. Use LogListenerRegistry.register() to add listeners. 📋");
//        } else {
//            // SPI 模式：处理 Spring 容器中的监听器（作为 SPI 的补充）
//            registerSpringListeners();
//        }
//    }
//
//    /**
//     * 注册 Spring 容器中的监听器（仅 SPI 模式下执行）
//     */
//    private void registerSpringListeners() {
//        List<String> excludes = properties.getExcludeListeners();
//        int registeredCount = 0;
//
//        for (LogListener listener : allListeners) {
//            String listenerName = listener.getClass().getSimpleName();
//
//            // 如果该监听器在 YAML 的黑名单中，则跳过注册
//            if (excludes != null && excludes.contains(listenerName)) {
//                System.out.println("[LogSystem] 🚫 Listener excluded by config: " + listenerName);
//                continue;
//            }
//
//            // 检查是否已注册（避免 SPI 和 Spring 重复注册）
//            if (!isListenerRegistered(listener)) {
//                LogWriter.registerListener(listener);
//                registeredCount++;
//            }
//        }
//
//        // 打印引擎初始化与装配详情
//        System.out.println(String.format(
//                "[LogSystem] SPI mode active. Registered %d Spring-managed listeners. 🚀",
//                registeredCount
//        ));
//    }
//
//    /**
//     * 检查监听器是否已注册
//     */
//    private boolean isListenerRegistered(LogListener listener) {
//        String listenerClassName = listener.getClass().getName();
//        return LogWriter.getListenerCount() > 0 &&
//               LogWriter.hasListenerOfClass(listenerClassName);
//    }
//}
