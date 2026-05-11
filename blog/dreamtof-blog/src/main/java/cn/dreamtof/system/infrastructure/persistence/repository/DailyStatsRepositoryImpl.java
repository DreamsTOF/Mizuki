package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.DailyStatsAssembler;
import cn.dreamtof.system.domain.model.entity.DailyStats;
import cn.dreamtof.system.domain.repository.DailyStatsRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.DailyStatsMapper;
import cn.dreamtof.system.infrastructure.persistence.po.DailyStatsPO;
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
public class DailyStatsRepositoryImpl extends ServiceImpl<DailyStatsMapper, DailyStatsPO> implements DailyStatsRepository {

    private final DailyStatsAssembler assembler;

    @Override
    public DailyStats create(DailyStats entity) {
        DailyStatsPO po = assembler.toPO(entity);
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
    public DailyStats update(DailyStats entity) {
        DailyStatsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public DailyStats getById(UUID id) {
        DailyStatsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<DailyStats> listAll() {
        List<DailyStatsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<DailyStats> page(PageReq pageReq) {
        Page<DailyStatsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<DailyStatsPO> resultPage = super.page(flexPage, queryWrapper);
        List<DailyStats> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<DailyStats> entities) {
        List<DailyStatsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(DailyStatsPO::getId).eq(id).exists();
    }

    @Override
    public List<DailyStats> listByIds(List<UUID> ids) {
        List<DailyStatsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<DailyStats> seek(UUID cursor, int limit) {
        List<DailyStatsPO> poList = queryChain()
                .where(DailyStatsPO::getId).gt(cursor)
                .orderBy(DailyStatsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<DailyStatsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public DailyStats getByStatDate(OffsetDateTime statDate) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(DailyStatsPO::getStatDate).eq(statDate);
        DailyStatsPO po = super.getOne(queryWrapper);
        return assembler.toEntity(po);
    }

    @Override
    public List<DailyStats> listByDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(DailyStatsPO::getStatDate).ge(startDate);
        queryWrapper.and(DailyStatsPO::getStatDate).le(endDate);
        queryWrapper.orderBy(DailyStatsPO::getStatDate).asc();
        List<DailyStatsPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }
}
