package cn.dreamtof.log.core;

import cn.dreamtof.core.context.OperationContext;
import cn.dreamtof.core.context.Operator;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Marker;

/**
 * 📝 LogEvent: 结构化日志事件载体 (ClickHouse 高性能版)
 * <p>
 * 【架构特性】：
 * 1. 物理快照：构造时立即捕获 timestamp 和 threadName，防止在 Virtual Thread 异步消费时丢失第一现场。
 * 2. 拓扑追踪：强依赖 OperationContext.OperationInfo，无缝衔接 UUIDv7 驱动的 Trace/Span 树状追踪。
 * 3. 极速比对：Marker 采用对象引用存储，供下游使用 `==` 进行纳秒级路由比对。
 * </p>
 */
@Getter
public class LogEvent {
    /** 日志级别权重 */
    private final LogLevel level;
    /** 原始消息模版 (含 {} 占位符，延迟到消费端格式化) */
    private final String messagePattern;
    /** 动态参数数组 */
    private final Object[] args;
    /** 异常堆栈信息 */
    private final Throwable throwable;

    /** * 🚀 核心上下文 (Contextual Link)
     * 包含 TraceId, SpanId, ParentSpanId (UUIDv7 格式) 以及 Operator 业务身份。
     */
    private final OperationContext.OperationInfo context;

    /** 记录器名称 (Logger Name，通常为类全限定名) */
    private final String loggerName;
    
    /** 物理发生时间戳 (毫秒精度，ClickHouse 排序核心) */
    private final long timestamp;

    /** * 🚀 现场线程名 (Thread Name)
     * 【排查关键】：记录打日志时的原始业务线程名称（如 http-nio-8080-exec-1）。
     */
    private final String threadName;

    /** 路由标记 (Marker，如 AUDIT, SECURITY) */
    @Setter
    private Marker marker;

    /**
     * 构造函数：初始化日志物理快照
     * * @param level          日志级别
     * @param messagePattern 消息模板
     * @param args           参数
     * @param throwable      异常
     * @param context        当前线程绑定的上下文快照
     * @param loggerName     Logger名称
     */
    public LogEvent(LogLevel level, String messagePattern, Object[] args, Throwable throwable,
                    OperationContext.OperationInfo context, String loggerName) {
        this.level = level;
        this.messagePattern = messagePattern;
        this.args = args;
        this.throwable = throwable;
        this.context = context;
        this.loggerName = loggerName;
        
        // ⚡️ 冻结时间与物理线程空间
        this.timestamp = System.currentTimeMillis();
        this.threadName = Thread.currentThread().getName();
    }

    /**
     * 【语法糖】：快捷获取操作人信息，防 NPE
     *
     * @return 当前操作人对象 Operator 或 null
     */
    public Operator getOperator() {
        return context != null ? context.getOperator() : null;
    }
}