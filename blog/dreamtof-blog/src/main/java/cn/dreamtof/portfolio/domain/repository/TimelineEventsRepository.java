package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEvents;

import java.util.List;
import java.util.UUID;

public interface TimelineEventsRepository {

    TimelineEvents create(TimelineEvents entity);

    boolean removeById(UUID id);

    TimelineEvents update(TimelineEvents entity);

    TimelineEvents getById(UUID id);

    List<TimelineEvents> listAll();

    PageResult<TimelineEvents> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<TimelineEvents> entities);

    boolean existsById(UUID id);

    List<TimelineEvents> listByIds(List<UUID> ids);

    CursorResult<TimelineEvents> seek(UUID cursor, int limit);

    List<TimelineEvents> listByEventType(String eventType);

    List<String> listEventTypes();
}
