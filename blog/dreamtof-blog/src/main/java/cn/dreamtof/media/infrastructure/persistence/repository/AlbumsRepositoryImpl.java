package cn.dreamtof.media.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.application.assembler.AlbumsAssembler;
import cn.dreamtof.media.domain.model.entity.Albums;
import cn.dreamtof.media.domain.repository.AlbumsRepository;
import cn.dreamtof.media.infrastructure.persistence.mapper.AlbumsMapper;
import cn.dreamtof.media.infrastructure.persistence.po.AlbumsPO;
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
public class AlbumsRepositoryImpl extends ServiceImpl<AlbumsMapper, AlbumsPO> implements AlbumsRepository {

    private final AlbumsAssembler assembler;

    @Override
    public Albums create(Albums entity) {
        AlbumsPO po = assembler.toPO(entity);
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
    public Albums update(Albums entity) {
        AlbumsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Albums getById(UUID id) {
        AlbumsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Albums> listAll() {
        List<AlbumsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Albums> page(PageReq pageReq) {
        Page<AlbumsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<AlbumsPO> resultPage = super.page(flexPage, queryWrapper);
        List<Albums> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Albums> entities) {
        List<AlbumsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(AlbumsPO::getId).eq(id).exists();
    }

    @Override
    public List<Albums> listByIds(List<UUID> ids) {
        List<AlbumsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Albums> seek(UUID cursor, int limit) {
        List<AlbumsPO> poList = queryChain()
                .where(AlbumsPO::getId).gt(cursor)
                .orderBy(AlbumsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<AlbumsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }
}
