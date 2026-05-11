package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.domain.model.entity.ProjectTags;

import java.util.List;
import java.util.UUID;

public interface ProjectTagsRepository {

    ProjectTags create(ProjectTags entity);

    boolean removeById(UUID id);

    ProjectTags update(ProjectTags entity);

    ProjectTags getById(UUID id);

    List<ProjectTags> listAll();

    PageResult<ProjectTags> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<ProjectTags> entities);

    boolean existsById(UUID id);

    List<ProjectTags> listByIds(List<UUID> ids);

    List<ProjectTags> listByProjectId(UUID projectId);

    void removeByProjectId(UUID projectId);
}
