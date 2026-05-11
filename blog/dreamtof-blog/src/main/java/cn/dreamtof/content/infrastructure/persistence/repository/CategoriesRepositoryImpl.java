package cn.dreamtof.content.infrastructure.persistence.repository;

import cn.dreamtof.content.application.assembler.CategoriesAssembler;
import cn.dreamtof.content.domain.model.entity.Categories;
import cn.dreamtof.content.domain.repository.CategoriesRepository;
import cn.dreamtof.content.infrastructure.persistence.mapper.CategoriesMapper;
import cn.dreamtof.content.infrastructure.persistence.po.CategoriesPO;
import cn.dreamtof.content.infrastructure.persistence.po.table.CategoriesTableDef;
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
public class CategoriesRepositoryImpl extends ServiceImpl<CategoriesMapper, CategoriesPO> implements CategoriesRepository {

    private final CategoriesAssembler assembler;

    private static final CategoriesTableDef T = CategoriesTableDef.CATEGORIES_PO;

    @Override
    public Categories create(Categories entity) {
        CategoriesPO po = assembler.toPO(entity);
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
    public Categories update(Categories entity) {
        CategoriesPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Categories getById(UUID id) {
        CategoriesPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Categories> listAll() {
        List<CategoriesPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Categories> page(PageReq pageReq) {
        Page<CategoriesPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.SORT_ORDER.asc());
        Page<CategoriesPO> resultPage = super.page(flexPage, qw);
        List<Categories> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Categories> entities) {
        List<CategoriesPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<Categories> listByIds(List<UUID> ids) {
        List<CategoriesPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Categories> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<CategoriesPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<CategoriesPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public Categories findBySlug(String slug) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.SLUG.eq(slug));
        qw.limit(1);
        CategoriesPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }

    @Override
    public List<Categories> listByParentId(UUID parentId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.PARENT_ID.eq(parentId));
        qw.orderBy(T.SORT_ORDER.asc());
        List<CategoriesPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<Categories> listEnabled() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.HAS_ENABLED.eq(true));
        qw.orderBy(T.SORT_ORDER.asc());
        List<CategoriesPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }
}
