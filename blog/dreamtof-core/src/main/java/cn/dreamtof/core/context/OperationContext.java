package cn.dreamtof.core.context;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.lang.ScopedValue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 操作上下文 - 基于 Java 25 ScopedValue (正式版)
 * <p>
 * <b>核心设计：</b>
 * 1. 使用 ScopedValue 提供高性能线程上下文。
 * 2. <b>标识化设计：</b> 通过 isDefault 字段明确区分是"真实请求"还是"系统降级"。
 * </p>
 */
public class OperationContext {

    // 定义 ScopedValue 实例
    private static final ScopedValue<OperationInfo> CONTEXT = ScopedValue.newInstance();

    /**
     * 获取 ScopedValue 对象，用于切面绑定：
     * ScopedValue.where(OperationContext.getScopedValue(), info).run(() -> { ... });
     */
    public static ScopedValue<OperationInfo> getScopedValue() {
        return CONTEXT;
    }

    /**
     * 获取当前上下文信息
     * <p>
     * 逻辑：
     * 1. 如果已绑定：返回真实的业务上下文 (isDefault = false)。
     * 2. 如果未绑定：返回一个带标记的默认对象 (isDefault = true)。
     * </p>
     */
    public static OperationInfo get() {
        if (CONTEXT.isBound()) {
            return CONTEXT.get();
        }

        // 返回包含特殊标识的默认上下文，确保业务代码不会因空指针崩溃
        return OperationInfo.builder()
                .isDefault(true)
                .traceId(UuidCreator.getTimeOrderedEpoch())
                .deviceId("UNKNOWN-DEVICE") // 默认标识
                .startTime(System.currentTimeMillis())
                .userAgent("System")
                .businessModule("System")
                .businessAction("Background-Task")
                .clientIp("127.0.0.1")
                .build();
    }

    /**
     * 如果你更倾向于在某些场景下直接判空，可以使用此方法
     */
    public static OperationInfo getOrNull() {
        return CONTEXT.isBound() ? CONTEXT.get() : null;
    }

    // ========================================================================
    // 快捷访问器 (语法糖)
    // ========================================================================

    /**
     * 快速获取当前 TraceId
     */
    public static UUID traceId() {
        return get().getTraceId();
    }

    /**
     * 检查当前上下文是否是真实的业务请求 (由拦截器触发)
     */
    public static boolean isReal() {
        return CONTEXT.isBound() && !CONTEXT.get().isDefault();
    }

    /**
     * 检查当前线程是否已绑定上下文
     */
    public static boolean isBound() {
        return CONTEXT.isBound();
    }

    /**
     * 操作元数据模型
     */
    @Data
    @Builder
    public static class OperationInfo {

        /** * 特殊标识：是否为系统默认生成的上下文
         * true: 系统自动降级生成的 (如后台任务、未拦截的异步线程)
         * false: 真实的 Controller 拦截生成的
         */
        private boolean isDefault;

        // --- 追踪与时间 ---
        private UUID traceId;       // 永远不变
        private UUID spanId;        // 当前任务的 ID
        private UUID parentSpanId;  // 记录父任务的 ID
        private long startTime;

        // --- 核心身份：直接持有 Operator 对象 ---
        private Operator operator;

        // --- 业务语义 ---
        private String businessModule;
        private String businessAction;

        // --- 终端信息 ---
        private String clientIp;
        private String userAgent;
        private String deviceId;

        // --- 运行环境 ---
        private String methodName;
        private Object[] args;



        // --- 扩展字段 ---
        @Builder.Default
        private Map<String, Object> extensions = new HashMap<>();

        // --- 兼容性方法：为了方便获取 ID 和 Name ---
        // 修改为 Object 以兼容 Long/String
        public Serializable getOperatorId() { return operator != null ? operator.getId() : null; }

        public String getOperatorName() { return operator != null ? operator.getName() : "Guest"; }
    }
}
