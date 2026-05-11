package cn.dreamtof.social.application.service;

import cn.dreamtof.social.domain.model.entity.FriendTagLinks;
import cn.dreamtof.social.domain.repository.FriendTagLinksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendTagLinksAppService {

    private final FriendTagLinksRepository repository;

    public FriendTagLinks create(FriendTagLinks entity) {
        return repository.create(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public List<FriendTagLinks> listByFriendId(UUID friendId) {
        return repository.listByFriendId(friendId);
    }

    public List<FriendTagLinks> listByTagId(UUID tagId) {
        return repository.listByTagId(tagId);
    }

    public boolean removeByFriendId(UUID friendId) {
        return repository.removeByFriendId(friendId);
    }

    public boolean removeByFriendIdAndTagId(UUID friendId, UUID tagId) {
        return repository.removeByFriendIdAndTagId(friendId, tagId);
    }

    public boolean saveBatch(List<FriendTagLinks> entities) {
        return repository.saveBatch(entities);
    }
}
