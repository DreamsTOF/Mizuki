package cn.dreamtof.log.infrastructure.persistence.repository;


import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.dreamtof.log.domain.model.AppLogs;
import cn.dreamtof.log.infrastructure.persistence.mapper.AppLogsMapper;
import cn.dreamtof.log.domain.repository.AppLogsRepository;
import org.springframework.stereotype.Repository;
import com.mybatisflex.core.paginate.Page;

import java.util.List;
import java.util.UUID;

/**
 *  仓储实现 (Infrastructure Layer)
 * <p>
 * 实现领域层的仓储接口，直接与数据库交互。
 * </p>
 *
 * @author dream
 * @since 
 */
@Repository
public class AppLogsRepositoryImpl extends ServiceImpl<AppLogsMapper, AppLogs> implements AppLogsRepository {

    // ================== 基础操作 (透传 Flex) ==================

    /**
     * 保存  实体
     *
     * @param entity  实体对象
     * @return true 保存成功，false 保存失败
     */
    @Override
    public boolean saveAppLogs(AppLogs entity) {
        return save(entity);
    }

    /**
     * 根据 ID 删除 
     *
     * @param id 主键 ID
     * @return true 删除成功，false 删除失败
     */
    @Override
    public boolean removeAppLogsById(UUID id) {
        return removeById(id);
    }

    /**
     * 根据 ID 更新 
     *
     * @param entity 包含 ID 和更新内容的实体对象
     * @return true 更新成功，false 更新失败
     */
    @Override
    public boolean updateAppLogsById(AppLogs entity) {
        return updateById(entity);
    }

    /**
     * 根据 ID 获取  详情
     *
     * @param id 主键 ID
     * @return  实体对象
     */
    @Override
    public AppLogs getAppLogsById(UUID id) {
        return getById(id);
    }

    /**
     * 获取所有  列表
     *
     * @return  列表集合
     */
    @Override
    public List<AppLogs> listAppLogs() {
        return list();
    }

    /**
     * 分页查询 
     *
     * @param page 分页参数对象
     * @return 包含查询结果的分页对象
     */
    @Override
    public Page<AppLogs> pageAppLogs(Page<AppLogs> page) {
        return page(page);
    }

    // ================== 逻辑操作 (返回 null) ==================


    /**
     * 批量删除
     *
     * @param ids 主键 ID 集合
     * @return Boolean 目前返回 null，待实现
     */
    @Override
    public Boolean removeByIds(List<UUID> ids) {
        // 如果需要启用 Flex 批量删除，可改为 return super.removeByIds(ids);
        return removeByIds(ids);
    }
}