package cn.dreamtof.media.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.media.domain.model.entity.AlbumPhotos;
import cn.dreamtof.media.domain.model.entity.Albums;
import cn.dreamtof.media.domain.repository.AlbumPhotosRepository;
import cn.dreamtof.media.domain.repository.AlbumsRepository;
import cn.dreamtof.core.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumDomainService {

    private final AlbumsRepository albumsRepository;
    private final AlbumPhotosRepository photosRepository;

    public Albums createAlbum(Albums entity) {
        Asserts.notNull(entity, "相册实体不能为空");
        Asserts.notBlank(entity.getTitle(), "相册标题不能为空");
        return albumsRepository.create(entity);
    }

    public Albums updateAlbum(Albums entity) {
        Asserts.notNull(entity, "相册实体不能为空");
        Asserts.notNull(entity.getId(), "相册ID不能为空");
        Albums existing = albumsRepository.getById(entity.getId());
        Asserts.notNull(existing, "相册不存在");
        return albumsRepository.update(entity);
    }

    public boolean deleteAlbum(UUID id) {
        Albums existing = albumsRepository.getById(id);
        Asserts.notNull(existing, "相册不存在");
        photosRepository.removeByAlbumId(id);
        boolean removed = albumsRepository.removeById(id);
        log.info("相册删除完成, albumId={}", id);
        return removed;
    }

    public Albums setCoverPhoto(UUID albumId, UUID photoId) {
        Albums album = albumsRepository.getById(albumId);
        Asserts.notNull(album, "相册不存在");
        AlbumPhotos photo = photosRepository.getById(photoId);
        Asserts.notNull(photo, "图片不存在");
        Asserts.isTrue(photo.getAlbumId().equals(albumId), "图片不属于该相册");
        album.setCover(photo.getUrl());
        return albumsRepository.update(album);
    }

    public Albums getById(UUID id) {
        Albums entity = albumsRepository.getById(id);
        Asserts.notNull(entity, "相册不存在");
        return entity;
    }

    public List<Albums> listAll() {
        return albumsRepository.listAll();
    }

    public List<AlbumPhotos> listPhotosByAlbumId(UUID albumId) {
        return photosRepository.listByAlbumId(albumId);
    }
}
