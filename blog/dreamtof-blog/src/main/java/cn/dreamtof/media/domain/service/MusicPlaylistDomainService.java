package cn.dreamtof.media.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.media.domain.model.entity.MusicPlaylists;
import cn.dreamtof.media.domain.repository.MusicPlaylistsRepository;
import cn.dreamtof.media.domain.repository.MusicTracksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicPlaylistDomainService {

    private final MusicPlaylistsRepository playlistsRepository;
    private final MusicTracksRepository tracksRepository;

    public MusicPlaylists createPlaylist(MusicPlaylists entity) {
        Asserts.notNull(entity, "播放列表实体不能为空");
        Asserts.notBlank(entity.getName(), "播放列表名称不能为空");
        return playlistsRepository.create(entity);
    }

    public MusicPlaylists updatePlaylist(MusicPlaylists entity) {
        Asserts.notNull(entity, "播放列表实体不能为空");
        Asserts.notNull(entity.getId(), "播放列表ID不能为空");
        MusicPlaylists existing = playlistsRepository.getById(entity.getId());
        Asserts.notNull(existing, "播放列表不存在");
        return playlistsRepository.update(entity);
    }

    public boolean deletePlaylist(UUID id) {
        MusicPlaylists existing = playlistsRepository.getById(id);
        Asserts.notNull(existing, "播放列表不存在");
        tracksRepository.removeByPlaylistId(id);
        boolean removed = playlistsRepository.removeById(id);
        log.info("播放列表删除完成, playlistId={}", id);
        return removed;
    }

    public MusicPlaylists toggleEnabled(UUID id, boolean enabled) {
        MusicPlaylists playlist = playlistsRepository.getById(id);
        Asserts.notNull(playlist, "播放列表不存在");
        playlist.setHasEnabled(enabled);
        return playlistsRepository.update(playlist);
    }

    public MusicPlaylists getById(UUID id) {
        MusicPlaylists entity = playlistsRepository.getById(id);
        Asserts.notNull(entity, "播放列表不存在");
        return entity;
    }

    public List<MusicPlaylists> listEnabled() {
        return playlistsRepository.listEnabled();
    }

    public List<MusicPlaylists> listAll() {
        return playlistsRepository.listAll();
    }
}
