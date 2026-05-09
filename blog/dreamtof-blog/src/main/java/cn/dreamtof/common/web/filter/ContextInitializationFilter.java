package cn.dreamtof.common.web.filter;

import cn.dev33.satoken.stp.StpUtil;
import cn.dreamtof.common.util.SaTokenMixUtil;
import cn.dreamtof.core.context.CurrentOperatorSupplier;
import cn.dreamtof.core.context.OperationContext;
import cn.dreamtof.core.context.Operator;
import cn.dreamtof.common.web.utils.RequestKeyHolder;
import cn.dreamtof.core.utils.TraceIdHandler;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static cn.dreamtof.core.constants.GlobalConstants.DEVICE_HEADER;
import static cn.dreamtof.core.constants.GlobalConstants.TRACE_HEADER;

/**
 * 👑 全局上下文过滤器
 * 职责：在请求入口处捕获 TraceId、IP、设备 ID，并初始化 ScopedValue 容器。
 */
@Component
@Order(-100)
@Slf4j
public class ContextInitializationFilter implements Filter {

    @Value("${spring.application.name:Unknown-App}")
    private String applicationName;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        // 1. 提取基础元数据
        UUID traceId = TraceIdHandler.fetchTraceId(httpRequest.getHeader(TRACE_HEADER));
        String deviceId = httpRequest.getHeader(DEVICE_HEADER);
        String clientIp = getRemoteIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent"); // ✅ 补上漏掉的 UA

        // 2. 直接在此处初始化 Operator (不再依赖外部 Supplier)
        Operator op = null;
        try {
            // 只有当 Sa-Token 判定已登录时才创建 Operator
            if (StpUtil.isLogin()) {
                op = Operator.builder()
                        .id((java.io.Serializable) StpUtil.getLoginId())
                        .name(SaTokenMixUtil.findString("username")) // 假设 Payload 里存了 name
                        .ip(clientIp)
                        .deviceId(deviceId)
                        .useAgent(userAgent)
                        .build();
            }
        } catch (Exception ignored) {
            log.error("Sa-Token 初始化异常", ignored);
            // 防止 Sa-Token 未初始化或异常导致请求中断
        }

        // 3. 构造完整的上下文快照
        OperationContext.OperationInfo info = OperationContext.OperationInfo.builder()
                .isDefault(false)
                .traceId(traceId)
                .deviceId(deviceId)
                .userAgent(userAgent) // ✅ 填充到上下文中
                .startTime(startTime)
                .clientIp(clientIp)
                .operator(op)
                .businessModule(applicationName)
                .build();

        // 4. 使用 ScopedValue 开启生命周期包裹
        try {
            ScopedValue.where(OperationContext.getScopedValue(), info)
                       .where(RequestKeyHolder.holder(), new AtomicReference<>())
                       .run(() -> {
                           try {
                               chain.doFilter(request, response);
                           } catch (Exception e) {
                               throw new RuntimeException(e);
                           }
                       });
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException ioException) throw ioException;
            if (cause instanceof ServletException servletException) throw servletException;
            throw e;
        }
    }

    private String getRemoteIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0] : ip;
    }
}