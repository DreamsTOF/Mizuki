package cn.dreamtof.log.core;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 自定义日志工厂
 * 负责统一管理和分配 CustomLogImpl 实例
 */
public class CustomLoggerFactory implements ILoggerFactory {

    // 缓存已经创建的 Logger 实例，避免重复创建
    private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(String name) {
        // 如果缓存中有，直接返回；如果没有，实例化我们的 CustomLogImpl
        return loggerMap.computeIfAbsent(name, CustomLogImpl::new);
    }
}