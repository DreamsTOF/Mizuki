package cn.dreamtof.content.domain.service;

import cn.dreamtof.content.domain.model.entity.Tags;
import cn.dreamtof.content.domain.repository.TagsRepository;
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
public class TagDomainService {

    private final TagsRepository tagsRepository;
    private final SlugService slugService;

    public Tags createTag(String name, String slug) {
        Asserts.notBlank(name, "标签名称不能为空");
        Tags existing = tagsRepository.findByName(name);
        Asserts.isTrue(existing == null, "标签名称已存在: " + name);
        String finalSlug = slug != null ? slug : slugService.generateSlug(name);
        finalSlug = slugService.ensureUnique(finalSlug, null);
        Tags entity = Tags.create(name, finalSlug);
        return tagsRepository.create(entity);
    }

    public Tags updateTag(UUID id, String name, String slug) {
        Tags existing = tagsRepository.getById(id);
        Asserts.notNull(existing, "标签不存在");
        if (name != null && !name.equals(existing.getName())) {
            Tags byName = tagsRepository.findByName(name);
            Asserts.isTrue(byName == null || byName.getId().equals(id), "标签名称已存在: " + name);
        }
        String finalSlug = slug;
        if (finalSlug != null && !finalSlug.equals(existing.getSlug())) {
            finalSlug = slugService.ensureUnique(finalSlug, id);
        }
        existing.update(name, finalSlug);
        return tagsRepository.update(existing);
    }

    public List<UUID> upsertTags(List<String> tagNames) {
        Asserts.isTrue(tagNames != null && !tagNames.isEmpty(), "标签名称列表不能为空");
        List<UUID> tagIds = new ArrayList<>(tagNames.size());
        for (String tagName : tagNames) {
            Tags existing = tagsRepository.findByName(tagName);
            if (existing != null) {
                tagIds.add(existing.getId());
            } else {
                String slug = slugService.generateSlug(tagName);
                slug = slugService.ensureUnique(slug, null);
                Tags created = Tags.create(tagName, slug);
                created = tagsRepository.create(created);
                tagIds.add(created.getId());
            }
        }
        return tagIds;
    }

    public Tags findBySlug(String slug) {
        Asserts.notBlank(slug, "标签slug不能为空");
        return tagsRepository.findBySlug(slug);
    }

    public Tags getById(UUID id) {
        Tags entity = tagsRepository.getById(id);
        Asserts.notNull(entity, "标签不存在");
        return entity;
    }

    public boolean removeById(UUID id) {
        return tagsRepository.removeById(id);
    }
}
