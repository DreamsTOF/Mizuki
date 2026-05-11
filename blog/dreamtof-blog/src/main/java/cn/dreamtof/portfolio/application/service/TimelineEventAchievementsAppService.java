package cn.dreamtof.portfolio.application.service;

import cn.dreamtof.portfolio.domain.model.entity.TimelineEventAchievements;
import cn.dreamtof.portfolio.domain.repository.TimelineEventAchievementsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 时间线成就关联表 应用服务
 * <p>
 * 子表实体，核心业务由 TimelineDomainService 编排，
 * 本服务仅提供基础 CRUD 代理能力。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineEventAchievementsAppService {

    private final TimelineEventAchievementsRepository repository;

    public TimelineEventAchievements create(TimelineEventAchievements entity) {
        return repository.create(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public List<TimelineEventAchievements> listByTimelineEventId(UUID timelineEventId) {
        return repository.listByTimelineEventId(timelineEventId);
    }
}
