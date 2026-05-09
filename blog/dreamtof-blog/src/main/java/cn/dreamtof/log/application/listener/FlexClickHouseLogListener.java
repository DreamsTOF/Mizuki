//package cn.dreamtof.log.application.listener;
//
//import cn.dreamtof.core.context.Operator;
//import cn.dreamtof.log.core.LogEvent;
//import cn.dreamtof.log.spi.LogListener;
//import cn.dreamtof.log.domain.model.AppLogs;
//import cn.dreamtof.log.infrastructure.persistence.mapper.AppLogsMapper;
//import org.slf4j.helpers.MessageFormatter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * 🚀 基于 MyBatis-Flex 的 ClickHouse 异步日志写入器
// * <p>
// * 架构特性：
// * 1. 缓冲消峰：基于 LinkedBlockingQueue 实现内存攒批，保护 ClickHouse 免受高频碎片化写入的冲击。
// * 2. 双重触发：当队列达到 batchSize 时立即触发写入；或者每隔 flushIntervalMs 定时写入（兜底）。
// * 3. 优雅停机：实现 AutoCloseable，在 Spring 容器销毁时排空队列，确保日志不丢失。
// * </p>
// */
//@Component
//public class FlexClickHouseLogListener implements LogListener, AutoCloseable {
//
//    // ==========================================
//    // ⚙️ 核心配置参数
//    // ==========================================
//    private final int batchSize = 2000;          // 触发攒批写入的阈值
//    private final int maxQueueCapacity = 50000;  // 内存队列最大积压量 (防止 OOM)
//    private final long flushIntervalMs = 3000;   // 定时刷盘兜底时间 (毫秒)
//
//    // ==========================================
//    // 🛠️ 组件与状态
//    // ==========================================
//    private final BlockingQueue<LogEvent> buffer;
//    private final AtomicBoolean running = new AtomicBoolean(true);
//    private final ScheduledExecutorService flusher;
//    private final AppLogsMapper appLogsMapper;
//
//    // 从 Spring Boot 环境自动获取当前微服务元数据
//    @Value("${spring.application.name:unknown-service}")
//    private String appName;
//
//    @Value("${spring.profiles.active:dev}")
//    private String env;
//
//    @Autowired
//    public FlexClickHouseLogListener(AppLogsMapper appLogsMapper) {
//        this.appLogsMapper = appLogsMapper;
//        this.buffer = new LinkedBlockingQueue<>(maxQueueCapacity);
//
//        // 启动专属的后台定时刷盘守护线程
//        this.flusher = Executors.newSingleThreadScheduledExecutor(r -> {
//            Thread t = new Thread(r, "CK-Flex-Flusher");
//            t.setDaemon(true);
//            return t;
//        });
//
//        // 注册定时任务 (每 3 秒检查一次并刷盘)
//        this.flusher.scheduleAtFixedRate(this::flush, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
//    }
//
//    /**
//     * 📥 接收 LogWriter 投递的日志事件 (非阻塞)
//     */
//    @Override
//    public void onLog(LogEvent event) {
//        if (!running.get()) return;
//
//        // 非阻塞入队
//        boolean success = buffer.offer(event);
//        if (!success) {
//            // 如果真的积压了 5 万条日志还没写进去，说明 ClickHouse 宕机了，果断丢弃保护 JVM
//            System.err.println("[ClickHouse] Buffer full, dropping log: " + event.getLoggerName());
//        }
//
//        // 如果容量达到批次阈值，立即异步触发刷盘
//        if (buffer.size() >= batchSize) {
//            CompletableFuture.runAsync(this::flush, flusher);
//        }
//    }
//
//    /**
//     * 🌊 核心攒批与写入逻辑 (加锁防止并发重复消费)
//     */
//    private synchronized void flush() {
//        if (buffer.isEmpty()) return;
//
//        // 1. 将队列中的数据迅速 Drain (抽干) 到本地 List 中，尽快释放锁
//        List<LogEvent> drainList = new ArrayList<>(batchSize);
//        buffer.drainTo(drainList, batchSize);
//
//        if (drainList.isEmpty()) return;
//
//        // 2. 将内部 LogEvent 转换为 MyBatis-Flex 实体
//        List<AppLogs> entities = new ArrayList<>(drainList.size());
//        for (LogEvent event : drainList) {
//            entities.add(convertToEntity(event));
//        }
//
//        // 3. 🚀 执行批量写入
//        try {
//            // 第二个参数是底层单次执行的 batchSize，1000 是一个性能极佳的甜点值
//            appLogsMapper.insertBatch(entities, 1000);
//        } catch (Exception e) {
//            // 这里可以接死信队列或者记录到本地灾备文件中
//            System.err.println("[ClickHouse] Flex batch insert failed! Lost " + entities.size() + " logs. Error: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 🔄 实体转换器：将物理快照映射为数据库行
//     */
//    private AppLogs convertToEntity(LogEvent event) {
//        // 解析原始消息模板 (如 "User {} login")
//        String formattedMsg = MessageFormatter.arrayFormat(event.getMessagePattern(), event.getArgs()).getMessage();
//
//        AppLogs.AppLogsBuilder builder = AppLogs.builder()
//                // 💡 时间戳转换：long 毫秒 -> ClickHouse DateTime64 映射的 LocalDateTime
//                .timestamp(LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), ZoneId.systemDefault()))
//                .appName(this.appName)
//                .env(this.env)
//                .level(event.getLevel().name())
//                .loggerName(event.getLoggerName())
//                .threadName(event.getThreadName())
//                .message(formattedMsg);
//
//        // Marker 路由标记
//        if (event.getMarker() != null) {
//            builder.marker(event.getMarker().getName());
//        }
//
//        // 👑 链路追踪与业务上下文 (UUIDv7 解析)
//        if (event.getContext() != null) {
//            // 如果你的 traceId 已经是 UUID 类型，直接 set 即可
//            builder.traceId(event.getContext().getTraceId())
//                   .spanId(event.getContext().getSpanId())
//                   .parentSpanId(event.getContext().getParentSpanId());
//
//            // 业务操作人身份提取
//            Operator op = event.getOperator();
//            if (op != null) {
//                builder.userId(op.getId() != null ? String.valueOf(op.getId()) : null)
//                       .tenantId(op.getTenantId() != null ? String.valueOf(op.getTenantId()) : null)
//                       .clientIp(op.getIp());
//                // 如果 Operator 里有 extraProps 扩展字段，兜底存入 extContext (JSON 格式)
////                 if (op.getExtraProps() != null && !op.getExtraProps().isEmpty()) {
////                    builder.extContext(JsonUtils.toJsonString(op.getExtraProps()));
////                 }
//            }
//        }
//
//        // 异常堆栈提取
//        if (event.getThrowable() != null) {
//            builder.stackTrace(getStackTrace(event.getThrowable()));
//        }
//
//        return builder.build();
//    }
//
//    /**
//     * 🛠️ 提取异常堆栈为字符串
//     */
//    private String getStackTrace(Throwable t) {
//        java.io.StringWriter sw = new java.io.StringWriter();
//        t.printStackTrace(new java.io.PrintWriter(sw));
//        return sw.toString();
//    }
//
//    /**
//     * 🛑 优雅停机钩子：Spring 容器销毁时调用
//     */
//    @Override
//    public void close() {
//        System.out.println("[ClickHouse] Shutting down log listener, draining remaining buffer...");
//        running.set(false);
//        flush(); // 停机前强行刷最后一次盘
//        flusher.shutdown();
//        try {
//            if (!flusher.awaitTermination(2, TimeUnit.SECONDS)) {
//                flusher.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            flusher.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//}