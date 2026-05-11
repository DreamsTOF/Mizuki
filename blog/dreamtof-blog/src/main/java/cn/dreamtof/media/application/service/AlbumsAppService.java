package cn.dreamtof.media.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.media.domain.model.entity.Albums;
import cn.dreamtof.media.domain.repository.AlbumsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumsAppService {

    private final AlbumsRepository repository;

    public Albums create(Albums entity) {
        return repository.create(entity);
    }

    public Albums update(Albums entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public Albums getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<Albums> listAll() {
        return repository.listAll();
    }

    public PageResult<Albums> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<Albums> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }
}
