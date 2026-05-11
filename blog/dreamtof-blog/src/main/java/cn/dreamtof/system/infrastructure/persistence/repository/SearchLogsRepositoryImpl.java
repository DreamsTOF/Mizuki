package cn.dreamtof.system.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.application.assembler.SearchLogsAssembler;
import cn.dreamtof.system.domain.model.entity.SearchLogs;
import cn.dreamtof.system.domain.repository.SearchLogsRepository;
import cn.dreamtof.system.infrastructure.persistence.mapper.SearchLogsMapper;
import cn.dreamtof.system.infrastructure.persistence.po.SearchLogsPO;
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
public class SearchLogsRepositoryImpl extends ServiceImpl<SearchLogsMapper, SearchLogsPO> implements SearchLogsRepository {

    private final SearchLogsAssembler assembler;

    @Override
    public SearchLogs create(SearchLogs entity) {
        SearchLogsPO po = assembler.toPO(entity);
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
    public SearchLogs update(SearchLogs entity) {
        SearchLogsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public SearchLogs getById(UUID id) {
        SearchLogsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<SearchLogs> listAll() {
        List<SearchLogsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<SearchLogs> page(PageReq pageReq) {
        Page<SearchLogsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<SearchLogsPO> resultPage = super.page(flexPage, queryWrapper);
        List<SearchLogs> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<SearchLogs> entities) {
        List<SearchLogsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(SearchLogsPO::getId).eq(id).exists();
    }

    @Override
    public List<SearchLogs> listByIds(List<UUID> ids) {
        List<SearchLogsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<SearchLogs> seek(UUID cursor, int limit) {
        List<SearchLogsPO> poList = queryChain()
                .where(SearchLogsPO::getId).gt(cursor)
                .orderBy(SearchLogsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<SearchLogsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<Object[]> getHotKeywords(int limit) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.select("keyword, COUNT(*) as cnt");
        queryWrapper.groupBy("keyword");
        queryWrapper.orderBy("cnt", false);
        queryWrapper.limit(limit);
        return super.listAs(queryWrapper, Object[].class);
    }
}
