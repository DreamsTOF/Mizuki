package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.CustomPages;

import java.util.List;
import java.util.UUID;

public interface CustomPagesRepository {

    CustomPages create(CustomPages entity);

    boolean removeById(UUID id);

    CustomPages update(CustomPages entity);

    CustomPages getById(UUID id);

    List<CustomPages> listAll();

    PageResult<CustomPages> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<CustomPages> entities);

    boolean existsById(UUID id);

    List<CustomPages> listByIds(List<UUID> ids);

    CursorResult<CustomPages> seek(UUID cursor, int limit);

    CustomPages findByPageKey(String pageKey);

    List<CustomPages> listByEnabled(boolean enabled);
}
