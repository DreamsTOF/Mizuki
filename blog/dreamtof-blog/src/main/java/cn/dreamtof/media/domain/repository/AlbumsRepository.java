package cn.dreamtof.media.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.Albums;

import java.util.List;
import java.util.UUID;

public interface AlbumsRepository {

    Albums create(Albums entity);

    boolean removeById(UUID id);

    Albums update(Albums entity);

    Albums getById(UUID id);

    List<Albums> listAll();

    PageResult<Albums> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Albums> entities);

    boolean existsById(UUID id);

    List<Albums> listByIds(List<UUID> ids);

    CursorResult<Albums> seek(UUID cursor, int limit);
}
