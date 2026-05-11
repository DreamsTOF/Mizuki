package cn.dreamtof.device.domain.service;

import cn.dreamtof.core.exception.Asserts;
import cn.dreamtof.device.domain.model.entity.DeviceCategories;
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
public class DeviceCategoryDomainService {

    private final DeviceCategoriesRepository categoriesRepository;
    private final DevicesRepository devicesRepository;

    public DeviceCategories createCategory(DeviceCategories entity) {
        Asserts.notNull(entity, "分类实体不能为空");
        Asserts.notBlank(entity.getName(), "分类名称不能为空");
        return categoriesRepository.create(entity);
    }

    public DeviceCategories updateCategory(DeviceCategories entity) {
        Asserts.notNull(entity, "分类实体不能为空");
        Asserts.notNull(entity.getId(), "分类ID不能为空");
        DeviceCategories existing = categoriesRepository.getById(entity.getId());
        Asserts.notNull(existing, "分类不存在");
        return categoriesRepository.update(entity);
    }

    public boolean deleteCategory(UUID id) {
        DeviceCategories existing = categoriesRepository.getById(id);
        Asserts.notNull(existing, "分类不存在");
        long deviceCount = devicesRepository.countByCategoryId(id);
        Asserts.isTrue(deviceCount == 0, "该分类下存在设备，无法删除");
        boolean removed = categoriesRepository.removeById(id);
        log.info("设备分类删除完成, categoryId={}", id);
        return removed;
    }

    public DeviceCategories getById(UUID id) {
        DeviceCategories entity = categoriesRepository.getById(id);
        Asserts.notNull(entity, "分类不存在");
        return entity;
    }

    public List<DeviceCategories> listAll() {
        return categoriesRepository.listAll();
    }
}
