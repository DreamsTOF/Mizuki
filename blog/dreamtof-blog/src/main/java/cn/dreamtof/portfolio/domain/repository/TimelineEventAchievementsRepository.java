package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.portfolio.domain.model.entity.TimelineEventAchievements;

import java.util.List;
import java.util.UUID;

public interface TimelineEventAchievementsRepository {

    TimelineEventAchievements create(TimelineEventAchievements entity);

    boolean removeById(UUID id);

    boolean saveBatch(List<TimelineEventAchievements> entities);

    List<TimelineEventAchievements> listByTimelineEventId(UUID timelineEventId);

    void removeByTimelineEventId(UUID timelineEventId);
}
