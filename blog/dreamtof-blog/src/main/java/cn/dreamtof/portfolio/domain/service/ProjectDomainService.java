package cn.dreamtof.portfolio.domain.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTags;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTechStacks;
import cn.dreamtof.portfolio.domain.model.entity.Projects;
import cn.dreamtof.portfolio.domain.repository.ProjectTagsRepository;
import cn.dreamtof.portfolio.domain.repository.ProjectTechStacksRepository;
import cn.dreamtof.portfolio.domain.repository.ProjectsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectDomainService {

    private final ProjectsRepository projectsRepository;
    private final ProjectTechStacksRepository techStacksRepository;
    private final ProjectTagsRepository projectTagsRepository;

    public Projects createProject(Projects entity) {
        Asserts.notNull(entity, "项目实体不能为空");
        return projectsRepository.create(entity);
    }

    public Projects updateProject(Projects entity) {
        Asserts.notNull(entity, "项目实体不能为空");
        Asserts.notNull(entity.getId(), "项目ID不能为空");
        Projects existing = projectsRepository.getById(entity.getId());
        Asserts.notNull(existing, "项目不存在");
        return projectsRepository.update(entity);
    }

    public boolean deleteProject(UUID id) {
        Projects existing = projectsRepository.getById(id);
        Asserts.notNull(existing, "项目不存在");
        existing.markDeleted();
        projectsRepository.update(existing);
        techStacksRepository.removeByProjectId(id);
        projectTagsRepository.removeByProjectId(id);
        log.info("项目软删除完成, projectId={}", id);
        return true;
    }

    public Projects toggleFeatured(UUID id, boolean featured) {
        Projects existing = projectsRepository.getById(id);
        Asserts.notNull(existing, "项目不存在");
        existing.toggleFeatured(featured);
        return projectsRepository.update(existing);
    }

    public Projects getById(UUID id) {
        Projects entity = projectsRepository.getById(id);
        Asserts.notNull(entity, "项目不存在");
        return entity;
    }

    public List<Projects> listByCategory(String category) {
        return projectsRepository.listByCategory(category);
    }

    public List<String> listCategories() {
        return projectsRepository.listCategories();
    }

    public PageResult<Projects> page(PageReq pageReq) {
        return projectsRepository.page(pageReq);
    }

    public List<Projects> listAll() {
        return projectsRepository.listAll();
    }

    public void syncTechStacks(UUID projectId, List<String> techNames) {
        Asserts.notNull(projectId, "项目ID不能为空");
        techStacksRepository.removeByProjectId(projectId);
        if (techNames == null || techNames.isEmpty()) {
            return;
        }
        List<ProjectTechStacks> newStacks = new ArrayList<>(techNames.size());
        for (String techName : techNames) {
            newStacks.add(ProjectTechStacks.create(projectId, techName));
        }
        techStacksRepository.saveBatch(newStacks);
    }

    public List<String> getTechStackNames(UUID projectId) {
        List<ProjectTechStacks> stacks = techStacksRepository.listByProjectId(projectId);
        List<String> names = new ArrayList<>(stacks.size());
        for (ProjectTechStacks stack : stacks) {
            names.add(stack.getTechName());
        }
        return names;
    }

    public void syncTags(UUID projectId, List<String> tagNames) {
        Asserts.notNull(projectId, "项目ID不能为空");
        projectTagsRepository.removeByProjectId(projectId);
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        List<ProjectTags> newTags = new ArrayList<>(tagNames.size());
        for (String tagName : tagNames) {
            newTags.add(ProjectTags.create(projectId, tagName));
        }
        projectTagsRepository.saveBatch(newTags);
    }

    public List<String> getTagNames(UUID projectId) {
        List<ProjectTags> tags = projectTagsRepository.listByProjectId(projectId);
        List<String> names = new ArrayList<>(tags.size());
        for (ProjectTags tag : tags) {
            names.add(tag.getTagName());
        }
        return names;
    }
}
