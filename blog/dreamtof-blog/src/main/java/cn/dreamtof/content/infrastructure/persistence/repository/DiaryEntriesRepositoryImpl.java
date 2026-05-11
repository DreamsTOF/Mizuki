package cn.dreamtof.content.infrastructure.persistence.repository;

import cn.dreamtof.content.application.assembler.DiaryEntriesAssembler;
import cn.dreamtof.content.domain.model.entity.DiaryEntries;
import cn.dreamtof.content.domain.repository.DiaryEntriesRepository;
import cn.dreamtof.content.infrastructure.persistence.mapper.DiaryEntriesMapper;
import cn.dreamtof.content.infrastructure.persistence.po.DiaryEntriesPO;
import cn.dreamtof.content.infrastructure.persistence.po.table.DiaryEntriesTableDef;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DiaryEntriesRepositoryImpl extends ServiceImpl<DiaryEntriesMapper, DiaryEntriesPO> implements DiaryEntriesRepository {

    private final DiaryEntriesAssembler assembler;

    private static final DiaryEntriesTableDef T = DiaryEntriesTableDef.DIARY_ENTRIES_PO;

    @Override
    public DiaryEntries create(DiaryEntries entity) {
        DiaryEntriesPO po = assembler.toPO(entity);
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
    public DiaryEntries update(DiaryEntries entity) {
        DiaryEntriesPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public DiaryEntries getById(UUID id) {
        DiaryEntriesPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<DiaryEntries> listAll() {
        List<DiaryEntriesPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<DiaryEntries> page(PageReq pageReq) {
        Page<DiaryEntriesPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.DATE.desc());
        Page<DiaryEntriesPO> resultPage = super.page(flexPage, qw);
        List<DiaryEntries> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<DiaryEntries> entities) {
        List<DiaryEntriesPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<DiaryEntries> listByIds(List<UUID> ids) {
        List<DiaryEntriesPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<DiaryEntries> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<DiaryEntriesPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<DiaryEntriesPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<DiaryEntries> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DATE.ge(startDate));
        qw.where(T.DATE.le(endDate));
        qw.orderBy(T.DATE.desc());
        List<DiaryEntriesPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }
}
