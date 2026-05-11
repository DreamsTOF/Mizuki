package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.CustomPagesAssembler;
import cn.dreamtof.system.domain.model.entity.CustomPages;
import cn.dreamtof.system.domain.repository.CustomPagesRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.CustomPagesMapper;
import cn.dreamtof.system.infrastructure.persistence.po.CustomPagesPO;
import cn.dreamtof.system.infrastructure.persistence.po.table.CustomPagesTableDef;
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
public class CustomPagesRepositoryImpl extends ServiceImpl<CustomPagesMapper, CustomPagesPO> implements CustomPagesRepository {

    private final CustomPagesAssembler assembler;

    private static final CustomPagesTableDef T = CustomPagesTableDef.CUSTOM_PAGES_PO;

    @Override
    public CustomPages create(CustomPages entity) {
        CustomPagesPO po = assembler.toPO(entity);
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
    public CustomPages update(CustomPages entity) {
        CustomPagesPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public CustomPages getById(UUID id) {
        CustomPagesPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<CustomPages> listAll() {
        List<CustomPagesPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<CustomPages> page(PageReq pageReq) {
        Page<CustomPagesPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.CREATED_AT.desc());
        Page<CustomPagesPO> resultPage = super.page(flexPage, qw);
        List<CustomPages> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<CustomPages> entities) {
        List<CustomPagesPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<CustomPages> listByIds(List<UUID> ids) {
        List<CustomPagesPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<CustomPages> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<CustomPagesPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<CustomPagesPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public CustomPages findByPageKey(String pageKey) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.PAGE_KEY.eq(pageKey));
        qw.limit(1);
        CustomPagesPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }

    @Override
    public List<CustomPages> listByEnabled(boolean enabled) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.HAS_ENABLED.eq(enabled));
        qw.orderBy(T.CREATED_AT.desc());
        List<CustomPagesPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }
}
