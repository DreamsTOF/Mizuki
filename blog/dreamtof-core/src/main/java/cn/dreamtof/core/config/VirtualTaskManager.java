package cn.dreamtof.core.config;

import cn.dreamtof.core.context.ContextRegistry;
import cn.dreamtof.core.context.OperationContext;
import cn.dreamtof.core.context.ThreadContextCopier;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VirtualTaskManager implements InitializingBean, DisposableBean {

    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(
            Thread.ofPlatform().name("vt-scheduler-").factory()
    );

    public static final Semaphore L10 = new Semaphore(10);
    public static final Semaphore L50 = new Semaphore(50);
    public static final Semaphore L100 = new Semaphore(100);
    public static final Semaphore L500 = new Semaphore(500);

    @Override
    public void afterPropertiesSet() {
        if (ContextRegistry.isRegistryModeEnabled()) {
            log.info("VirtualTaskManager: 注册表模式已启用");
        } else {
            log.info("VirtualTaskManager: Spring Bean 自动注入模式启用");
        }
    }

    @Override
    public void destroy() {
        log.info("VirtualTaskManager: 正在关闭虚拟线程调度资源...");
        EXECUTOR.shutdown();
        SCHEDULER.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void execute(Runnable task) {
        EXECUTOR.execute(decorate(task));
    }

    public static void executeWithLimit(Runnable task, Semaphore semaphore) {
        execute(() -> {
            try {
                semaphore.acquire();
                task.run();
            } catch (InterruptedException e) {
                log.warn("任务获取信号量许可时被中断");
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        });
    }

    public static void schedule(Runnable task, long delay, TimeUnit unit) {
        SCHEDULER.schedule(() -> execute(task), delay, unit);
    }

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(decorate(supplier), EXECUTOR);
    }

    public static <T> List<T> invokeAll(Collection<Supplier<T>> tasks) {
        if (tasks == null || tasks.isEmpty()) return Collections.emptyList();

        List<CompletableFuture<T>> futures = tasks.stream()
                .map(VirtualTaskManager::supply)
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public static <T> CompletableFuture<T> supplyWithRecovery(Supplier<T> supplier, Function<Throwable, T> fallback) {
        return supply(supplier).exceptionally(ex -> {
            log.error("虚拟线程任务执行异常，执行 Fallback 恢复逻辑: {}", ex.getMessage());
            return fallback.apply(ex);
        });
    }

    public static <T> void parallelForEach(Collection<T> data, Consumer<T> action) {
        if (data == null || data.isEmpty()) return;
        List<CompletableFuture<Void>> futures = data.stream()
                .map(item -> CompletableFuture.runAsync(decorate(() -> action.accept(item)), EXECUTOR))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private static Runnable decorate(Runnable task) {
        Map<ThreadContextCopier, Object> snapshots = captureSnapshots();
        List<ThreadContextCopier> copiers = ContextRegistry.getAllCopiers();

        return () -> {
            try {
                snapshots.forEach(ThreadContextCopier::restore);
                task.run();
            } catch (Throwable t) {
                log.error("Virtual Task execution error: ", t);
                throw t;
            } finally {
                copiers.forEach(ThreadContextCopier::clear);
            }
        };
    }

    private static <T> Supplier<T> decorate(Supplier<T> supplier) {
        OperationContext.OperationInfo parentInfo = OperationContext.get();
        OperationContext.OperationInfo childInfo = OperationContext.OperationInfo.builder()
                .isDefault(parentInfo.isDefault())
                .traceId(parentInfo.getTraceId())
                .spanId(UuidCreator.getTimeOrderedEpoch())
                .parentSpanId(parentInfo.getSpanId())
                .operator(parentInfo.getOperator())
                .businessModule(parentInfo.getBusinessModule())
                .businessAction(parentInfo.getBusinessAction())
                .clientIp(parentInfo.getClientIp())
                .userAgent(parentInfo.getUserAgent())
                .deviceId(parentInfo.getDeviceId())
                .extensions(new HashMap<>(parentInfo.getExtensions()))
                .build();

        Map<ThreadContextCopier, Object> snapshots = captureSnapshots();
        List<ThreadContextCopier> copiers = ContextRegistry.getAllCopiers();

        return () -> {
            try {
                snapshots.forEach(ThreadContextCopier::restore);
                return ScopedValue.where(OperationContext.getScopedValue(), childInfo).call(supplier::get);
            } finally {
                copiers.forEach(ThreadContextCopier::clear);
            }
        };
    }

    private static Map<ThreadContextCopier, Object> captureSnapshots() {
        List<ThreadContextCopier> copiers = ContextRegistry.getAllCopiers();
        if (copiers.isEmpty()) return Collections.emptyMap();
        Map<ThreadContextCopier, Object> snapshots = new HashMap<>(copiers.size());
        for (ThreadContextCopier copier : copiers) {
            Object snapshot = copier.capture();
            if (snapshot != null) {
                snapshots.put(copier, snapshot);
            }
        }
        return snapshots;
    }
}
