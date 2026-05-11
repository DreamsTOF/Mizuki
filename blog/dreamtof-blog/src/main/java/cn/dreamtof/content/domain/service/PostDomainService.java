package cn.dreamtof.content.domain.service;

import cn.dreamtof.content.domain.model.entity.PostTags;
import cn.dreamtof.content.domain.model.entity.Posts;
import cn.dreamtof.content.domain.model.entity.Tags;
import cn.dreamtof.content.domain.repository.PostTagsRepository;
import cn.dreamtof.content.domain.repository.PostsRepository;
import cn.dreamtof.core.exception.Asserts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostDomainService {

    private final PostsRepository postsRepository;
    private final PostTagsRepository postTagsRepository;
    private final SlugService slugService;
    private final WordCountService wordCountService;

    public Posts createPost(Posts entity) {
        Asserts.notNull(entity, "文章实体不能为空");
        String uniqueSlug = slugService.ensureUnique(entity.getSlug(), null);
        entity.setSlug(uniqueSlug);
        int wordCount = wordCountService.countWords(entity.getContent());
        entity.updateWordCount(wordCount);
        return postsRepository.create(entity);
    }

    public Posts updatePost(Posts entity) {
        Asserts.notNull(entity, "文章实体不能为空");
        Asserts.notNull(entity.getId(), "文章ID不能为空");
        Posts existing = postsRepository.getById(entity.getId());
        Asserts.notNull(existing, "文章不存在");
        if (entity.getSlug() != null && !entity.getSlug().equals(existing.getSlug())) {
            String uniqueSlug = slugService.ensureUnique(entity.getSlug(), entity.getId());
            entity.setSlug(uniqueSlug);
        }
        if (entity.getContent() != null) {
            int wordCount = wordCountService.countWords(entity.getContent());
            entity.updateWordCount(wordCount);
        }
        return postsRepository.update(entity);
    }

    public boolean deletePost(UUID id) {
        Posts existing = postsRepository.getById(id);
        Asserts.notNull(existing, "文章不存在");
        existing.markDeleted();
        postsRepository.update(existing);
        postTagsRepository.removeByPostId(id);
        log.info("文章软删除完成, postId={}", id);
        return true;
    }

    public Posts publishDraft(UUID id) {
        Posts existing = postsRepository.getById(id);
        Asserts.notNull(existing, "文章不存在");
        existing.publish();
        return postsRepository.update(existing);
    }

    public Posts unpublish(UUID id) {
        Posts existing = postsRepository.getById(id);
        Asserts.notNull(existing, "文章不存在");
        existing.unpublish();
        return postsRepository.update(existing);
    }

    public Posts togglePin(UUID id, boolean pinned, Integer priority) {
        Posts existing = postsRepository.getById(id);
        Asserts.notNull(existing, "文章不存在");
        existing.togglePin(pinned, priority);
        return postsRepository.update(existing);
    }

    public long incrementViewCount(UUID id) {
        Asserts.notNull(id, "文章ID不能为空");
        return postsRepository.incrementViewCount(id);
    }

    public boolean validatePassword(UUID id, String rawPassword) {
        Posts existing = postsRepository.getById(id);
        Asserts.notNull(existing, "文章不存在");
        if (!Boolean.TRUE.equals(existing.getEncrypted())) {
            return true;
        }
        Asserts.notBlank(rawPassword, "密码不能为空");
        return existing.getPassword() != null
                && existing.getPassword().equals(rawPassword);
    }

    public void syncPostTags(UUID postId, List<UUID> tagIds) {
        Asserts.notNull(postId, "文章ID不能为空");
        postTagsRepository.removeByPostId(postId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<PostTags> newLinks = new ArrayList<>(tagIds.size());
        for (UUID tagId : tagIds) {
            if (!postTagsRepository.existsByPostIdAndTagId(postId, tagId)) {
                newLinks.add(PostTags.create(postId, tagId));
            }
        }
        if (!newLinks.isEmpty()) {
            postTagsRepository.saveBatch(newLinks);
        }
    }

    public List<Tags> getPostTags(UUID postId, TagDomainService tagDomainService) {
        List<PostTags> links = postTagsRepository.listByPostId(postId);
        List<Tags> tags = new ArrayList<>(links.size());
        for (PostTags link : links) {
            Tags tag = tagDomainService.getById(link.getTagId());
            if (tag != null) {
                tags.add(tag);
            }
        }
        return tags;
    }

    public Posts getById(UUID id) {
        Posts entity = postsRepository.getById(id);
        Asserts.notNull(entity, "文章不存在");
        return entity;
    }

    public Posts getBySlug(String slug) {
        return postsRepository.findBySlug(slug);
    }
}
