package cn.dreamtof.social.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.domain.model.entity.Friends;

import java.util.List;
import java.util.UUID;

public interface FriendsRepository {

    Friends create(Friends entity);

    boolean removeById(UUID id);

    Friends update(Friends entity);

    Friends getById(UUID id);

    List<Friends> listAll();

    PageResult<Friends> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Friends> entities);

    boolean existsById(UUID id);

    List<Friends> listByIds(List<UUID> ids);

    CursorResult<Friends> seek(UUID cursor, int limit);
}
