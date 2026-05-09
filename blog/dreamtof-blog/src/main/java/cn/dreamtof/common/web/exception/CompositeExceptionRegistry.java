package cn.dreamtof.common.web.exception;

import cn.dreamtof.core.exception.BaseExceptionRegistry;
import cn.dreamtof.core.exception.CommonErrorCode;
import cn.dreamtof.core.exception.IErrorCode;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 复合异常注册器 (大管家)
 * 职责：动态聚合所有模块的注册器，实现异常的全局路由。模块插拔时自动感知。
 */
@Component
@Primary // 极其重要：确保 ExceptionHandler 注入的是这个类
public class CompositeExceptionRegistry extends BaseExceptionRegistry {

    private final List<BaseExceptionRegistry> delegates;

    public CompositeExceptionRegistry(List<BaseExceptionRegistry> registries) {
        // 自动注入所有的 Registry，但必须排除自己，防止无限递归死循环
        this.delegates = registries.stream()
                .filter(r -> r.getClass() != this.getClass())
                .collect(Collectors.toList());
    }

    @Override
    public IErrorCode getErrorCode(Throwable throwable) {
        if (throwable == null) return CommonErrorCode.SYSTEM_ERROR;

        Throwable root = super.unwrap(throwable);
        Class<? extends Throwable> clazz = root.getClass();

        // 1. 查复合类的全局缓存
        IErrorCode cached = cache.get(clazz);
        if (cached != null) return cached;

        // 2. 遍历所有子模块注册器进行浅查找 (不触发兜底)
        IErrorCode match = null;
        for (BaseExceptionRegistry delegate : delegates) {
            match = delegate.findMatch(clazz);
            if (match != null) {
                break; // 一旦有模块认领了这个异常，立刻停止
            }
        }

        // 3. 全局兜底
        if (match == null) {
            match = CommonErrorCode.SYSTEM_ERROR;
        }

        // 4. 写入全局缓存
        cache.put(clazz, match);
        return match;
    }
}