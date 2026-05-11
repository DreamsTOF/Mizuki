package cn.dreamtof.content.infrastructure.persistence.repository;

import cn.dreamtof.content.application.assembler.ArchivesAssembler;
import cn.dreamtof.content.domain.model.entity.Archives;
import cn.dreamtof.content.domain.repository.ArchivesRepository;
import cn.dreamtof.content.infrastructure.persistence.mapper.ArchivesMapper;
import cn.dreamtof.content.infrastructure.persistence.po.ArchivesPO;
import cn.dreamtof.content.infrastructure.persistence.po.table.ArchivesTableDef;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import com.mybatisflex.core.paginate.Page;
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
public class ArchivesRepositoryImpl extends ServiceImpl<ArchivesMapper, ArchivesPO> implements ArchivesRepository {

    private final ArchivesAssembler assembler;

    private static final ArchivesTableDef T = ArchivesTableDef.ARCHIVES_PO;

    @Override
    public Archives create(Archives entity) {
        ArchivesPO po = assembler.toPO(entity);
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
    public Archives update(Archives entity) {
        ArchivesPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Archives getById(UUID id) {
        ArchivesPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Archives> listAll() {
        List<ArchivesPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Archives> page(PageReq pageReq) {
        Page<ArchivesPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.YEAR.desc());
        qw.orderBy(T.MONTH.desc());
        Page<ArchivesPO> resultPage = super.page(flexPage, qw);
        List<Archives> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Archives> entities) {
        List<ArchivesPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<Archives> listByIds(List<UUID> ids) {
        List<ArchivesPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Archives> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<ArchivesPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<ArchivesPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public Archives findByYearAndMonth(Integer year, Integer month) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.YEAR.eq(year));
        qw.where(T.MONTH.eq(month));
        qw.limit(1);
        ArchivesPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }

    @Override
    public List<Archives> listByYear(Integer year) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.YEAR.eq(year));
        qw.orderBy(T.MONTH.desc());
        List<ArchivesPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public void deleteAll() {
        super.remove(QueryWrapper.create());
    }
}
