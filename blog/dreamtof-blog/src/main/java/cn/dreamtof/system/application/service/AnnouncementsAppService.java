package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.Announcements;
import cn.dreamtof.system.domain.repository.AnnouncementsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementsAppService {

    private final AnnouncementsRepository repository;

    public Announcements create(Announcements entity) {
        return repository.create(entity);
    }

    public Announcements update(Announcements entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public Announcements getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<Announcements> listAll() {
        return repository.listAll();
    }

    public PageResult<Announcements> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<Announcements> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<Announcements> listActive() {
        return repository.listActive();
    }
}
