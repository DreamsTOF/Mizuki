package cn.dreamtof.portfolio.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.application.assembler.ProjectsAssembler;
import cn.dreamtof.portfolio.domain.model.entity.Projects;
import cn.dreamtof.portfolio.domain.repository.ProjectsRepository;
import cn.dreamtof.portfolio.infrastructure.persistence.mapper.ProjectsMapper;
import cn.dreamtof.portfolio.infrastructure.persistence.po.ProjectsPO;
import cn.dreamtof.portfolio.infrastructure.persistence.po.table.ProjectsTableDef;
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
public class ProjectsRepositoryImpl extends ServiceImpl<ProjectsMapper, ProjectsPO> implements ProjectsRepository {

    private final ProjectsAssembler assembler;
    private static final ProjectsTableDef T = ProjectsTableDef.PROJECTS_PO;

    @Override
    public Projects create(Projects entity) {
        ProjectsPO po = assembler.toPO(entity);
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
    public Projects update(Projects entity) {
        ProjectsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Projects getById(UUID id) {
        ProjectsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Projects> listAll() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        qw.orderBy(T.CREATED_AT.desc());
        List<ProjectsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Projects> page(PageReq pageReq) {
        Page<ProjectsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        qw.orderBy(T.CREATED_AT.desc());
        Page<ProjectsPO> resultPage = super.page(flexPage, qw);
        List<Projects> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Projects> entities) {
        List<ProjectsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<Projects> listByIds(List<UUID> ids) {
        List<ProjectsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Projects> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<ProjectsPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<ProjectsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<Projects> listByCategory(String category) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.CATEGORY.eq(category));
        qw.where(T.DELETED_AT.isNull());
        qw.orderBy(T.SORT_ORDER.asc());
        qw.orderBy(T.CREATED_AT.desc());
        List<ProjectsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<String> listCategories() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DELETED_AT.isNull());
        qw.select(T.CATEGORY);
        qw.groupBy(T.CATEGORY);
        List<ProjectsPO> poList = super.list(qw);
        List<String> categories = new ArrayList<>(poList.size());
        for (ProjectsPO po : poList) {
            if (po.getCategory() != null) {
                categories.add(po.getCategory());
            }
        }
        return categories;
    }
}
