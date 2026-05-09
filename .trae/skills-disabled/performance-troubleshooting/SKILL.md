---
name: performance-troubleshooting
description: Dreamtof 项目性能问题诊断指南，涵盖查询慢、内存占用高、吞吐量低等性能问题的快速诊断与解决方案。当用户遇到接口响应慢、数据库CPU高、堆内存持续增长、GC频繁、TPS不达标、并发能力不足或需要进行压测优化时，必须使用此技能。包含性能红线（查询P99<100ms、写入>1000 TPS、缓存命中率>90%）、查询慢诊断（索引缺失、N+1查询、深分页、缓存未用）、内存占用高诊断（大对象、缓存无上限、集合增长、包装类型）、吞吐量低诊断（未批量处理、未用虚拟线程、同步阻塞、连接池小）以及完整的快速诊断流程图。
---

## 一、性能红线（必须达成）

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 查询延迟 P99 | < 100ms | 单表/简单关联查询 |
| 复杂查询 P99 | < 500ms | 3表以上关联查询 |
| 写入吞吐 | > 1000 TPS | 单业务聚合写入 |
| 缓存命中率 | > 90% | 热点数据缓存 |
| 响应时间 P95 | < 200ms | API接口整体 |

**监控工具**：Micrometer + Prometheus、Arthas、JMeter 压测

## 二、问题诊断流程

### 查询慢（P99 > 100ms）

**诊断流程**：
```
问题：接口响应慢（> 2s）
├─ 是否使用 FlexSmartQuery？ → 否 → 替换（避免 Mapper 直连）
├─ 是否缺少索引？ → 是 → 添加复合索引（EXPLAIN 分析）
├─ 是否 N+1 查询？ → 是 → 改为批量查询（一次 IN 查询）
├─ 是否深分页？ → 是 → 改用游标分页（CursorReq）
├─ 是否使用缓存？ → 否 → 添加 Caffeine/Redis 缓存
└─ 是否批量处理？ → 否 → 使用 FlexUltraInserter（80倍提升）
```

### 内存占用高（堆内存持续增长）

**诊断流程**：
```
问题：堆内存持续增长，GC频繁
├─ 是否创建大对象？ → 是 → 分片或流式处理
├─ 缓存是否无上限？ → 是 → 设置 maximumSize + LRU
├─ 集合是否持续增长？ → 是 → 使用有界队列或清理策略
└─ 是否使用包装类型？ → 是 → 改用基本类型（int/long）
```

### 吞吐量低（TPS < 1000）

**诊断流程**：
```
问题：吞吐量低，并发能力不足
├─ 是否使用批量插入？ → 否 → 使用 FlexUltraInserter
├─ 是否使用虚拟线程？ → 否 → 使用 VirtualTaskManager（万级并发）
├─ 是否同步阻塞？ → 是 → 改为异步非阻塞（CompletableFuture）
└─ 连接池是否过小？ → 是 → 增加 HikariCP 连接数（50-100）

**现象**：堆内存持续增长，GC频繁，可能OOM

**诊断流程**：

```text
问题：堆内存持续增长
├─ 检查1：是否创建了大对象？
│   → 是 → 分片或流式处理
│   ├─ 大文件（>1MB）→ 流式读写
│   ├─ 大集合（>10000条）→ 分批处理
│   └─ 大报告 → 分片生成或流式输出
│
├─ 检查2：缓存是否无上限？
│   → 是 → 设置 maximumSize
│   ├─ Caffeine 缓存 → 设置 maximumSize
│   ├─ Redis 缓存 → 设置 maxmemory
│   └─ 添加 LRU 淘汰策略
│
├─ 检查3：集合是否持续增长？
│   → 是 → 使用有界队列或清理策略
│   ├─ 静态集合 → 改为有界队列
│   ├─ 缓存 → 添加过期时间
│   └─ 定期清理 → 添加定时任务
│
└─ 检查4：是否使用包装类型？
    → 是 → 改用基本类型
    ├─ Integer → int
    ├─ Long → long
    └─ 使用 fastutil 等高性能集合
```

**解决方案示例**：

```java
// ❌ 错误1：缓存无上限
private static final Map<String, Object> CACHE = new HashMap<>();
// 无限增长，最终 OOM

