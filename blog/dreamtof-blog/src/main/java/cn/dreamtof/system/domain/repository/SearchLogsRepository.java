package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.SearchLogs;

import java.util.List;
import java.util.UUID;

public interface SearchLogsRepository {

    SearchLogs create(SearchLogs entity);

    boolean removeById(UUID id);

    SearchLogs update(SearchLogs entity);

    SearchLogs getById(UUID id);

    List<SearchLogs> listAll();

    PageResult<SearchLogs> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<SearchLogs> entities);

    boolean existsById(UUID id);

    List<SearchLogs> listByIds(List<UUID> ids);

    CursorResult<SearchLogs> seek(UUID cursor, int limit);

    List<Object[]> getHotKeywords(int limit);
}
