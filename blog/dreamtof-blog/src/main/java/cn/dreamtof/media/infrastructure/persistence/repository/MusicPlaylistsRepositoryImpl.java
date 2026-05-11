package cn.dreamtof.media.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.application.assembler.MusicPlaylistsAssembler;
import cn.dreamtof.media.domain.model.entity.MusicPlaylists;
import cn.dreamtof.media.domain.repository.MusicPlaylistsRepository;
import cn.dreamtof.media.infrastructure.persistence.mapper.MusicPlaylistsMapper;
import cn.dreamtof.media.infrastructure.persistence.po.MusicPlaylistsPO;
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
public class MusicPlaylistsRepositoryImpl extends ServiceImpl<MusicPlaylistsMapper, MusicPlaylistsPO> implements MusicPlaylistsRepository {

    private final MusicPlaylistsAssembler assembler;

    @Override
    public MusicPlaylists create(MusicPlaylists entity) {
        MusicPlaylistsPO po = assembler.toPO(entity);
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
    public MusicPlaylists update(MusicPlaylists entity) {
        MusicPlaylistsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public MusicPlaylists getById(UUID id) {
        MusicPlaylistsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<MusicPlaylists> listAll() {
        List<MusicPlaylistsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<MusicPlaylists> page(PageReq pageReq) {
        Page<MusicPlaylistsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<MusicPlaylistsPO> resultPage = super.page(flexPage, queryWrapper);
        List<MusicPlaylists> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<MusicPlaylists> entities) {
        List<MusicPlaylistsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(MusicPlaylistsPO::getId).eq(id).exists();
    }

    @Override
    public List<MusicPlaylists> listByIds(List<UUID> ids) {
        List<MusicPlaylistsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<MusicPlaylists> seek(UUID cursor, int limit) {
        List<MusicPlaylistsPO> poList = queryChain()
                .where(MusicPlaylistsPO::getId).gt(cursor)
                .orderBy(MusicPlaylistsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<MusicPlaylistsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<MusicPlaylists> listEnabled() {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(MusicPlaylistsPO::getHasEnabled).eq(true);
        List<MusicPlaylistsPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }
}
