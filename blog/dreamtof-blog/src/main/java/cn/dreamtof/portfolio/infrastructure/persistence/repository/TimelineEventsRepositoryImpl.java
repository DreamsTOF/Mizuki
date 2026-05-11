package cn.dreamtof.portfolio.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.application.assembler.TimelineEventsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.TimelineEvents;
import cn.dreamtof.portfolio.domain.repository.TimelineEventsRepository;
import cn.dreamtof.portfolio.infrastructure.persistence.mapper.TimelineEventsMapper;
import cn.dreamtof.portfolio.infrastructure.persistence.po.TimelineEventsPO;
import cn.dreamtof.portfolio.infrastructure.persistence.po.table.TimelineEventsTableDef;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TimelineEventsRepositoryImpl extends ServiceImpl<TimelineEventsMapper, TimelineEventsPO> implements TimelineEventsRepository {

    private final TimelineEventsAssembler assembler;
    private static final TimelineEventsTableDef T = TimelineEventsTableDef.TIMELINE_EVENTS_PO;

    @Override
    public TimelineEvents create(TimelineEvents entity) {
        TimelineEventsPO po = assembler.toPO(entity);
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
    public TimelineEvents update(TimelineEvents entity) {
        TimelineEventsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public TimelineEvents getById(UUID id) {
        TimelineEventsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<TimelineEvents> listAll() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.START_DATE.desc());
        List<TimelineEventsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<TimelineEvents> page(PageReq pageReq) {
        Page<TimelineEventsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.START_DATE.desc());
        Page<TimelineEventsPO> resultPage = super.page(flexPage, qw);
        List<TimelineEvents> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<TimelineEvents> entities) {
        List<TimelineEventsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<TimelineEvents> listByIds(List<UUID> ids) {
        List<TimelineEventsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<TimelineEvents> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<TimelineEventsPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<TimelineEventsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<TimelineEvents> listByEventType(String eventType) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.TYPE.eq(eventType));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.START_DATE.desc());
        List<TimelineEventsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<String> listEventTypes() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.select(T.TYPE);
        qw.groupBy(T.TYPE);
        List<TimelineEventsPO> poList = super.list(qw);
        List<String> types = new ArrayList<>(poList.size());
        for (TimelineEventsPO po : poList) {
            if (po.getType() != null) {
                types.add(po.getType());
            }
        }
        return types;
    }
}
