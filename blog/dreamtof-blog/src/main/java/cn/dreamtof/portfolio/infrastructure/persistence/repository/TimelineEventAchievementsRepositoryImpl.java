package cn.dreamtof.portfolio.infrastructure.persistence.repository;

import cn.dreamtof.portfolio.application.assembler.TimelineEventAchievementsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventAchievements;
import cn.dreamtof.portfolio.domain.repository.TimelineEventAchievementsRepository;
import cn.dreamtof.portfolio.infrastructure.persistence.mapper.TimelineEventAchievementsMapper;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventAchievementsPO;
import cn.dreamtof.portfolio.infrastructure.persistence.po.table.TimelineEventAchievementsTableDef;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TimelineEventAchievementsRepositoryImpl extends ServiceImpl<TimelineEventAchievementsMapper, TimelineEventAchievementsPO> implements TimelineEventAchievementsRepository {

    private final TimelineEventAchievementsAssembler assembler;
    private static final TimelineEventAchievementsTableDef T = TimelineEventAchievementsTableDef.TIMELINE_EVENT_ACHIEVEMENTS_PO;

    @Override
    public TimelineEventAchievements create(TimelineEventAchievements entity) {
        TimelineEventAchievementsPO po = assembler.toPO(entity);
        if (super.save(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public boolean removeById(UUID id) {
        return super.removeById(id);
    }

    @Override
    public boolean saveBatch(List<TimelineEventAchievements> entities) {
        List<TimelineEventAchievementsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public List<TimelineEventAchievements> listByTimelineEventId(UUID timelineEventId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.TIMELINE_EVENT_ID.eq(timelineEventId));
        qw.orderBy(T.SORT_ORDER.asc());
        List<TimelineEventAchievementsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public void removeByTimelineEventId(UUID timelineEventId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.TIMELINE_EVENT_ID.eq(timelineEventId));
        super.removeByIds(super.list(qw).stream().map(TimelineEventAchievementsPO::getId).toList());
    }
}
