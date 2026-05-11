package cn.dreamtof.media.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.media.domain.model.entity.AlbumPhotos;
import cn.dreamtof.media.domain.model.entity.Albums;
import cn.dreamtof.media.domain.repository.AlbumPhotosRepository;
import cn.dreamtof.media.domain.repository.AlbumsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumPhotoService {

    private final AlbumPhotosRepository photosRepository;
    private final AlbumsRepository albumsRepository;

    public List<AlbumPhotos> batchUpload(UUID albumId, List<AlbumPhotos> photos) {
        Asserts.notNull(albumId, "相册ID不能为空");
        Albums album = albumsRepository.getById(albumId);
        Asserts.notNull(album, "相册不存在");
        List<AlbumPhotos> created = new ArrayList<>(photos.size());
        for (AlbumPhotos photo : photos) {
            photo.setAlbumId(albumId);
            created.add(photosRepository.create(photo));
        }
        log.info("批量上传相册图片完成, albumId={}, count={}", albumId, created.size());
        return created;
    }

    public boolean deletePhoto(UUID photoId) {
        return photosRepository.removeById(photoId);
    }
}
