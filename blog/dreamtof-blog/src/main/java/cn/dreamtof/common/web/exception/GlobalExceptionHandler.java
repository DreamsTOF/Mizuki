package cn.dreamtof.common.web.exception;

import cn.dreamtof.common.web.utils.AuthErrorCode;
import cn.dreamtof.core.base.BaseResponse;
import cn.dreamtof.core.context.OperationContext;
import cn.dreamtof.core.exception.*;
import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;
import java.util.Date;

/**
 * 全局异常处理器 (DDD 重构插拔版)
 * <p>
 * 架构改进：
 * 1. 面向大管家 (CompositeExceptionRegistry)：不再写死具体的 ErrorCode，全部通过大管家动态路由。
 * 2. 完美解耦：即便拔掉 User 或 Auth 模块，此文件也无需修改。
 * 3. 职责分离：仅保留日志打印与 HTTP Status 的计算封装。
 * </p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 注入异常注册表大管家 (Spring 会自动注入被 @Primary 标记的 CompositeExceptionRegistry)
    private final BaseExceptionRegistry exceptionRegistry;

    public GlobalExceptionHandler(BaseExceptionRegistry exceptionRegistry) {
        this.exceptionRegistry = exceptionRegistry;
    }

    // =================================================================================
    // 1. 业务异常处理 (WARN) - 通常包含明确的业务逻辑错误
    // =================================================================================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> handleBusinessException(BusinessException e) {
        IErrorCode errorCode = ErrorCode.getByCode(e.getCode());
        if (errorCode == null) {
            errorCode = CommonErrorCode.SYSTEM_ERROR; 
        }
        logWarnWithContext(errorCode, e.getMessage(), e);
        HttpStatus status = determineHttpStatus(errorCode);
        return ResponseEntity.status(status)
                .body(new BaseResponse<>(errorCode.getCode(), null, e.getMessage(), false));
    }

    // =================================================================================
    // 2. 参数与协议异常 (WARN) - 拦截特定异常以提取友好的提示信息
    // =================================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        FieldError fieldError = bindingResult.getFieldError();
        IErrorCode errorCode = exceptionRegistry.getErrorCode(e); 
        String userMessage = (fieldError != null) 
                ? String.format("%s: %s", fieldError.getField(), fieldError.getDefaultMessage()) 
                : errorCode.getMessage();
        logWarnWithContext(errorCode, userMessage, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(errorCode.getCode(), null, userMessage, false));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<?>> handleJsonParseException(HttpMessageNotReadableException e) {
        IErrorCode errorCode = exceptionRegistry.getErrorCode(e);
        String detail = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        log.warn("⚠️ [WebInfra] | {} | HttpMessageNotReadableException | {}", errorCode.getMessage(), detail, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new BaseResponse<>(errorCode.getCode(), null, detail != null ? detail : errorCode.getMessage(), false));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<?>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        IErrorCode errorCode = exceptionRegistry.getErrorCode(e);
        logWarnWithContext(errorCode, e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new BaseResponse<>(errorCode.getCode(), null, e.getMessage(), false));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<BaseResponse<?>> handleNotLoginException(NotLoginException e) {
        log.warn("⚠️ [Auth] | {} | NotLoginException | Code:{} | {}",
                OperationContext.get().getTraceId(),
                AuthErrorCode.TOKEN_EXPIRED.getCode(),
                e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new BaseResponse<>(AuthErrorCode.TOKEN_EXPIRED.getCode(), null,
                        AuthErrorCode.TOKEN_EXPIRED.getMessage(), false));
    }

    // =================================================================================
    // 3. 系统兜底异常 (ERROR) - 处理所有未知的、未被单独拦截的异常
    // =================================================================================

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<BaseResponse<?>> handleAllExceptions(Throwable e) {
        IErrorCode errorCode = exceptionRegistry.getErrorCode(e);
        if (errorCode == null) {
            errorCode = CommonErrorCode.SYSTEM_ERROR;
        }
        logErrorWithContext(errorCode, e, e.getMessage());
        HttpStatus status = determineHttpStatus(errorCode);
        String msg = e.getMessage() != null ? e.getMessage() : errorCode.getMessage();
        return ResponseEntity.status(status)
                .body(new BaseResponse<>(errorCode.getCode(), null, msg, false));
    }

    // =================================================================================
    // 4. 私有辅助方法
    // =================================================================================

    /**
     * 根据 IErrorCode 的 code 范围段落决定 HttpStatus
     */
    private HttpStatus determineHttpStatus(IErrorCode errorCode) {
        int code = errorCode.getCode();
        
        // 1xxxx: 客户端错误
        if (code >= 10000 && code < 20000) return HttpStatus.BAD_REQUEST;
        
        // 2xxxx: 认证错误
        if (code >= 20000 && code < 30000) {
            if (code == 20000) return HttpStatus.UNAUTHORIZED; // UNAUTHORIZED
            if (code == 20001) return HttpStatus.FORBIDDEN;    // FORBIDDEN
            return HttpStatus.UNAUTHORIZED;
        }
        
        // 404xx: 资源不存在
        if (code == 40400 || code == 40402) return HttpStatus.NOT_FOUND;
        
        // 3xxxx: 业务逻辑错误
        if (code >= 30000 && code < 40000) return HttpStatus.BAD_REQUEST;
        
        // 其他（5xxxx, 9xxxx）: 服务器内部错误
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 记录 WARN 日志 (业务及参数异常使用)
     */
    private void logWarnWithContext(IErrorCode errorCode, String details) {
        logWarnWithContext(errorCode, details, null);
    }

    private void logWarnWithContext(IErrorCode errorCode, String details, Throwable e) {
        OperationContext.OperationInfo context = OperationContext.get();
        String errorSource = errorCode.getClass().getSimpleName();

        if (e != null) {
            log.warn("⚠️ [{}] | {} | {} | Code:{} | {}", 
                    context.getBusinessModule(),
                    context.getTraceId(),
                    errorSource,
                    errorCode.getCode(),
                    details, e);
        } else {
            log.warn("⚠️ [{}] | {} | {} | Code:{} | {}",
                    context.getBusinessModule(),
                    context.getTraceId(),
                    errorSource,
                    errorCode.getCode(),
                    details);
        }
    }

    /**
     * 记录 ERROR 日志 (系统崩溃或未知异常使用)
     */
    private void logErrorWithContext(IErrorCode errorCode, Throwable e, String details) {
        OperationContext.OperationInfo context = OperationContext.get();
        long duration = System.currentTimeMillis() - context.getStartTime();

        log.error("""
                
                ============== ❌ SYSTEM ERROR [TraceId: {}] ==============
                | Time      : {} (Duration: {}ms)
                | Method    : {}
                | Operator  : {} ({})
                | Params    : {}
                | Exception : {}
                | ErrorCode : {} ({})
                | Details   : {}
                ===========================================================
                """,
                context.getTraceId(),
                new Date(),
                duration,
                context.getMethodName(),
                context.getOperatorName(), context.getOperatorId(),
                Arrays.toString(context.getArgs()),
                e.getClass().getName(),
                errorCode.getCode(), errorCode.getClass().getSimpleName(),
                details,
                e); 
    }
}