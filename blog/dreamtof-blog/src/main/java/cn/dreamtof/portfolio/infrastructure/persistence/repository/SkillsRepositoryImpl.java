package cn.dreamtof.portfolio.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.application.assembler.SkillsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.Skills;
import cn.dreamtof.portfolio.domain.repository.SkillsRepository;
import cn.dreamtof.portfolio.infrastructure.persistence.mapper.SkillsMapper;
import cn.dreamtof.portfolio.infrastructure.persistence.po.SkillsPO;
import cn.dreamtof.portfolio.infrastructure.persistence.po.table.SkillsTableDef;
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
public class SkillsRepositoryImpl extends ServiceImpl<SkillsMapper, SkillsPO> implements SkillsRepository {

    private final SkillsAssembler assembler;
    private static final SkillsTableDef T = SkillsTableDef.SKILLS_PO;

    @Override
    public Skills create(Skills entity) {
        SkillsPO po = assembler.toPO(entity);
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
    public Skills update(Skills entity) {
        SkillsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Skills getById(UUID id) {
        SkillsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Skills> listAll() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.CATEGORY.asc());
        qw.orderBy(T.NAME.asc());
        List<SkillsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Skills> page(PageReq pageReq) {
        Page<SkillsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.CATEGORY.asc());
        qw.orderBy(T.NAME.asc());
        Page<SkillsPO> resultPage = super.page(flexPage, qw);
        List<Skills> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Skills> entities) {
        List<SkillsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<Skills> listByIds(List<UUID> ids) {
        List<SkillsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Skills> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<SkillsPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<SkillsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<Skills> listByCategory(String category) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.CATEGORY.eq(category));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.NAME.asc());
        List<SkillsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<String> listCategories() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.select(T.CATEGORY);
        qw.groupBy(T.CATEGORY);
        List<SkillsPO> poList = super.list(qw);
        List<String> categories = new ArrayList<>(poList.size());
        for (SkillsPO po : poList) {
            if (po.getCategory() != null) {
                categories.add(po.getCategory());
            }
        }
        return categories;
    }
}
