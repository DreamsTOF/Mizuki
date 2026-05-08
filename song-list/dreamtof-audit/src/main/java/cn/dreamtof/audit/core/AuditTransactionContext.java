package cn.dreamtof.audit.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.lang.ScopedValue;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static cn.dreamtof.log.core.LogMarkers.AUDIT;
import static java.util.Collections.emptySet;

/**
 * 审计事务上下文 (Java 25 ScopedValue 版)
 */
@Slf4j
public class AuditTransactionContext {

    // 【核心升级】：使用 ScopedValue 替代 ThreadLocal，天生防内存泄漏，完美适配虚拟线程
    public static final ScopedValue<ContextPayload> CONTEXT = ScopedValue.newInstance();

    @Data
    public static class ContextPayload {
        private final String module;
        private final String action;
        private boolean partial; // 简略模式标识
        private final boolean skipNull;
        private final boolean systemFields;
        private final boolean refresh;
        private final Set<Class<?>> allowedEntities;
        private final List<Record> records = new ArrayList<>();
        private final Map<String, Boolean> snapshotCache = new ConcurrentHashMap<>();

        // 用于存放切面预抓取的旧状态，防止 Listener 重复查库（防 N+1）
        private final Map<String, Object> prefetchedOldStates = new ConcurrentHashMap<>();
        private final AtomicInteger listenerQueryCount = new AtomicInteger(0);

        public ContextPayload(String module, String action, boolean partial, boolean skipNull, boolean systemFields, boolean refresh, Set<Class<?>> allowedEntities) {
            this.module = module;
            this.action = action;
            this.partial = partial;
            this.skipNull = skipNull;
            this.systemFields = systemFields;
            this.refresh = refresh;
            this.allowedEntities = allowedEntities != null ? allowedEntities : emptySet();
        }

        /** 创建保存点：记录当前记录数 */
        public int createSavepoint() {
            return records.size();
        }

        /** 事务回滚：剥离保存点之后的审计记录 */
        public void rollbackToSavepoint(int savepoint) {
            if (savepoint < records.size()) {
                List<Record> discarded = new ArrayList<>(records.subList(savepoint, records.size()));
                records.subList(savepoint, records.size()).clear();
                // 释放缓存占用，允许重试抓取
                for (Record r : discarded) {
                    if (r.getId() != null) {
                        snapshotCache.remove(r.getClazz().getName() + ":" + r.getId());
                    }
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class Record {
        private final Class<?> clazz;
        private final Serializable id;
        private final Object oldState;
        private Object newState;
        private OperationType type;
    }

    public enum OperationType { INSERT, UPDATE, DELETE }

    public static ContextPayload get() {
        return CONTEXT.isBound() ? CONTEXT.get() : null;
    }

    /**
     * 完整修复：添加审计记录
     * 增加：针对大事务的内存过载保护
     */
    public static void addRecord(Record record) {
        ContextPayload payload = get();
        if (payload == null) return;

        // 1. 溢出检查：如果单次请求修改的数据量超过阈值（如 1000 条），不再记录明细
        // 防止 ContextPayload 过大导致 JVM 堆内存直接 OOM
        if (payload.getRecords().size() >= 1000) {
            if (payload.getRecords().size() == 1000) {
                log.warn("[自动审计] 事务变更记录数超过上限(1000)，后续变更将不再记录明细以防止内存溢出。");
            }
            return;
        }

        // 2. 自动升阶：如果变更数量较多，自动降级为“简略模式”减少内存占用
        if (payload.getRecords().size() > 200) {
            payload.setPartial(true);
        }

        payload.getRecords().add(record);
    }

    public static boolean hasSnapshot(Class<?> clazz, Serializable id) {
        ContextPayload payload = get();
        if (payload == null) return false;
        String key = clazz.getName() + ":" + id;
        return payload.getSnapshotCache().putIfAbsent(key, true) != null;
    }

    /**
     * 手动推入已存在的旧状态实体
     */
    public static void pushOldStates(List<?> oldEntities) {
        ContextPayload payload = get();
        if (payload == null || oldEntities == null || oldEntities.isEmpty()) return;

        for (Object entity : oldEntities) {
            Class<?> clazz = entity.getClass();
            try {
                com.mybatisflex.core.table.TableInfo info = com.mybatisflex.core.table.TableInfoFactory.ofEntityClass(clazz);
                if (info == null || info.getPrimaryKeyList().isEmpty()) continue;

                String pkProp = info.getPrimaryKeyList().getFirst().getProperty();
                java.lang.reflect.Field pkField = cn.dreamtof.core.utils.FastReflectUtils.getField(clazz, pkProp);
                if (pkField == null) continue;

                Object idObj = pkField.get(entity);
                if (idObj instanceof Serializable id) {
                    if (!hasSnapshot(clazz, id)) {
                        addRecord(new Record(clazz, id, entity, null, OperationType.UPDATE));
                    }
                }
            } catch (Exception e) {
                log.warn(AUDIT,"[自动审计] 手动推入实体旧状态失败: 实体={}, 原因={}", clazz.getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * 根据 MyBatis-Flex 查询条件预抓取旧状态
     */
    public static void prefetchByQuery(Class<?> clazz, com.mybatisflex.core.query.QueryWrapper queryWrapper) {
        if (get() == null) return;
        try {
            List<?> oldEntities = com.mybatisflex.core.mybatis.Mappers.ofEntityClass(clazz).selectListByQuery(queryWrapper);
            pushOldStates(oldEntities);
        } catch (Exception e) {
            log.warn(AUDIT,"[自动审计] 执行 QueryWrapper 预抓取旧状态失败: 实体={}, 原因={}", clazz.getSimpleName(), e.getMessage());
        }
    }
}