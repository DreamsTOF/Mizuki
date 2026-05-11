package cn.dreamtof.media.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.application.assembler.AlbumPhotosAssembler;
import cn.dreamtof.media.domain.model.entity.AlbumPhotos;
import cn.dreamtof.media.domain.repository.AlbumPhotosRepository;
import cn.dreamtof.media.infrastructure.persistence.mapper.AlbumPhotosMapper;
import cn.dreamtof.media.infrastructure.persistence.po.AlbumPhotosPO;
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
public class AlbumPhotosRepositoryImpl extends ServiceImpl<AlbumPhotosMapper, AlbumPhotosPO> implements AlbumPhotosRepository {

    private final AlbumPhotosAssembler assembler;

    @Override
    public AlbumPhotos create(AlbumPhotos entity) {
        AlbumPhotosPO po = assembler.toPO(entity);
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
    public AlbumPhotos update(AlbumPhotos entity) {
        AlbumPhotosPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public AlbumPhotos getById(UUID id) {
        AlbumPhotosPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<AlbumPhotos> listAll() {
        List<AlbumPhotosPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<AlbumPhotos> page(PageReq pageReq) {
        Page<AlbumPhotosPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<AlbumPhotosPO> resultPage = super.page(flexPage, queryWrapper);
        List<AlbumPhotos> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<AlbumPhotos> entities) {
        List<AlbumPhotosPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(AlbumPhotosPO::getId).eq(id).exists();
    }

    @Override
    public List<AlbumPhotos> listByIds(List<UUID> ids) {
        List<AlbumPhotosPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<AlbumPhotos> seek(UUID cursor, int limit) {
        List<AlbumPhotosPO> poList = queryChain()
                .where(AlbumPhotosPO::getId).gt(cursor)
                .orderBy(AlbumPhotosPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<AlbumPhotosPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<AlbumPhotos> listByAlbumId(UUID albumId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(AlbumPhotosPO::getAlbumId).eq(albumId);
        queryWrapper.orderBy(AlbumPhotosPO::getCreatedAt).asc();
        List<AlbumPhotosPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }

    @Override
    public void removeByAlbumId(UUID albumId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(AlbumPhotosPO::getAlbumId).eq(albumId);
        super.remove(queryWrapper);
    }
}
