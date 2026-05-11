package cn.dreamtof.content.domain.service;

import cn.dreamtof.content.domain.model.entity.Categories;
import cn.dreamtof.content.domain.repository.CategoriesRepository;
import cn.dreamtof.core.exception.Asserts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryDomainService {

    private final CategoriesRepository categoriesRepository;
    private final SlugService slugService;

    public Categories createCategory(String name, String slug, UUID parentId,
                                     String description, String icon, String coverImage,
                                     Integer sortOrder, Boolean hasEnabled) {
        Asserts.notBlank(name, "分类名称不能为空");
        if (parentId != null) {
            Categories parent = categoriesRepository.getById(parentId);
            Asserts.notNull(parent, "父分类不存在");
        }
        String finalSlug = slug != null ? slug : slugService.generateSlug(name);
        finalSlug = slugService.ensureUnique(finalSlug, null);
        Categories existing = categoriesRepository.findBySlug(finalSlug);
        Asserts.isTrue(existing == null, "分类slug已存在: " + finalSlug);
        Categories entity = Categories.create(name, finalSlug, parentId, description, icon, coverImage, sortOrder, hasEnabled);
        return categoriesRepository.create(entity);
    }

    public Categories updateCategory(UUID id, String name, String slug, UUID parentId,
                                     String description, String icon, String coverImage,
                                     Integer sortOrder, Boolean hasEnabled) {
        Categories existing = categoriesRepository.getById(id);
        Asserts.notNull(existing, "分类不存在");
        if (parentId != null) {
            Asserts.isFalse(parentId.equals(id), "分类不能成为自身的子分类");
            Categories parent = categoriesRepository.getById(parentId);
            Asserts.notNull(parent, "父分类不存在");
        }
        String finalSlug = slug;
        if (finalSlug != null && !finalSlug.equals(existing.getSlug())) {
            finalSlug = slugService.ensureUnique(finalSlug, id);
        }
        existing.update(name, finalSlug, parentId, description, icon, coverImage, sortOrder, hasEnabled);
        return categoriesRepository.update(existing);
    }

    public List<Map<String, Object>> buildCategoryTree() {
        List<Categories> allCategories = categoriesRepository.listAll();
        Map<UUID, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (Categories cat : allCategories) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", cat.getId());
            node.put("name", cat.getName());
            node.put("slug", cat.getSlug());
            node.put("description", cat.getDescription());
            node.put("parentId", cat.getParentId());
            node.put("icon", cat.getIcon());
            node.put("coverImage", cat.getCoverImage());
            node.put("sortOrder", cat.getSortOrder());
            node.put("hasEnabled", cat.getHasEnabled());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(cat.getId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Categories cat : allCategories) {
            Map<String, Object> node = nodeMap.get(cat.getId());
            if (cat.getParentId() == null || !nodeMap.containsKey(cat.getParentId())) {
                roots.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(cat.getParentId());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                children.add(node);
            }
        }
        return roots;
    }

    public Categories findBySlug(String slug) {
        Asserts.notBlank(slug, "分类slug不能为空");
        return categoriesRepository.findBySlug(slug);
    }

    public Categories getById(UUID id) {
        Categories entity = categoriesRepository.getById(id);
        Asserts.notNull(entity, "分类不存在");
        return entity;
    }

    public boolean removeById(UUID id) {
        Categories existing = categoriesRepository.getById(id);
        Asserts.notNull(existing, "分类不存在");
        List<Categories> children = categoriesRepository.listByParentId(id);
        Asserts.isTrue(children.isEmpty(), "该分类下存在子分类，无法删除");
        existing.markDeleted();
        categoriesRepository.update(existing);
        return true;
    }
}
