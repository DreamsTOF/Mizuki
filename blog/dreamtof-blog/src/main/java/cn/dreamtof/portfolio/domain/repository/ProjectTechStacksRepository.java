package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTechStacks;

import java.util.List;
import java.util.UUID;

public interface ProjectTechStacksRepository {

    ProjectTechStacks create(ProjectTechStacks entity);

    boolean removeById(UUID id);

    ProjectTechStacks update(ProjectTechStacks entity);

    ProjectTechStacks getById(UUID id);

    List<ProjectTechStacks> listAll();

    PageResult<ProjectTechStacks> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<ProjectTechStacks> entities);

    boolean existsById(UUID id);

    List<ProjectTechStacks> listByIds(List<UUID> ids);

    List<ProjectTechStacks> listByProjectId(UUID projectId);

    void removeByProjectId(UUID projectId);
}
