package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.portfolio.domain.model.entity.TimelineEventSkills;

import java.util.List;
import java.util.UUID;

public interface TimelineEventSkillsRepository {

    TimelineEventSkills create(TimelineEventSkills entity);

    boolean removeById(UUID id);

    boolean saveBatch(List<TimelineEventSkills> entities);

    List<TimelineEventSkills> listByTimelineEventId(UUID timelineEventId);

    void removeByTimelineEventId(UUID timelineEventId);
}
