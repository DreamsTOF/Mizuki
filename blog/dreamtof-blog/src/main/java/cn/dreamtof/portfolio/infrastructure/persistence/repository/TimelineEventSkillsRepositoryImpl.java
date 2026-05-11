package cn.dreamtof.portfolio.infrastructure.persistence.repository;

import cn.dreamtof.portfolio.application.assembler.TimelineEventSkillsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEventSkills;
import cn.dreamtof.portfolio.domain.repository.TimelineEventSkillsRepository;
import cn.dreamtof.portfolio.infrastructure.persistence.mapper.TimelineEventSkillsMapper;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventSkillsPO;
import cn.dreamtof.portfolio.infrastructure.persistence.po.table.TimelineEventSkillsTableDef;
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
public class TimelineEventSkillsRepositoryImpl extends ServiceImpl<TimelineEventSkillsMapper, TimelineEventSkillsPO> implements TimelineEventSkillsRepository {

    private final TimelineEventSkillsAssembler assembler;
    private static final TimelineEventSkillsTableDef T = TimelineEventSkillsTableDef.TIMELINE_EVENT_SKILLS_PO;

    @Override
    public TimelineEventSkills create(TimelineEventSkills entity) {
        TimelineEventSkillsPO po = assembler.toPO(entity);
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
    public boolean saveBatch(List<TimelineEventSkills> entities) {
        List<TimelineEventSkillsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public List<TimelineEventSkills> listByTimelineEventId(UUID timelineEventId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.TIMELINE_EVENT_ID.eq(timelineEventId));
        List<TimelineEventSkillsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public void removeByTimelineEventId(UUID timelineEventId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.TIMELINE_EVENT_ID.eq(timelineEventId));
        super.removeByIds(super.list(qw).stream().map(TimelineEventSkillsPO::getId).toList());
    }
}
