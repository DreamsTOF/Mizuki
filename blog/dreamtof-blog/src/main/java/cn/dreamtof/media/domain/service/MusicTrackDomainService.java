package cn.dreamtof.media.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.media.domain.model.entity.MusicPlaylists;
import cn.dreamtof.media.domain.model.entity.MusicTracks;
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
public class MusicTrackDomainService {

    private final MusicTracksRepository tracksRepository;
    private final MusicPlaylistsRepository playlistsRepository;

    public MusicTracks createTrack(MusicTracks entity) {
        Asserts.notNull(entity, "曲目实体不能为空");
        Asserts.notNull(entity.getPlaylistId(), "所属播放列表ID不能为空");
        Asserts.notBlank(entity.getTitle(), "曲目名称不能为空");
        MusicPlaylists playlist = playlistsRepository.getById(entity.getPlaylistId());
        Asserts.notNull(playlist, "播放列表不存在");
        return tracksRepository.create(entity);
    }

    public MusicTracks updateTrack(MusicTracks entity) {
        Asserts.notNull(entity, "曲目实体不能为空");
        Asserts.notNull(entity.getId(), "曲目ID不能为空");
        MusicTracks existing = tracksRepository.getById(entity.getId());
        Asserts.notNull(existing, "曲目不存在");
        return tracksRepository.update(entity);
    }

    public boolean deleteTrack(UUID id) {
        return tracksRepository.removeById(id);
    }

    public void reorderTracks(UUID playlistId, List<UUID> trackIds) {
        Asserts.notNull(playlistId, "播放列表ID不能为空");
        List<MusicTracks> tracks = tracksRepository.listByPlaylistId(playlistId);
        for (int i = 0; i < trackIds.size(); i++) {
            UUID trackId = trackIds.get(i);
            for (MusicTracks track : tracks) {
                if (track.getId().equals(trackId)) {
                    track.setSortOrder(i);
                    tracksRepository.update(track);
                    break;
                }
            }
        }
        log.info("曲目重排序完成, playlistId={}", playlistId);
    }

    public List<MusicTracks> listByPlaylistId(UUID playlistId) {
        return tracksRepository.listByPlaylistId(playlistId);
    }
}
