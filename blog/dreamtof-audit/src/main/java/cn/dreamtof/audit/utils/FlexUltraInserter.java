package cn.dreamtof.audit.utils;

import cn.dreamtof.core.config.VirtualTaskManager;
import cn.dreamtof.core.utils.FastBeanMeta;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.row.RowMapper;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * 🚀 FlexUltraInserter - 虚拟线程增强版
 * 集成了动态进度监控、异步/同步双模式、以及自适应降级重试机制。
 */
@Slf4j
@Component
public class FlexUltraInserter implements InitializingBean {

    private static final int SAFE_BATCH_SIZE = 2000;
    private static final int DEFAULT_FACTOR = 10;

    private static FlexUltraInserter instance;
    private final SmartTransactionTemplate transactionHelper;

    // 任务仪表盘：存储所有活跃任务的状态
    private final Map<String, TaskStatus> dashboard = new ConcurrentHashMap<>();

    public FlexUltraInserter(SmartTransactionTemplate transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

    @Override
    public void afterPropertiesSet() {
        instance = this;
    }

    // ================= [ 入口 1: 同步调用 ] =================

    /**
     * 同步执行：调用异步逻辑并阻塞等待直到完成
     */
    public static <T> void execute(Collection<T> data, BiConsumer<T, Throwable> errorCallback) {
        if (data == null || data.isEmpty()) return;

        String taskId = executeAsync(data, errorCallback);
        TaskStatus status = instance.dashboard.get(taskId);

        // 阻塞等待：轮询检查状态是否为终态
        try {
            while (status != null && "RUNNING".equals(status.getPhase())) {
                Thread.sleep(100); // 虚拟线程环境下，这里的 sleep 很轻量
            }
        } catch (InterruptedException e) {
            log.error("同步等待任务 {} 时被中断", taskId);
            Thread.currentThread().interrupt();
        } finally {
            // 同步结束后清理内存
            if (taskId != null) instance.dashboard.remove(taskId);
        }
    }

    // ================= [ 入口 2: 异步调用 ] =================

    /**
     * 异步执行：立即返回任务 ID，任务由 VirtualTaskManager 处理
     */
    public static <T> String executeAsync(Collection<T> data, BiConsumer<T, Throwable> errorCallback) {
        if (data == null || data.isEmpty()) return null;

        String taskId = "V-TASK-" + UUID.randomUUID().toString().substring(0, 8);
        TaskStatus status = new TaskStatus(data.size());
        instance.dashboard.put(taskId, status);

        // 使用你提供的虚拟线程管理器提交任务
        VirtualTaskManager.execute(() -> {
            try {
                instance.doProcess(taskId, data, errorCallback);
                status.setPhase("SUCCESS");
                log.info("任务 {} 执行成功", taskId);
            } catch (Throwable t) {
                status.setPhase("FAILED");
                log.error("任务 {} 执行异常崩溃: ", taskId, t);
            }
        });

        return taskId;
    }

    /**
     * 获取任务状态 (用于前端轮询)
     */
    public static TaskStatus getStatus(String taskId) {
        return instance.dashboard.get(taskId);
    }

    // ================= [ 核心处理逻辑 ] =================

    private <T> void doProcess(String taskId, Collection<T> data, BiConsumer<T, Throwable> errorCallback) {
        TaskStatus status = dashboard.get(taskId);
        status.setPhase("RUNNING");

        long startTime = System.currentTimeMillis();
        List<T> allData = new ArrayList<>(data);
        Class<?> clazz = allData.get(0).getClass();
        TableInfo tableInfo = TableInfoFactory.ofEntityClass(clazz);

        try {
            for (int i = 0; i < allData.size(); i += SAFE_BATCH_SIZE) {
                int end = Math.min(i + SAFE_BATCH_SIZE, allData.size());
                List<T> subList = allData.subList(i, end);

                // 执行自适应注入
                adaptiveInject(status, subList, DEFAULT_FACTOR, tableInfo, errorCallback);

                // 更新心跳时间，证明任务没“死”
                status.setLastActiveTime(System.currentTimeMillis());

                // 打印阶段性日志
                log.info("任务 [{}] 进度: {}/{} (成功: {}, 失败: {})",
                        taskId, (i + subList.size()), allData.size(),
                        status.getSuccessCount().get(), status.getFailureCount().get());
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            status.setCompletedTime(System.currentTimeMillis());

            log.info("""
    ==================== 批量插入报告 [%s] ====================
    TaskID: %s
    ⏱️ 总耗时: %d ms
    ✅ 成功: %d 条 | ❌ 失败: %d 条
    🏎️ 最终性能: %.4f ms/条
    =========================================================""".formatted(
                            clazz.getSimpleName(),
                            taskId,
                            duration,
                            status.getSuccessCount().get(),
                            status.getFailureCount().get(),
                            (double) duration / allData.size()
                    )
            );
        }
    }

    private <T> void adaptiveInject(TaskStatus status, List<T> subList, int factor,
                                    TableInfo tableInfo, BiConsumer<T, Throwable> errorCallback) {
        if (subList.isEmpty()) return;

        // 如果只剩一条，直接执行单条插入
        if (subList.size() == 1) {
            processSingle(status, subList.get(0), tableInfo, errorCallback);
            return;
        }

        try {
            String schema = tableInfo.getSchema();
            String tableName = tableInfo.getTableName();

            transactionHelper.execute(ts -> {
                return Db.executeBatch(subList, subList.size(), RowMapper.class, (mapper, entity) -> {
                    Row row = convertEntityToRow(entity, tableInfo);
                    mapper.insert(schema, tableName, row);
                });
            });
            // 批量成功更新计数
            status.getSuccessCount().addAndGet(subList.size());
        } catch (Throwable t) {
            // 异常降级：缩小批次大小递归重试
            int nextSize = Math.max(1, subList.size() / factor);
            log.warn("⚠️ 任务进度中批次异常(Size:{})，降级为 Size:{} 重试...", subList.size(), nextSize);

            for (int i = 0; i < subList.size(); i += nextSize) {
                int end = Math.min(i + nextSize, subList.size());
                adaptiveInject(status, subList.subList(i, end), factor, tableInfo, errorCallback);
            }
        }
    }

    private <T> void processSingle(TaskStatus status, T entity, TableInfo tableInfo,
                                   BiConsumer<T, Throwable> errorCallback) {
        try {
            String schema = tableInfo.getSchema();
            String tableName = tableInfo.getTableName();

            transactionHelper.execute(ts -> {
                Row row = convertEntityToRow(entity, tableInfo);
                return Db.insert(schema, tableName, row);
            });
            status.getSuccessCount().incrementAndGet();
        } catch (Throwable t) {
            status.getFailureCount().incrementAndGet();
            if (errorCallback != null) errorCallback.accept(entity, t);
        }
    }

    /**
     * 高性能转换：Entity -> Row
     */
    private Row convertEntityToRow(Object entity, TableInfo tableInfo) {
        Row row = new Row();
        FastBeanMeta meta = FastBeanMeta.of(entity.getClass());

        tableInfo.getPropertyColumnMapping().forEach((property, column) -> {
            var accessor = meta.getAccessor(property);
            if (accessor != null && accessor.getter() != null) {
                Object value = accessor.getter().apply(entity);
                if (value != null) {
                    row.put(column, value);
                }
            }
        });
        return row;
    }

    // ================= [ 状态模型 ] =================

    /**
     * 任务动态状态类
     */
    @Data
    public static class TaskStatus {
        private final int totalCount;           // 总数据量
        private AtomicInteger successCount = new AtomicInteger(0);
        private AtomicInteger failureCount = new AtomicInteger(0);
        private String phase = "PENDING";       // PENDING, RUNNING, SUCCESS, FAILED

        private long startTime = System.currentTimeMillis();
        private long lastActiveTime = System.currentTimeMillis(); // 心跳时间
        private Long completedTime;

        /**
         * 获取当前百分比进度 (0-100)
         */
        public double getProgress() {
            if (totalCount == 0) return 100;
            return (double) (successCount.get() + failureCount.get()) / totalCount * 100;
        }

        /**
         * 判定是否长时间无响应 (卡死判定)
         */
        public boolean isStuck(long timeoutMs) {
            return "RUNNING".equals(phase) &&
                    (System.currentTimeMillis() - lastActiveTime > timeoutMs);
        }
    }
}