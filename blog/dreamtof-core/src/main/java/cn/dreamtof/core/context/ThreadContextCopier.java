package cn.dreamtof.core.context;

/**
 * 线程上下文拷贝器 SPI (Service Provider Interface)
 * <p>
 * 作用：允许上层业务（如 DB 模块、租户模块、MDC 等）向底层注册自己的 ThreadLocal 传递逻辑。
 * 从而保证 Core 模块不依赖任何具体的第三方框架。
 * </p>
 */
public interface ThreadContextCopier {
    
    /** 1. 在父线程调用，捕获并返回当前的上下文状态 */
    Object capture();
    
    /** 2. 在子线程调用，恢复上下文状态 */
    void restore(Object contextSnapshot);
    
    /** 3. 在子线程结束时调用，清理上下文，防止线程池污染 */
    void clear();
}