package cn.dreamtof.log.domain.repository;


import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import cn.dreamtof.log.domain.model.AppLogs;
import java.util.List;
import java.util.UUID;

/**
 *  仓储接口 (Domain Layer)
 * <p>
 * 定义领域层的持久化操作标准，继承 IService 以获得基础服务能力。
 * </p>
 *
 * @author dream
 * @since 
 */
public interface AppLogsRepository extends IService<AppLogs> {

    // ================== 1. 基础具名操作 (6个) ==================

    /**
     * 保存  实体
     *
     * @param entity  实体对象
     * @return true 保存成功，false 保存失败
     */
    boolean saveAppLogs(AppLogs entity);

    /**
     * 根据 ID 删除 
     *
     * @param id 主键 ID
     * @return true 删除成功，false 删除失败
     */
    boolean removeAppLogsById(UUID id);

    /**
     * 根据 ID 更新 
     *
     * @param entity 包含 ID 和更新内容的实体对象
     * @return true 更新成功，false 更新失败
     */
    boolean updateAppLogsById(AppLogs entity);

    /**
     * 根据 ID 获取  详情
     *
     * @param id 主键 ID
     * @return  实体对象，若不存在则返回 null
     */
    AppLogs getAppLogsById(UUID id);

    /**
     * 获取所有  列表
     *
     * @return  列表集合
     */
    List<AppLogs> listAppLogs();

    /**
     * 分页查询 
     *
     * @param page 分页参数对象
     * @return 包含查询结果的分页对象
     */
    Page<AppLogs> pageAppLogs(Page<AppLogs> page);

    // ================== 2. 逻辑/业务操作 (5个, 返回 Boolean) ==================

    /**
     * 批量删除
     *
     * @param ids 主键 ID 集合
     * @return Boolean 执行结果
     */
    Boolean removeByIds(List<UUID> ids);

}