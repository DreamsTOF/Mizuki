package cn.dreamtof.audit.core;

import cn.dreamtof.audit.utils.IdExtractor;
import cn.dreamtof.core.utils.FastReflectUtils;
import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.Serializable;

import static cn.dreamtof.log.core.LogMarkers.AUDIT;
import static com.mybatisflex.core.query.QueryMethods.column;

/**
 * 👑 全局审计数据库监听器 (极速版)
 * <p>
 * 作用：拦截 MyBatis-Flex 的生命周期钩子。
 * 优化点：在获取数据库元数据时改用 FastReflectUtils，降低元数据解析开销。
 */
@Slf4j
@Component
public class GlobalAuditListener implements InsertListener, UpdateListener {

    /**
     * 新增事件处理：直接存入 newState
     */
    @Override
    public void onInsert(Object entity) {
        AuditTransactionContext.ContextPayload payload = AuditTransactionContext.get();
        if (payload == null || !payload.getAllowedEntities().contains(entity.getClass())) return;

        AuditTransactionContext.addRecord(new AuditTransactionContext.Record(
                entity.getClass(), null, null, entity, AuditTransactionContext.OperationType.INSERT
        ));
    }

    /**
     * 更新事件处理：自动抓取 OldState
     */
    /**
     * 完整修复：数据库更新监听器
     * 增加：立刻执行深拷贝，防止缓存引用污染
     */
    @Override
    public void onUpdate(Object entity) {
        AuditTransactionContext.ContextPayload payload = AuditTransactionContext.get();
        Class<?> realClass = ClassUtils.getUserClass(entity.getClass());
        if (payload == null || !payload.getAllowedEntities().contains(realClass)) return;

        try {
            TableInfo tableInfo = TableInfoFactory.ofEntityClass(realClass);
            if (tableInfo == null || tableInfo.getPrimaryKeyList().isEmpty()) return;

            Serializable id = IdExtractor.extractId(entity);
            // 如果当前 ID 在此事务上下文中已经有了 Snapshot，说明是重复触发，直接跳过
            if (id == null || AuditTransactionContext.hasSnapshot(realClass, id)) return;

            // 1. 尝试从切面预抓取的缓存中获取
            String cacheKey = realClass.getName() + ":" + id;
            Object oldState = payload.getPrefetchedOldStates().get(cacheKey);

            if (oldState == null) {
                // 2. 熔断保护：防止单次事务触发过多 SQL 查询
                if (payload.getListenerQueryCount().incrementAndGet() > 50) {
                    log.warn(AUDIT, "[自动审计] 侦测到查询雪崩！已自动降级为简略模式。实体: {}", realClass.getSimpleName());
                    payload.setPartial(true);
                    return;
                }

                // 3. 执行数据库查询获取原始快照
                Object pkNode = tableInfo.getPrimaryKeyList().getFirst();
                String colName = (String) FastReflectUtils.getPropertyValue(pkNode, "column");
                String tableName = tableInfo.getTableName();
                String targetDs = tableInfo.getDataSource();
                String previousDs = DataSourceKey.get();

                try {
                    if (targetDs != null) DataSourceKey.use(targetDs);
                    Row row = Db.selectOneByQuery(tableName, QueryWrapper.create().where(column(colName).eq(id)));
                    // 【核心修复】：查出来的对象必须立刻 Clone，彻底切断与 MyBatis Session 的联系
                    oldState = (row != null) ? FastReflectUtils.deepCopy(row.toEntity(realClass)) : null;
                } finally {
                    DataSourceKey.use(previousDs != null ? previousDs : "");
                }
            }

            if (oldState != null) {
                AuditTransactionContext.addRecord(new AuditTransactionContext.Record(
                        realClass, id, oldState, null, AuditTransactionContext.OperationType.UPDATE
                ));
            }
        } catch (Exception e) {
            log.warn(AUDIT, "[自动审计] 监听器抓取旧数据失败: {}, 原因={}", realClass.getSimpleName(), e.getMessage());
        }
    }
}