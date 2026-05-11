package cn.dreamtof.device.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.device.domain.model.entity.DeviceCategories;
import cn.dreamtof.device.domain.model.entity.Devices;
import cn.dreamtof.device.domain.repository.DeviceCategoriesRepository;
import cn.dreamtof.device.domain.repository.DevicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceDomainService {

    private final DevicesRepository devicesRepository;
    private final DeviceCategoriesRepository categoriesRepository;

    public Devices createDevice(Devices entity) {
        Asserts.notNull(entity, "设备实体不能为空");
        Asserts.notBlank(entity.getName(), "设备名称不能为空");
        if (entity.getCategoryId() != null) {
            DeviceCategories category = categoriesRepository.getById(entity.getCategoryId());
            Asserts.notNull(category, "设备分类不存在");
        }
        return devicesRepository.create(entity);
    }

    public Devices updateDevice(Devices entity) {
        Asserts.notNull(entity, "设备实体不能为空");
        Asserts.notNull(entity.getId(), "设备ID不能为空");
        Devices existing = devicesRepository.getById(entity.getId());
        Asserts.notNull(existing, "设备不存在");
        return devicesRepository.update(entity);
    }

    public boolean deleteDevice(UUID id) {
        Devices existing = devicesRepository.getById(id);
        Asserts.notNull(existing, "设备不存在");
        boolean removed = devicesRepository.removeById(id);
        log.info("设备删除完成, deviceId={}", id);
        return removed;
    }

    public Devices getById(UUID id) {
        Devices entity = devicesRepository.getById(id);
        Asserts.notNull(entity, "设备不存在");
        return entity;
    }

    public List<Devices> listByCategory(UUID categoryId) {
        return devicesRepository.listByCategoryId(categoryId);
    }

    public List<Devices> listAll() {
        return devicesRepository.listAll();
    }
}
