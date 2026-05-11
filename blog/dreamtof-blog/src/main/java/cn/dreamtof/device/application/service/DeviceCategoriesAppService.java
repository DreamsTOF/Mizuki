package cn.dreamtof.device.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.device.domain.model.entity.DeviceCategories;
import cn.dreamtof.device.domain.repository.DeviceCategoriesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceCategoriesAppService {

    private final DeviceCategoriesRepository repository;

    public DeviceCategories create(DeviceCategories entity) {
        return repository.create(entity);
    }

    public DeviceCategories update(DeviceCategories entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public DeviceCategories getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<DeviceCategories> listAll() {
        return repository.listAll();
    }

    public PageResult<DeviceCategories> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<DeviceCategories> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }
}
