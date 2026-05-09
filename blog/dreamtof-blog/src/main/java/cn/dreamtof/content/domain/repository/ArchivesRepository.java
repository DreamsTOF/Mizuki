package cn.dreamtof.content.domain.repository;


import com.mybatisflex.core.paginate.Page;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.blog.content.domain.model.entity.Archives;   // 领域层 Entity
import cn.dreamtof.blog.content.api.request.ArchivesPageReq;
import cn.dreamtof.blog.content.api.request.ArchivesCursorReq;
import cn.dreamtof.core.base.CursorResult;
import java.util.List;

import java.util.UUID;

/**
 * 文章归档索引表 仓储接口 (Domain Layer)
 * <p>
 * 职责：定义领域层所需的持久化契约。
 * 屏蔽底层框架 (MyBatis-Flex) 细节，仅操作领域实体。
 * </p>
 *
 * @author dream
 * @since 2026-05-08
 */
public interface ArchivesRepository {

    // ================== 1. 基础具名操作 ==================

    /**
     * 保存实体
     *
     * @param entity 领域对象
     * @return 包含 ID 的领域对象
     */
    Archives create(Archives entity);

    /**
     * 根据 ID 删除
     *
     * @param id 主键
     * @return true 成功
     */
    boolean removeById(UUID id);

    /**
     * 根据 ID 更新
     *
     * @param entity 包含 ID 的领域对象
     * @return 更新后的领域对象
     */
    Archives update(Archives entity);

    /**
     * 根据 ID 获取详情
     *
     * @param id 主键
     * @return 领域对象
     */
    Archives getById(UUID id);

    /**
     * 获取全量列表
     *
     * @return 领域实体集合
     */
    List<Archives> listAll();

    /**
     * 分页查询 (增强版)
     *
     * @param pageReq 分页请求参数
     * @return 领域实体分页结果
     */
    PageResult<Archives> page(ArchivesPageReq pageReq);

    // ================== 2. 增强扩展操作 ==================

    /**
     * 批量删除
     *
     * @param ids ID 集合
     * @return true 执行成功
     */
    Boolean removeByIds(List<UUID> ids);

    /**
     * 批量保存
     *
     * @param entities 领域实体集合
     * @return true 全部成功
     */
    boolean saveBatch(List<Archives> entities);

    /**
     * 检查是否存在
     *
     * @param id 主键
     * @return true 存在
     */
    boolean existsById(UUID id);

    /**
     * 根据 ID 集合批量获取
     *
     * @param ids ID 集合
     * @return 领域实体列表
     */
    List<Archives> listByIds(List<UUID> ids);

    /**
     * 游标查询 (Seek Method / 瀑布流)
     *
     * @param req 游标查询请求
     * @return 分页结果包装类
     */
    CursorResult<Archives> seek(ArchivesCursorReq req);
}