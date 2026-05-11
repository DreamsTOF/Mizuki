package cn.dreamtof.device.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.device.domain.model.entity.Devices;
import cn.dreamtof.device.domain.repository.DevicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevicesAppService {

    private final DevicesRepository repository;

    public Devices create(Devices entity) {
        return repository.create(entity);
    }

    public Devices update(Devices entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public Devices getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<Devices> listAll() {
        return repository.listAll();
    }

    public PageResult<Devices> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<Devices> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<Devices> listByCategoryId(UUID categoryId) {
        return repository.listByCategoryId(categoryId);
    }
}
