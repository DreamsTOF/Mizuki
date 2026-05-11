package cn.dreamtof.system.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.PageViews;
import cn.dreamtof.system.domain.repository.PageViewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageViewsAppService {

    private final PageViewsRepository repository;

    public PageViews create(PageViews entity) {
        return repository.create(entity);
    }

    public PageViews update(PageViews entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public PageViews getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<PageViews> listAll() {
        return repository.listAll();
    }

    public PageResult<PageViews> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<PageViews> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }
}
