package cn.dreamtof.log.application.service;


import cn.dreamtof.log.domain.model.AppLogs;
import cn.dreamtof.log.domain.repository.AppLogsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.mybatisflex.core.paginate.Page;
import java.util.List;
import java.util.UUID;

/**
 *  应用服务
 * <p>
 * 职责：
 * 1. 编排业务流程（事务控制、权限校验、通知发送）。
 * 2. 指挥 Repository 进行数据持久化。
 * 3. 不包含核心领域规则（核心规则请放在 Entity 中）。
 * </p>
 *
 * @author dream
 * @since 
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppLogsAppService {

    private final AppLogsRepository repository;
    private final TransactionTemplate transactionTemplate;

    /**
     * 创建
     * (可以在这里加入 create 相关的各种校验和初始化逻辑)
     */
    public boolean create(AppLogs entity) {
        return transactionTemplate.execute(status -> {
            // TODO: 可以在这里调用 entity.init() 或其他领域行为
            return repository.save(entity);
        });
    }

    /**
     * 更新
     */
    public boolean update(AppLogs entity) {
        return transactionTemplate.execute(status -> {
            return repository.updateById(entity);
        });
    }

    /**
     * 删除
     */
    public boolean delete(UUID id) {
        return transactionTemplate.execute(status -> {
            return repository.removeById(id);
        });
    }

    /**
     * 批量删除
     */
    public boolean deleteBatch(List<UUID> ids) {
        return transactionTemplate.execute(status -> {
            return repository.removeByIds(ids);
        });
    }

    /**
     * 查询详情
     */
    public AppLogs getDetail(UUID id) {
        return repository.getById(id);
    }

    /**
     * 列表查询
     */
    public List<AppLogs> list() {
        return repository.list();
    }

    /**
     * 分页查询
     */
    public Page<AppLogs> page(Page<AppLogs> page) {
        return repository.page(page);
    }

}