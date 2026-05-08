package cn.dreamtof.log.core;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * SLF4J 2.x SPI 服务提供者实现
 */
public class CustomSLF4JServiceProvider implements SLF4JServiceProvider {

    /**
     * 声明我们支持的 SLF4J API 版本
     */
    public static String REQUESTED_API_VERSION = "2.0.99"; // 支持 2.0.x 系列

    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() {
        try {
            // 在 SLF4J 初始化时，绑定我们自己的核心工厂
            loggerFactory = new CustomLoggerFactory();

            // Marker 和 MDC 我们直接复用 SLF4J 官方提供的基础实现即可
            markerFactory = new BasicMarkerFactory();
            mdcAdapter = new BasicMDCAdapter();

            System.out.println("[LogSystem] Custom SLF4J provider initialized successfully");
        } catch (Exception e) {
            // Fallback: 输出详细错误信息到 System.err，确保问题可见
            System.err.println("[CRITICAL] Failed to initialize custom logger: " + e.getMessage());
            e.printStackTrace(System.err);

            // 重新抛出异常，让 SLF4J 知道初始化失败
            throw e;
        }
    }
}