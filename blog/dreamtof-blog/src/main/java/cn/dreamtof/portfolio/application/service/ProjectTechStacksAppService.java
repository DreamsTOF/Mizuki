package cn.dreamtof.portfolio.application.service;

import cn.dreamtof.portfolio.domain.model.entity.ProjectTechStacks;
import cn.dreamtof.portfolio.domain.repository.ProjectTechStacksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 项目技术栈关联表 应用服务
 * <p>
 * 子表实体，核心业务由 ProjectDomainService 编排，
 * 本服务仅提供基础 CRUD 代理能力。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectTechStacksAppService {

    private final ProjectTechStacksRepository repository;

    public ProjectTechStacks create(ProjectTechStacks entity) {
        return repository.create(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public ProjectTechStacks getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<ProjectTechStacks> listByProjectId(UUID projectId) {
        return repository.listByProjectId(projectId);
    }
}
