package cn.dreamtof.core.exception;

import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * 异常注册表基类 - 提供动态注册与查找引擎
 */
@Slf4j
public abstract class BaseExceptionRegistry {

    protected final Map<Class<? extends Throwable>, IErrorCode> mappings = new HashMap<>(128);
    protected final Map<Class<? extends Throwable>, IErrorCode> cache = new ConcurrentHashMap<>(128);

    public void register(Class<? extends Throwable> exceptionClass, IErrorCode errorCode) {
        if (mappings.containsKey(exceptionClass)) {
            log.debug("Override exception mapping: {} -> {}", exceptionClass.getSimpleName(), errorCode.getCode());
        }
        mappings.put(exceptionClass, errorCode);
        cache.clear(); 
    }

    public IErrorCode getErrorCode(Throwable throwable) {
        if (throwable == null) return CommonErrorCode.SYSTEM_ERROR;

        Throwable root = unwrap(throwable);
        Class<? extends Throwable> clazz = root.getClass();

        IErrorCode cached = cache.get(clazz);
        if (cached != null) return cached;

        // 调用抽取出的查找逻辑
        IErrorCode match = findMatch(clazz);

        if (match == null) {
            match = CommonErrorCode.SYSTEM_ERROR;
        }
        cache.put(clazz, match);
        return match;
    }

    /**
     * 核心递归查找逻辑 (向子类和 Composite 开放)
     */
    public IErrorCode findMatch(Class<? extends Throwable> clazz) {
        Class<?> searchType = clazz;
        while (searchType != null && Throwable.class.isAssignableFrom(searchType)) {
            IErrorCode match = mappings.get(searchType);
            if (match != null) return match;
            searchType = searchType.getSuperclass();
        }
        return null; // 找不到返回 null，不直接兜底
    }

    /**
     * 异常拆包 (必须改为 protected，供 Composite 使用)
     */
    protected Throwable unwrap(Throwable e) {
        int depth = 0;
        while (depth < 5) {
            if (e instanceof InvocationTargetException) e = ((InvocationTargetException) e).getTargetException();
            else if (e instanceof UndeclaredThrowableException) e = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
            else if (e instanceof ExecutionException) e = e.getCause();
            else break;
            depth++;
            if (e == null) break;
        }
        return e;
    }
}