// ✅ 解决：有界缓存
private static final Cache<String, Object> CACHE =
    Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

// ❌ 错误2：未关闭流
public void export() throws IOException {
    InputStream is = file.getInputStream();
    // 未关闭流，内存泄漏
}

// ✅ 解决：try-with-resources
public void export() throws IOException {
    try (InputStream is = file.getInputStream()) {
        // 自动关闭
    }
}

// ❌ 错误3：大对象在内存中构建
public byte[] generateReport() {
    // 可能在内存中构建大报告（>100MB）
    return reportBuilder.build();
}

// ✅ 解决：流式处理
public void generateReport(OutputStream out) {
    // 流式写入，避免内存中构建大对象
    reportBuilder.streamTo(out);
}
```

### 4. 吞吐量低诊断

**现象**：TPS不达标；并发能力不足

**诊断流程**：

```text
问题：吞吐量低（< 1000 TPS）
├─ 检查1：是否使用批量插入？
│   → 否 → 使用 FlexUltraInserter
│   ├─ 单条插入：10000条 ~12000ms
│   └─ 批量插入：10000条 ~150ms（80倍提升）
│
├─ 检查2：是否使用虚拟线程？
│   → 否 → 使用 VirtualTaskManager
│   ├─ 平台线程：1000并发，内存1MB/线程
│   └─ 虚拟线程：10000+并发，内存几十KB/线程
│
├─ 检查3：是否同步阻塞？
│   → 是 → 改为异步非阻塞
│   ├─ 同步I/O → 异步I/O
│   └─ 阻塞调用 → CompletableFuture 异步
│
└─ 检查4：是否数据库连接池过小？
    → 是 → 增加连接池大小
    ├─ HikariCP 默认 10 个连接
    └─ 高并发场景调整至 50-100 个连接
```

**解决方案示例**：

```java
// ✅ 使用批量插入
@Service
@RequiredArgsConstructor
public class SongImportService {

    private final FlexUltraInserter ultraInserter;

    public void batchImport(List<SongPO> songs) {
        FlexUltraInserter.execute(songs, (item, throwable) -> {
            if (throwable != null) {
                log.error("导入失败: {}", item, throwable);
            }
        });
    }
}

// ✅ 使用虚拟线程
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

    private final VirtualTaskManager taskManager;

    public void processBatch(List<Long> ids) {
        // 使用虚拟线程池（万级并发）
        taskManager.runAsync(() -> {
            // 业务逻辑
        });

        // 批量任务并行处理
        ids.forEach(id -> {
            taskManager.runAsync(() -> processItem(id));
        });
    }

    public CompletableFuture<Long> asyncProcess(Long id) {
        return taskManager.supplyAsync(() -> {
            // 异步计算
            return processItem(id);
        });
    }
}
```

---

## 快速诊断流程图

```text
性能问题 → 查看监控指标 → 定位问题类型
    ↓
    ├─ 查询慢（P99 > 100ms）
    │   ├─ 是否缺少索引？ → 添加复合索引
    │   ├─ 是否 N+1 查询？ → 改为批量查询
    │   ├─ 是否深分页？ → 改用游标分页
    │   └─ 是否未使用缓存？ → 添加 Caffeine 缓存
    │
    ├─ 内存占用高（堆内存持续增长）
    │   ├─ 是否创建了大对象？ → 分片或流式处理
    │   ├─ 缓存是否无上限？ → 设置 maximumSize
    │   ├─ 集合是否持续增长？ → 使用有界队列
    │   └─ 是否使用包装类型？ → 改用基本类型
    │
    └─ 吞吐量低（TPS < 1000）
        ├─ 是否使用批量插入？ → 使用 FlexUltraInserter
        ├─ 是否使用虚拟线程？ → 使用 VirtualTaskManager
        ├─ 是否同步阻塞？ → 改为异步非阻塞
        └─ 是否连接池过小？ → 增加连接池大小
```

---

## 相关技能

- **性能优化**：详细性能优化技术规范（零反射、批量处理、游标分页、缓存、查询优化、虚拟线程、内存优化）
- **查询规范**：FlexSmartQuery 优化查询
- **架构总览**：架构级性能考虑

---

**版本**：1.0.0
**更新日期**：2026-04-17
**适用场景**：性能调优、压测优化、问题诊断
