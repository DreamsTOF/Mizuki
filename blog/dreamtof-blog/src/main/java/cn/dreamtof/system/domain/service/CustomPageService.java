package cn.dreamtof.system.domain.service;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.system.domain.model.entity.CustomPages;
import cn.dreamtof.system.domain.repository.CustomPagesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomPageService {

    private final CustomPagesRepository repository;

    public CustomPages create(CustomPages entity) {
        CustomPages existing = repository.findByPageKey(entity.getPageKey());
        Asserts.isTrue(existing == null, "页面标识已存在: " + entity.getPageKey());
        return repository.create(entity);
    }

    public CustomPages update(CustomPages entity) {
        CustomPages existing = repository.getById(entity.getId());
        Asserts.notNull(existing, "自定义页面不存在");
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public CustomPages getById(UUID id) {
        CustomPages entity = repository.getById(id);
        Asserts.notNull(entity, "自定义页面不存在");
        return entity;
    }

    public CustomPages getByPageKey(String pageKey) {
        return repository.findByPageKey(pageKey);
    }

    public List<CustomPages> listAll() {
        return repository.listAll();
    }

    public List<CustomPages> listEnabled() {
        return repository.listByEnabled(true);
    }

    public PageResult<CustomPages> page(PageReq pageReq) {
        return repository.page(pageReq);
    }
}
