package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.PageViews;

import java.util.List;
import java.util.UUID;

public interface PageViewsRepository {

    PageViews create(PageViews entity);

    boolean removeById(UUID id);

    PageViews update(PageViews entity);

    PageViews getById(UUID id);

    List<PageViews> listAll();

    PageResult<PageViews> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<PageViews> entities);

    boolean existsById(UUID id);

    List<PageViews> listByIds(List<UUID> ids);

    CursorResult<PageViews> seek(UUID cursor, int limit);

    long countByPagePath(String pagePath);

    List<PageViews> listTopPages(int limit);
}
