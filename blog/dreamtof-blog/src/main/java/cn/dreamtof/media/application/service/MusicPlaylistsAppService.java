package cn.dreamtof.media.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.MusicPlaylists;
import cn.dreamtof.media.domain.repository.MusicPlaylistsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicPlaylistsAppService {

    private final MusicPlaylistsRepository repository;

    public MusicPlaylists create(MusicPlaylists entity) {
        return repository.create(entity);
    }

    public MusicPlaylists update(MusicPlaylists entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public MusicPlaylists getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<MusicPlaylists> listAll() {
        return repository.listAll();
    }

    public PageResult<MusicPlaylists> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<MusicPlaylists> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<MusicPlaylists> listEnabled() {
        return repository.listEnabled();
    }
}
