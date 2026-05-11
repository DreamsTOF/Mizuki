package cn.dreamtof.device.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.device.domain.model.entity.Devices;

import java.util.List;
import java.util.UUID;

public interface DevicesRepository {

    Devices create(Devices entity);

    boolean removeById(UUID id);

    Devices update(Devices entity);

    Devices getById(UUID id);

    List<Devices> listAll();

    PageResult<Devices> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Devices> entities);

    boolean existsById(UUID id);

    List<Devices> listByIds(List<UUID> ids);

    CursorResult<Devices> seek(UUID cursor, int limit);

    List<Devices> listByCategoryId(UUID categoryId);

    long countByCategoryId(UUID categoryId);
}
