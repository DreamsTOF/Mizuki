package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.DailyStats;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface DailyStatsRepository {

    DailyStats create(DailyStats entity);

    boolean removeById(UUID id);

    DailyStats update(DailyStats entity);

    DailyStats getById(UUID id);

    List<DailyStats> listAll();

    PageResult<DailyStats> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<DailyStats> entities);

    boolean existsById(UUID id);

    List<DailyStats> listByIds(List<UUID> ids);

    CursorResult<DailyStats> seek(UUID cursor, int limit);

    DailyStats getByStatDate(OffsetDateTime statDate);

    List<DailyStats> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate);
}
