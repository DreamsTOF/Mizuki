package cn.dreamtof.social.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.domain.model.entity.FriendTags;

import java.util.List;
import java.util.UUID;

public interface FriendTagsRepository {

    FriendTags create(FriendTags entity);

    boolean removeById(UUID id);

    FriendTags update(FriendTags entity);

    FriendTags getById(UUID id);

    List<FriendTags> listAll();

    PageResult<FriendTags> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<FriendTags> entities);

    boolean existsById(UUID id);

    List<FriendTags> listByIds(List<UUID> ids);

    CursorResult<FriendTags> seek(UUID cursor, int limit);
}
