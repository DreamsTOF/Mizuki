package cn.dreamtof.core.config;

import cn.dreamtof.core.context.ThreadContextCopier;
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MDC 上下文传播器
 * <p>
 * 将父线程的 MDC 上下文（如 traceId）传播到子线程，
 * 确保异步任务中的日志也能正确关联到请求链路。
 * </p>
 */
@Component
@Primary
public class MdcCopier implements ThreadContextCopier {

    @Override
    public Object capture() {
        return MDC.getCopyOfContextMap();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restore(Object contextSnapshot) {
        if (contextSnapshot instanceof Map) {
            Map<String, String> contextMap = (Map<String, String>) contextSnapshot;
            MDC.setContextMap(contextMap);
        }
    }

    @Override
    public void clear() {
        MDC.clear();
    }
}
