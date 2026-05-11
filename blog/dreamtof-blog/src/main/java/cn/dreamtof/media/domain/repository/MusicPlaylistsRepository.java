package cn.dreamtof.media.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.MusicPlaylists;

import java.util.List;
import java.util.UUID;

public interface MusicPlaylistsRepository {

    MusicPlaylists create(MusicPlaylists entity);

    boolean removeById(UUID id);

    MusicPlaylists update(MusicPlaylists entity);

    MusicPlaylists getById(UUID id);

    List<MusicPlaylists> listAll();

    PageResult<MusicPlaylists> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<MusicPlaylists> entities);

    boolean existsById(UUID id);

    List<MusicPlaylists> listByIds(List<UUID> ids);

    CursorResult<MusicPlaylists> seek(UUID cursor, int limit);

    List<MusicPlaylists> listEnabled();
}
