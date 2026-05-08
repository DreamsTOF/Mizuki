package cn.dreamtof.core.context;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.core.task.TaskDecorator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 异步上下文传播工具（双模式版）
 * <p>
 * 支持两种模式：
 * <ul>
 *   <li><b>Spring Bean 自动注入</b>（默认）：自动注入所有 {@link ThreadContextCopier} Bean</li>
 *   <li><b>注册表模式</b>：通过配置 {@code dreamtof.context.registry.enabled=true} 启用，
 *       使用 {@link ContextRegistry} 显式注册</li>
 * </ul>
 * 两种模式互斥，启用注册表模式后 Spring Bean 自动注入将被忽略。
 * </p>
 */
public class ContextPropagator implements TaskDecorator {

    /**
     * 获取当前有效的 copier 列表
     */
    private static List<ThreadContextCopier> getActiveCopiers() {
        return ContextRegistry.getAllCopiers();
    }

    /**
     * [内部方法] 供 VirtualTaskManager 使用
     */
    static List<ThreadContextCopier> getActiveCopiersForInternal() {
        return getActiveCopiers();
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        OperationContext.OperationInfo parentInfo = OperationContext.get();
        OperationContext.OperationInfo childInfo = parentInfo;
        childInfo.setParentSpanId(parentInfo.getSpanId());
        childInfo.setSpanId(UuidCreator.getTimeOrderedEpoch());
        
        List<ThreadContextCopier> copiers = getActiveCopiers();
        List<Object> snapshots = new ArrayList<>(copiers.size());
        for (ThreadContextCopier copier : copiers) {
            snapshots.add(copier.capture());
        }
        return () -> {
            for (int i = 0; i < copiers.size(); i++) {
                copiers.get(i).restore(snapshots.get(i));
            }
            try {
                ScopedValue.where(OperationContext.getScopedValue(), childInfo).run(runnable);
            } finally {
                for (ThreadContextCopier copier : copiers) {
                    try {
                        copier.clear();
                    } catch (Exception ignored) {}
                }
            }
        };
    }

    public static Runnable wrap(Runnable runnable) {
        OperationContext.OperationInfo info = OperationContext.get();
        List<ThreadContextCopier> copiers = getActiveCopiers();
        List<Object> snapshots = new ArrayList<>(copiers.size());
        for (ThreadContextCopier copier : copiers) snapshots.add(copier.capture());

        return () -> {
            for (int i = 0; i < copiers.size(); i++) copiers.get(i).restore(snapshots.get(i));
            try {
                ScopedValue.where(OperationContext.getScopedValue(), info).run(runnable);
            } finally {
                for (ThreadContextCopier copier : copiers) copier.clear();
            }
        };
    }

    public static <T> Callable<T> wrap(Callable<T> callable) {
        OperationContext.OperationInfo info = OperationContext.get();
        List<ThreadContextCopier> copiers = getActiveCopiers();
        List<Object> snapshots = new ArrayList<>(copiers.size());
        for (ThreadContextCopier copier : copiers) snapshots.add(copier.capture());

        return () -> {
            for (int i = 0; i < copiers.size(); i++) copiers.get(i).restore(snapshots.get(i));
            try {
                return ScopedValue.where(OperationContext.getScopedValue(), info).call(callable::call);
            } finally {
                for (ThreadContextCopier copier : copiers) copier.clear();
            }
        };
    }
}
