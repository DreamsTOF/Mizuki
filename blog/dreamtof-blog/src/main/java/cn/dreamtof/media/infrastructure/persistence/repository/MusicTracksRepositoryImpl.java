package cn.dreamtof.media.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.application.assembler.MusicTracksAssembler;
import cn.dreamtof.media.domain.model.entity.MusicTracks;
import cn.dreamtof.media.domain.repository.MusicTracksRepository;
import cn.dreamtof.media.infrastructure.persistence.mapper.MusicTracksMapper;
import cn.dreamtof.media.infrastructure.persistence.po.MusicTracksPO;
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
public class MusicTracksRepositoryImpl extends ServiceImpl<MusicTracksMapper, MusicTracksPO> implements MusicTracksRepository {

    private final MusicTracksAssembler assembler;

    @Override
    public MusicTracks create(MusicTracks entity) {
        MusicTracksPO po = assembler.toPO(entity);
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
    public MusicTracks update(MusicTracks entity) {
        MusicTracksPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public MusicTracks getById(UUID id) {
        MusicTracksPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<MusicTracks> listAll() {
        List<MusicTracksPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<MusicTracks> page(PageReq pageReq) {
        Page<MusicTracksPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<MusicTracksPO> resultPage = super.page(flexPage, queryWrapper);
        List<MusicTracks> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<MusicTracks> entities) {
        List<MusicTracksPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(MusicTracksPO::getId).eq(id).exists();
    }

    @Override
    public List<MusicTracks> listByIds(List<UUID> ids) {
        List<MusicTracksPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<MusicTracks> seek(UUID cursor, int limit) {
        List<MusicTracksPO> poList = queryChain()
                .where(MusicTracksPO::getId).gt(cursor)
                .orderBy(MusicTracksPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<MusicTracksPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<MusicTracks> listByPlaylistId(UUID playlistId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(MusicTracksPO::getPlaylistId).eq(playlistId);
        queryWrapper.orderBy(MusicTracksPO::getSortOrder).asc();
        List<MusicTracksPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }

    @Override
    public void removeByPlaylistId(UUID playlistId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(MusicTracksPO::getPlaylistId).eq(playlistId);
        super.remove(queryWrapper);
    }

    @Override
    public long countByPlaylistId(UUID playlistId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(MusicTracksPO::getPlaylistId).eq(playlistId);
        return super.count(queryWrapper);
    }
}
