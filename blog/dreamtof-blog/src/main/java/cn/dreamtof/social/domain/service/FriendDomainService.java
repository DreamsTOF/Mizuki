package cn.dreamtof.social.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.social.domain.model.entity.FriendTagLinks;
import cn.dreamtof.social.domain.model.entity.FriendTags;
import cn.dreamtof.social.domain.model.entity.Friends;
import cn.dreamtof.social.domain.repository.FriendTagLinksRepository;
import cn.dreamtof.social.domain.repository.FriendTagsRepository;
import cn.dreamtof.social.domain.repository.FriendsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendDomainService {

    private final FriendsRepository friendsRepository;
    private final FriendTagLinksRepository tagLinksRepository;
    private final FriendTagsRepository friendTagsRepository;

    public Friends createFriend(Friends entity) {
        Asserts.notNull(entity, "友链实体不能为空");
        Asserts.notBlank(entity.getTitle(), "站点标题不能为空");
        return friendsRepository.create(entity);
    }

    public Friends updateFriend(Friends entity) {
        Asserts.notNull(entity, "友链实体不能为空");
        Asserts.notNull(entity.getId(), "友链ID不能为空");
        Friends existing = friendsRepository.getById(entity.getId());
        Asserts.notNull(existing, "友链不存在");
        return friendsRepository.update(entity);
    }

    public boolean deleteFriend(UUID id) {
        Friends existing = friendsRepository.getById(id);
        Asserts.notNull(existing, "友链不存在");
        tagLinksRepository.removeByFriendId(id);
        boolean removed = friendsRepository.removeById(id);
        log.info("友链删除完成, friendId={}", id);
        return removed;
    }

    public Friends getById(UUID id) {
        Friends entity = friendsRepository.getById(id);
        Asserts.notNull(entity, "友链不存在");
        return entity;
    }

    public List<Friends> listAll() {
        return friendsRepository.listAll();
    }

    public List<String> getTagNames(UUID friendId) {
        List<FriendTagLinks> links = tagLinksRepository.listByFriendId(friendId);
        List<String> names = new ArrayList<>(links.size());
        for (FriendTagLinks link : links) {
            FriendTags tag = friendTagsRepository.getById(link.getTagId());
            if (tag != null) {
                names.add(tag.getName());
            }
        }
        return names;
    }
}
