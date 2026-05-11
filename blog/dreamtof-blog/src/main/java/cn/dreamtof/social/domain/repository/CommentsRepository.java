package cn.dreamtof.social.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.domain.model.entity.Comments;

import java.util.List;
import java.util.UUID;

public interface CommentsRepository {

    Comments create(Comments entity);

    boolean removeById(UUID id);

    Comments update(Comments entity);

    Comments getById(UUID id);

    List<Comments> listAll();

    PageResult<Comments> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Comments> entities);

    boolean existsById(UUID id);

    List<Comments> listByIds(List<UUID> ids);

    CursorResult<Comments> seek(UUID cursor, int limit);

    List<Comments> listByTargetId(UUID targetId);

    List<Comments> listByTargetIdAndType(UUID targetId, String type);

    long countByTargetId(UUID targetId);

    long countByTargetIdAndType(UUID targetId, String type);

    List<Comments> listPending();
}
