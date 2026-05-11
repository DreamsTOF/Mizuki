package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.portfolio.domain.model.entity.TimelineEventLinks;

import java.util.List;
import java.util.UUID;

public interface TimelineEventLinksRepository {

    TimelineEventLinks create(TimelineEventLinks entity);

    boolean removeById(UUID id);

    boolean saveBatch(List<TimelineEventLinks> entities);

    List<TimelineEventLinks> listByTimelineEventId(UUID timelineEventId);

    void removeByTimelineEventId(UUID timelineEventId);
}
