package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.domain.model.entity.Projects;

import java.util.List;
import java.util.UUID;

public interface ProjectsRepository {

    Projects create(Projects entity);

    boolean removeById(UUID id);

    Projects update(Projects entity);

    Projects getById(UUID id);

    List<Projects> listAll();

    PageResult<Projects> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Projects> entities);

    boolean existsById(UUID id);

    List<Projects> listByIds(List<UUID> ids);

    CursorResult<Projects> seek(UUID cursor, int limit);

    List<Projects> listByCategory(String category);

    List<String> listCategories();
}
