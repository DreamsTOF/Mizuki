package cn.dreamtof.audit.utils;

import cn.dreamtof.audit.core.AuditTransactionContext;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 全局统一事务调度器
 * 作用：替代原生的 TransactionTemplate，无缝植入审计的“保存点”回滚机制。
 */
@Component
public class SmartTransactionTemplate extends TransactionTemplate {


    public SmartTransactionTemplate(PlatformTransactionManager transactionManager) {
        super(transactionManager);
    }

    @Override
    public <T> T execute(@NonNull TransactionCallback<T> action)  {
        // 1. 获取当前审计事务上下文载荷
        AuditTransactionContext.ContextPayload payload = AuditTransactionContext.get();

        // 2. 如果存在审计上下文，记录当前快照位置（Savepoint）
        int savepoint = (payload != null) ? payload.createSavepoint() : 0;

        try {
            // 调用父类原生 execute 方法开启事务
            return super.execute(status -> {
                T result = action.doInTransaction(status);

                // 3. 检查事务状态。如果业务代码显式调用了 setRollbackOnly()
                // 且审计上下文存在，则需要将审计记录回滚到保存点
                if (status.isRollbackOnly() && payload != null) {
                    payload.rollbackToSavepoint(savepoint); //
                }
                return result;
            });
        } catch (Throwable e) {
            // 4. 捕获异常：如果事务因异常导致回滚
            // 同步剥离保存点之后的审计记录，防止“脏审计数据”残留
            if (payload != null) {
                payload.rollbackToSavepoint(savepoint); //
            }
            // 继续抛出异常，交给 Spring 事务管理器处理回滚逻辑
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        }
    }
}