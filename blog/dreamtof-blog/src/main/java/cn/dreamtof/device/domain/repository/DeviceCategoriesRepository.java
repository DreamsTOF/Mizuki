package cn.dreamtof.device.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.device.domain.model.entity.DeviceCategories;

import java.util.List;
import java.util.UUID;

public interface DeviceCategoriesRepository {

    DeviceCategories create(DeviceCategories entity);

    boolean removeById(UUID id);

    DeviceCategories update(DeviceCategories entity);

    DeviceCategories getById(UUID id);

    List<DeviceCategories> listAll();

    PageResult<DeviceCategories> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<DeviceCategories> entities);

    boolean existsById(UUID id);

    List<DeviceCategories> listByIds(List<UUID> ids);

    CursorResult<DeviceCategories> seek(UUID cursor, int limit);
}
