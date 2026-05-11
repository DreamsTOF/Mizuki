package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.PageViewsAssembler;
import cn.dreamtof.system.domain.model.entity.PageViews;
import cn.dreamtof.system.domain.repository.PageViewsRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.PageViewsMapper;
import cn.dreamtof.system.infrastructure.persistence.po.PageViewsPO;
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
public class PageViewsRepositoryImpl extends ServiceImpl<PageViewsMapper, PageViewsPO> implements PageViewsRepository {

    private final PageViewsAssembler assembler;

    @Override
    public PageViews create(PageViews entity) {
        PageViewsPO po = assembler.toPO(entity);
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
    public PageViews update(PageViews entity) {
        PageViewsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public PageViews getById(UUID id) {
        PageViewsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<PageViews> listAll() {
        List<PageViewsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<PageViews> page(PageReq pageReq) {
        Page<PageViewsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<PageViewsPO> resultPage = super.page(flexPage, queryWrapper);
        List<PageViews> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<PageViews> entities) {
        List<PageViewsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(PageViewsPO::getId).eq(id).exists();
    }

    @Override
    public List<PageViews> listByIds(List<UUID> ids) {
        List<PageViewsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<PageViews> seek(UUID cursor, int limit) {
        List<PageViewsPO> poList = queryChain()
                .where(PageViewsPO::getId).gt(cursor)
                .orderBy(PageViewsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<PageViewsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public long countByPagePath(String pagePath) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(PageViewsPO::getPagePath).eq(pagePath);
        return super.count(queryWrapper);
    }

    @Override
    public List<PageViews> listTopPages(int limit) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.groupBy(PageViewsPO::getPagePath);
        queryWrapper.orderBy("COUNT(*)", false);
        queryWrapper.limit(limit);
        List<PageViewsPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }
}
