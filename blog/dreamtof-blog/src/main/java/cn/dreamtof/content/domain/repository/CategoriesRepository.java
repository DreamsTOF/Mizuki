package cn.dreamtof.content.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.model.entity.Categories;

import java.util.List;
import java.util.UUID;

public interface CategoriesRepository {

    Categories create(Categories entity);

    boolean removeById(UUID id);

    Categories update(Categories entity);

    Categories getById(UUID id);

    List<Categories> listAll();

    PageResult<Categories> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Categories> entities);

    boolean existsById(UUID id);

    List<Categories> listByIds(List<UUID> ids);

    CursorResult<Categories> seek(UUID cursor, int limit);

    Categories findBySlug(String slug);

    List<Categories> listByParentId(UUID parentId);

    List<Categories> listEnabled();
}
