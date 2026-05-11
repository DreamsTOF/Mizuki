package cn.dreamtof.media.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.Anime;

import java.util.List;
import java.util.UUID;

public interface AnimeRepository {

    Anime create(Anime entity);

    boolean removeById(UUID id);

    Anime update(Anime entity);

    Anime getById(UUID id);

    List<Anime> listAll();

    PageResult<Anime> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Anime> entities);

    boolean existsById(UUID id);

    List<Anime> listByIds(List<UUID> ids);

    CursorResult<Anime> seek(UUID cursor, int limit);

    List<Anime> listByStatus(String status);
}
