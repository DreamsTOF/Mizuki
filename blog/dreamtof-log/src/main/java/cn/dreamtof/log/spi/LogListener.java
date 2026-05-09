package cn.dreamtof.log.spi;

import cn.dreamtof.log.core.LogEvent;
import org.springframework.stereotype.Component;

/**
 * 日志监听器 SPI 接口
 * <p>
 * 任何想要消费日志的模块（如 Audit 审计、Monitor 监控），
 * 只需要实现此接口并注册到 LogWriter 即可。
 * </p>
 */

public interface LogListener {
    /**
     * 处理日志事件
     * 注意：此方法是在 LogWriter 的独立线程中被调用的，
     * 请确保处理逻辑高效，或者内部再次异步，避免阻塞日志主线程。
     */
    void onLog(LogEvent event);
}
