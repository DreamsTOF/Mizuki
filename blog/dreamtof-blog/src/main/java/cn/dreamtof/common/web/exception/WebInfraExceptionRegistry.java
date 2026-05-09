package cn.dreamtof.common.web.exception;

import cn.dreamtof.core.exception.BaseExceptionRegistry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


/**
 * 用户/Web模块异常注册器
 * 职责：映射 Spring MVC 常用异常、校验异常以及数据库持久化相关异常
 */
public class WebInfraExceptionRegistry extends BaseExceptionRegistry {

    public WebInfraExceptionRegistry() {
        // ========================================================================
        // 1. Web 层参数校验与请求异常
        // ========================================================================
        
        // Spring Validation: @RequestBody 校验失败
        register(MethodArgumentNotValidException.class, WebInfraErrorCode.PARAM_FORMAT_ERROR);

        // 缺少必要的请求参数
        register(MissingServletRequestParameterException.class, WebInfraErrorCode.PARAM_IS_NULL);
        
        // 参数类型转换失败 (例如期待 int 结果传了 string)
        register(MethodArgumentTypeMismatchException.class, WebInfraErrorCode.PARAM_FORMAT_ERROR);

        // HTTP 方法不匹配 (如 GET 接口被 POST 调用)
        register(HttpRequestMethodNotSupportedException.class, WebInfraErrorCode.HTTP_METHOD_NOT_SUPPORTED);
        
        // Content-Type 不支持 (如接口要求 application/json)
        register(HttpMediaTypeNotSupportedException.class, WebInfraErrorCode.MEDIA_TYPE_NOT_SUPPORTED);


        // ========================================================================
        // 2. 数据库与持久化异常 (基于 Spring DAO 体系)
        // ========================================================================
        
        // 违反唯一约束 (重复索引)
        register(DuplicateKeyException.class, WebInfraErrorCode.DUPLICATE_KEY);
        
        // 数据完整性违规 (外键约束、字段非空等)
        register(DataIntegrityViolationException.class, WebInfraErrorCode.DATA_INTEGRITY_VIOLATION);
        
        // 乐观锁失败
        register(OptimisticLockingFailureException.class, WebInfraErrorCode.OPTIMISTIC_LOCK_ERROR);
        
        
        // ========================================================================
        // 3. 兜底策略
        // ========================================================================
        
        // 某些通用的 DataAccessException 可以指向通用数据库错误
        register(org.springframework.dao.DataAccessException.class, WebInfraErrorCode.DATABASE_ERROR);
    }
}