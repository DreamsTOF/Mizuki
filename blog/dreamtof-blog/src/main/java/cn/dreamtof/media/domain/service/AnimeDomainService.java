package cn.dreamtof.media.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.core.utils.DateUtils;
import cn.dreamtof.media.domain.model.entity.Anime;
import cn.dreamtof.media.domain.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimeDomainService {

    private final AnimeRepository animeRepository;

    public Anime createAnime(Anime entity) {
        Asserts.notNull(entity, "番剧实体不能为空");
        Asserts.notBlank(entity.getTitle(), "番剧标题不能为空");
        return animeRepository.create(entity);
    }

    public Anime updateAnime(Anime entity) {
        Asserts.notNull(entity, "番剧实体不能为空");
        Asserts.notNull(entity.getId(), "番剧ID不能为空");
        Anime existing = animeRepository.getById(entity.getId());
        Asserts.notNull(existing, "番剧不存在");
        return animeRepository.update(entity);
    }

    public boolean deleteAnime(UUID id) {
        Anime existing = animeRepository.getById(id);
        Asserts.notNull(existing, "番剧不存在");
        boolean removed = animeRepository.removeById(id);
        log.info("番剧删除完成, animeId={}", id);
        return removed;
    }

    public Anime updateProgress(UUID id, Integer progress) {
        Anime anime = animeRepository.getById(id);
        Asserts.notNull(anime, "番剧不存在");
        Asserts.notNull(progress, "进度不能为空");
        anime.setProgress(progress);
        return animeRepository.update(anime);
    }

    public Anime getById(UUID id) {
        Anime entity = animeRepository.getById(id);
        Asserts.notNull(entity, "番剧不存在");
        return entity;
    }

    public List<Anime> listByStatus(String status) {
        return animeRepository.listByStatus(status);
    }

    public List<Anime> listAll() {
        return animeRepository.listAll();
    }
}
