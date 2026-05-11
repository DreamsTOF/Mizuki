package cn.dreamtof.content.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.model.entity.Archives;

import java.util.List;
import java.util.UUID;

public interface ArchivesRepository {

    Archives create(Archives entity);

    boolean removeById(UUID id);

    Archives update(Archives entity);

    Archives getById(UUID id);

    List<Archives> listAll();

    PageResult<Archives> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Archives> entities);

    boolean existsById(UUID id);

    List<Archives> listByIds(List<UUID> ids);

    CursorResult<Archives> seek(UUID cursor, int limit);

    Archives findByYearAndMonth(Integer year, Integer month);

    List<Archives> listByYear(Integer year);

    void deleteAll();
}
