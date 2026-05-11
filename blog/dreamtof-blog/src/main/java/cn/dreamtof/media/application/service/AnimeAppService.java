package cn.dreamtof.media.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.Anime;
import cn.dreamtof.media.domain.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimeAppService {

    private final AnimeRepository repository;

    public Anime create(Anime entity) {
        return repository.create(entity);
    }

    public Anime update(Anime entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public Anime getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<Anime> listAll() {
        return repository.listAll();
    }

    public PageResult<Anime> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<Anime> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<Anime> listByStatus(String status) {
        return repository.listByStatus(status);
    }
}
