package cn.dreamtof.media.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.application.assembler.AnimeAssembler;
import cn.dreamtof.media.domain.model.entity.Anime;
import cn.dreamtof.media.domain.repository.AnimeRepository;
import cn.dreamtof.media.infrastructure.persistence.mapper.AnimeMapper;
import cn.dreamtof.media.infrastructure.persistence.po.AnimePO;
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
public class AnimeRepositoryImpl extends ServiceImpl<AnimeMapper, AnimePO> implements AnimeRepository {

    private final AnimeAssembler assembler;

    @Override
    public Anime create(Anime entity) {
        AnimePO po = assembler.toPO(entity);
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
    public Anime update(Anime entity) {
        AnimePO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Anime getById(UUID id) {
        AnimePO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Anime> listAll() {
        List<AnimePO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Anime> page(PageReq pageReq) {
        Page<AnimePO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<AnimePO> resultPage = super.page(flexPage, queryWrapper);
        List<Anime> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Anime> entities) {
        List<AnimePO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(AnimePO::getId).eq(id).exists();
    }

    @Override
    public List<Anime> listByIds(List<UUID> ids) {
        List<AnimePO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Anime> seek(UUID cursor, int limit) {
        List<AnimePO> poList = queryChain()
                .where(AnimePO::getId).gt(cursor)
                .orderBy(AnimePO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<AnimePO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<Anime> listByStatus(String status) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(AnimePO::getStatus).eq(status);
        List<AnimePO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }
}
