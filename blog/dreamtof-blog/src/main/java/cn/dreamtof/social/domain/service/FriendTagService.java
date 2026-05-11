package cn.dreamtof.social.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.social.domain.model.entity.FriendTagLinks;
import cn.dreamtof.social.domain.model.entity.FriendTags;
import cn.dreamtof.social.domain.repository.FriendTagLinksRepository;
import cn.dreamtof.social.domain.repository.FriendTagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendTagService {

    private final FriendTagsRepository friendTagsRepository;
    private final FriendTagLinksRepository tagLinksRepository;

    public FriendTags createTag(FriendTags entity) {
        Asserts.notNull(entity, "标签实体不能为空");
        Asserts.notBlank(entity.getName(), "标签名称不能为空");
        return friendTagsRepository.create(entity);
    }

    public FriendTags updateTag(FriendTags entity) {
        Asserts.notNull(entity, "标签实体不能为空");
        Asserts.notNull(entity.getId(), "标签ID不能为空");
        FriendTags existing = friendTagsRepository.getById(entity.getId());
        Asserts.notNull(existing, "标签不存在");
        return friendTagsRepository.update(entity);
    }

    public boolean deleteTag(UUID id) {
        FriendTags existing = friendTagsRepository.getById(id);
        Asserts.notNull(existing, "标签不存在");
        boolean removed = friendTagsRepository.removeById(id);
        log.info("友链标签删除完成, tagId={}", id);
        return removed;
    }

    public void linkTag(UUID friendId, UUID tagId) {
        Asserts.notNull(friendId, "友链ID不能为空");
        Asserts.notNull(tagId, "标签ID不能为空");
        FriendTagLinks link = new FriendTagLinks();
        link.setFriendId(friendId);
        link.setTagId(tagId);
        tagLinksRepository.create(link);
        log.info("友链标签关联创建, friendId={}, tagId={}", friendId, tagId);
    }

    public void unlinkTag(UUID friendId, UUID tagId) {
        Asserts.notNull(friendId, "友链ID不能为空");
        Asserts.notNull(tagId, "标签ID不能为空");
        tagLinksRepository.removeByFriendIdAndTagId(friendId, tagId);
        log.info("友链标签关联解除, friendId={}, tagId={}", friendId, tagId);
    }

    public List<FriendTags> listAll() {
        return friendTagsRepository.listAll();
    }
}
