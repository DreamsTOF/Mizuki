package cn.dreamtof.audit.aspect;

import cn.dreamtof.audit.annotation.AuditLog;
import cn.dreamtof.core.context.OperationContext;
import cn.dreamtof.core.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.dreamtof.log.core.LogMarkers.AUDIT;

@Slf4j
@Aspect
@Component
@Order(1)
@ConditionalOnProperty(prefix = "dreamtof.auto-audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OperationLogAspect {


    @Around("@within(org.springframework.web.bind.annotation.RestController) " +
            "|| @annotation(io.swagger.v3.oas.annotations.Operation)")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取基础上下文信息
        OperationContext.OperationInfo info = OperationContext.get();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. 初始化元数据（如果上下文尚未初始化）
        if (info.isDefault()) {
            info.setBusinessModule(resolveModule(joinPoint));
            info.setBusinessAction(resolveAction(method));
            info.setMethodName(signature.toShortString());
            info.setArgs(joinPoint.getArgs());
        }

        // 3. 注入 TraceId 到日志 MDC
        if (info.getTraceId() != null) {
            MDC.put("traceId", info.getTraceId().toString());
        }

        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMsg = null;

        try {
            // 执行目标方法
            return joinPoint.proceed();
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String httpMethod = (attributes != null) ? attributes.getRequest().getMethod() : "UNKNOWN";
            String requestUri = (attributes != null) ? attributes.getRequest().getRequestURI() : "";

            boolean isWriteOperation = !"GET".equalsIgnoreCase(httpMethod);
            boolean hasAuditLog = method.isAnnotationPresent(AuditLog.class);
            boolean hasOperation = method.isAnnotationPresent(Operation.class);

            /**
             * 重新定义的强力审计规则：
             * 1. 显式标注 @AuditLog -> 必记（最高优先级）
             * 2. 执行失败 (!success) -> 必记（排查线上事故）
             * 3. 所有的写操作 (POST/PUT/DELETE) -> 必记（包含你的业务修改和 Actuator 配置刷新）
             * 4. 只有成功的 GET 请求 -> 被排除（彻底解决查询导致的日志爆炸）
             */
            if (!success || hasAuditLog || isWriteOperation) {
                // 如果是第三方包（如 Actuator），虽然 info 里的 businessAction 可能是空的
                // 但 requestUri 依然能告诉你它刷新了配置
                printAuditLog(info, costTime, success, errorMsg);
            }

            MDC.remove("traceId");
        }
    }

    private void printAuditLog(OperationContext.OperationInfo info, long cost, boolean success, String errorMsg) {
        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("logType", "AUDIT_LOG");
        logMap.put("traceId", info.getTraceId());
        logMap.put("module", info.getBusinessModule());
        logMap.put("action", info.getBusinessAction());
        logMap.put("operatorId", info.getOperatorId());
        logMap.put("operatorName", info.getOperatorName());
        logMap.put("clientIp", info.getClientIp());
        logMap.put("costMs", cost);
        logMap.put("status", success ? "SUCCESS" : "FAIL");
        if (!success) {
            logMap.put("error", errorMsg);
        }
        log.info(AUDIT, "{}", JsonUtils.toJsonString(logMap));
    }

    private String resolveModule(ProceedingJoinPoint joinPoint) {
        Tag tag = joinPoint.getTarget().getClass().getAnnotation(Tag.class);
        return (tag != null && !tag.name().isEmpty()) ? tag.name() : joinPoint.getTarget().getClass().getSimpleName();
    }

    private String resolveAction(Method method) {
        Operation op = method.getAnnotation(Operation.class);
        return (op != null && !op.summary().isEmpty()) ? op.summary() : method.getName();
    }
}