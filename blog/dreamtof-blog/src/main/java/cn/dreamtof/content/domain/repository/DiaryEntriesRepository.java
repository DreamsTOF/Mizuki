package cn.dreamtof.content.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.model.entity.DiaryEntries;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface DiaryEntriesRepository {

    DiaryEntries create(DiaryEntries entity);

    boolean removeById(UUID id);

    DiaryEntries update(DiaryEntries entity);

    DiaryEntries getById(UUID id);

    List<DiaryEntries> listAll();

    PageResult<DiaryEntries> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<DiaryEntries> entities);

    boolean existsById(UUID id);

    List<DiaryEntries> listByIds(List<UUID> ids);

    CursorResult<DiaryEntries> seek(UUID cursor, int limit);

    List<DiaryEntries> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate);
}
