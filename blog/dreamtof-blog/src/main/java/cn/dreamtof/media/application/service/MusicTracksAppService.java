package cn.dreamtof.media.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.MusicTracks;
import cn.dreamtof.media.domain.repository.MusicTracksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicTracksAppService {

    private final MusicTracksRepository repository;

    public MusicTracks create(MusicTracks entity) {
        return repository.create(entity);
    }

    public MusicTracks update(MusicTracks entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public MusicTracks getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<MusicTracks> listAll() {
        return repository.listAll();
    }

    public PageResult<MusicTracks> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<MusicTracks> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<MusicTracks> listByPlaylistId(UUID playlistId) {
        return repository.listByPlaylistId(playlistId);
    }
}
