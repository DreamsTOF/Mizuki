package cn.dreamtof.audit.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 自动审计全局配置 (高并发工业级版)
 * 对应配置文件前缀: dreamtof.auto-audit
 */
@Data
@Component
@ConfigurationProperties(prefix = "dreamtof.auto-audit")
@ConditionalOnProperty(prefix = "dreamtof.auto-audit", name = "enabled", havingValue = "true")
public class AutoAuditProperties {

    /**
     * 全局总开关
     * false: 彻底关闭切面，不执行任何审计逻辑 (默认 false)
     */
    private boolean enabled = false;

    /**
     * 【隔离】审计日志专属数据源名称 (用于多数据源环境，保护主业务连接池)
     * 若为空，则使用当前上下文的默认数据源
     */
    private String dataSource = "";

    // ========================================================
    // 1. 业务行为默认配置 (@AuditLog 兜底参数)
    // ========================================================

    /** 是否默认开启简略模式 (新增/删除只记录摘要) */
    private boolean defaultPartial = false;
    /** 是否默认跳过空字段的对比 */
    private boolean defaultSkipNull = true;
    /** 是否默认记录系统字段 (createTime, updateTime等) */
    private boolean defaultSystemFields = false;
    /** 是否默认开启同步回填最新快照 (防触发器、防幻读) */
    private boolean defaultRefresh = true;

    // ========================================================
    // 2. 内存防暴跌阈值配置 (Threshold)
    // ========================================================
    private ThresholdProperties threshold = new ThresholdProperties();

    @Data
    public static class ThresholdProperties {
        /**
         * 自动降级阈值：单次事务修改实体超过此数量，自动触发 partial=true 简略模式
         * (防大批量修改耗尽 CPU)
         */
        private int partialLimit = 200;

        /**
         * 绝对截断阈值：单次事务修改超过此数量，直接抛弃后续记录
         * (防极端大事务引发 ContextPayload 导致 JVM 堆内存 OOM)
         */
        private int overflowLimit = 1000;
        
        /**
         * 监听器防超载阈值：同一事务中通过 Listener 触发的单条兜底查询次数上限
         * (防隐藏 N+1 瞬间击穿数据库)
         */
        private int listenerQueryLimit = 50;
    }

    // ========================================================
    // 3. 异步并发与限流配置 (Async)
    // ========================================================
    private AsyncProperties async = new AsyncProperties();

    @Data
    public static class AsyncProperties {
        /**
         * 异步任务缓冲队列最大容量
         * 队列满时将触发拒绝策略（记录警告并丢弃），防止积压 OOM
         */
        private int queueCapacity = 5000;

        /**
         * 虚拟线程计算并发度 (Semaphore 许可数)
         * 控制同时执行 FastBeanMeta 对比的虚拟线程数量，建议设为 CPU 核心数 * 2
         */
        private int computeConcurrency = 16;
    }

    // ========================================================
    // 4. 批量落库性能配置 (Batch Writer)
    // ========================================================
    private BatchProperties batch = new BatchProperties();

    @Data
    public static class BatchProperties {
        /**
         * 批量写入阈值 (条)：内存缓冲池积攒到多少条触发一次 DB 批量 Insert
         */
        private int flushSize = 500;

        /**
         * 批量写入最大延迟 (毫秒)：即使没达到 flushSize，超过此时间也会强制刷盘
         */
        private long flushIntervalMs = 1000L;
    }
}