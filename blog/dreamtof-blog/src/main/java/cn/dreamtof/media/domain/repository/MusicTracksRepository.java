package cn.dreamtof.media.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.MusicTracks;

import java.util.List;
import java.util.UUID;

public interface MusicTracksRepository {

    MusicTracks create(MusicTracks entity);

    boolean removeById(UUID id);

    MusicTracks update(MusicTracks entity);

    MusicTracks getById(UUID id);

    List<MusicTracks> listAll();

    PageResult<MusicTracks> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<MusicTracks> entities);

    boolean existsById(UUID id);

    List<MusicTracks> listByIds(List<UUID> ids);

    CursorResult<MusicTracks> seek(UUID cursor, int limit);

    List<MusicTracks> listByPlaylistId(UUID playlistId);

    void removeByPlaylistId(UUID playlistId);

    long countByPlaylistId(UUID playlistId);
}
