package cn.dreamtof.social.domain.repository;

import cn.dreamtof.social.domain.model.entity.FriendTagLinks;

import java.util.List;
import java.util.UUID;

public interface FriendTagLinksRepository {

    FriendTagLinks create(FriendTagLinks entity);

    boolean removeById(UUID id);

    List<FriendTagLinks> listByFriendId(UUID friendId);

    List<FriendTagLinks> listByTagId(UUID tagId);

    boolean removeByFriendId(UUID friendId);

    boolean removeByFriendIdAndTagId(UUID friendId, UUID tagId);

    boolean saveBatch(List<FriendTagLinks> entities);
}
