package cn.dreamtof.media.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.AlbumPhotos;
import cn.dreamtof.media.domain.repository.AlbumPhotosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumPhotosAppService {

    private final AlbumPhotosRepository repository;

    public AlbumPhotos create(AlbumPhotos entity) {
        return repository.create(entity);
    }

    public AlbumPhotos update(AlbumPhotos entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public AlbumPhotos getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<AlbumPhotos> listAll() {
        return repository.listAll();
    }

    public PageResult<AlbumPhotos> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<AlbumPhotos> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<AlbumPhotos> listByAlbumId(UUID albumId) {
        return repository.listByAlbumId(albumId);
    }
}
