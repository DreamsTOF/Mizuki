package cn.dreamtof.content.application.service;

import cn.dreamtof.content.api.vo.CategoryTreeVO;
import cn.dreamtof.content.api.vo.CategoryVO;
import cn.dreamtof.content.application.assembler.CategoriesAssembler;
import cn.dreamtof.content.domain.model.entity.Categories;
import cn.dreamtof.content.domain.service.CategoryDomainService;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.repository.CategoriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriesAppService {

    private final CategoryDomainService categoryDomainService;
    private final CategoriesRepository categoriesRepository;
    private final CategoriesAssembler assembler;

    public CategoryVO createCategory(String name, String slug, UUID parentId,
                                     String description, String icon, String coverImage,
                                     Integer sortOrder, Boolean hasEnabled) {
        Categories created = categoryDomainService.createCategory(name, slug, parentId,
                description, icon, coverImage, sortOrder, hasEnabled);
        log.info("分类创建完成, categoryId={}, name={}", created.getId(), name);
        return assembler.toVO(created);
    }

    public CategoryVO updateCategory(UUID id, String name, String slug, UUID parentId,
                                     String description, String icon, String coverImage,
                                     Integer sortOrder, Boolean hasEnabled) {
        Categories updated = categoryDomainService.updateCategory(id, name, slug, parentId,
                description, icon, coverImage, sortOrder, hasEnabled);
        log.info("分类更新完成, categoryId={}", id);
        return assembler.toVO(updated);
    }

    public boolean deleteCategory(UUID id) {
        boolean result = categoryDomainService.removeById(id);
        log.info("分类删除完成, categoryId={}", id);
        return result;
    }

    public CategoryVO getById(UUID id) {
        Categories entity = categoryDomainService.getById(id);
        return assembler.toVO(entity);
    }

    public CategoryVO getBySlug(String slug) {
        Categories entity = categoryDomainService.findBySlug(slug);
        return entity != null ? assembler.toVO(entity) : null;
    }

    public List<CategoryVO> listAll() {
        List<Categories> entities = categoriesRepository.listAll();
        return assembler.toVOList(entities);
    }

    public PageResult<CategoryVO> pageCategories(PageReq pageReq) {
        PageResult<Categories> pageResult = categoriesRepository.page(pageReq);
        List<CategoryVO> voList = assembler.toVOList(pageResult.getRecords());
        return PageResult.of(voList, pageResult.getTotal(), pageResult.getPages(), pageResult.getPageNum(), pageResult.getPageSize());
    }

    @SuppressWarnings("unchecked")
    public List<CategoryTreeVO> buildCategoryTree() {
        List<Map<String, Object>> tree = categoryDomainService.buildCategoryTree();
        return convertTree(tree);
    }

    private List<CategoryTreeVO> convertTree(List<Map<String, Object>> nodes) {
        List<CategoryTreeVO> result = new ArrayList<>();
        for (Map<String, Object> node : nodes) {
            CategoryTreeVO vo = new CategoryTreeVO();
            vo.setId((UUID) node.get("id"));
            vo.setName((String) node.get("name"));
            vo.setSlug((String) node.get("slug"));
            vo.setDescription((String) node.get("description"));
            vo.setParentId((UUID) node.get("parentId"));
            vo.setIcon((String) node.get("icon"));
            vo.setCoverImage((String) node.get("coverImage"));
            vo.setSortOrder((Integer) node.get("sortOrder"));
            vo.setHasEnabled((Boolean) node.get("hasEnabled"));
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children != null && !children.isEmpty()) {
                vo.setChildren(convertTree(children));
            } else {
                vo.setChildren(new ArrayList<>());
            }
            result.add(vo);
        }
        return result;
    }
}
