package cn.dreamtof.social.domain.repository;


import cn.dreamtof.blog.social.infrastructure.persistence.po.Comments;
import cn.dreamtof.blog.social.domain.repository.CommentsRepository;
import cn.dreamtof.blog.social.api.request.CommentsPageReq;
import cn.dreamtof.blog.social.api.request.CommentsCursorReq;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.dreamtof.core.config.VirtualTaskManager;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageResult;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import java.util.List;

import java.util.UUID;

/**
 * 评论表 应用服务
 * <p>
 * 职责：
 * 1. 编排业务流程（事务控制、权限校验、通知发送）。
 * 2. 负责 Entity 与 PO 的互相转换，隔离底层数据模型。
 * 3. 指挥 Repository 进行数据持久化。
 * </p>
 *
 * @author dream
 * @since 2026-05-08
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentsRepository {

    private final CommentsRepository repository;
    private final VirtualTaskManager virtualTaskManager;

    /**
     * 创建 评论表
     */
    public Comments create(Comments entity) {
        // TODO: 可以在这里调用 entity.init() 或其他领域行为

        // 直接下传 Entity，由 Repository 负责 Entity -> PO 转换
        return repository.create(entity);
    }

    /**
     * 更新 评论表
     */
    public Comments update(Comments entity) {
        // 直接下传 Entity，由 Repository 负责 Entity -> PO 转换
        return repository.update(entity);
    }

    /**
     * 删除 评论表
     */
    public boolean remove(UUID id) {
        return repository.removeById(id);
    }

    /**
     * 批量删除
     */
    public boolean removeByIds(List<UUID> ids) {
        return repository.removeByIds(ids);
    }

    /**
     * 查询详情
     */
    public Comments getDetail(UUID id) {
        // 查出 Entity，由 Repository 内部处理 PO -> Entity 转换
        return repository.getById(id);
    }

    /**
     * 列表查询
     */
    public List<Comments> listAll() {
        // 查出 Entity 列表
        return repository.listAll();
    }

    /**
     * 分页查询
     * @param pageReq 包含分页参数（pageNum, pageSize）及过滤条件的 DTO
     */
    public PageResult<Comments> page(CommentsPageReq pageReq) {
        // 逻辑下沉到 Repository，App 层保持纯净
        return repository.page(pageReq);
    }

    /**
     * 游标查询 (瀑布流)
     */
    public CursorResult<Comments> seek(CommentsCursorReq req) {
        // 逻辑下沉到 Repository，App 层保持纯净
        return repository.seek(req);
    }

}