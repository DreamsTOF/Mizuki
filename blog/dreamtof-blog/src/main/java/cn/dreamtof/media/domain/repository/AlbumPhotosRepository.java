package cn.dreamtof.media.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.AlbumPhotos;

import java.util.List;
import java.util.UUID;

public interface AlbumPhotosRepository {

    AlbumPhotos create(AlbumPhotos entity);

    boolean removeById(UUID id);

    AlbumPhotos update(AlbumPhotos entity);

    AlbumPhotos getById(UUID id);

    List<AlbumPhotos> listAll();

    PageResult<AlbumPhotos> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<AlbumPhotos> entities);

    boolean existsById(UUID id);

    List<AlbumPhotos> listByIds(List<UUID> ids);

    CursorResult<AlbumPhotos> seek(UUID cursor, int limit);

    List<AlbumPhotos> listByAlbumId(UUID albumId);

    void removeByAlbumId(UUID albumId);
}
