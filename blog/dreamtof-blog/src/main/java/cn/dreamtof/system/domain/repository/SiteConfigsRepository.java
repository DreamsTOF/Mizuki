package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.SiteConfigs;

import java.util.List;
import java.util.UUID;

public interface SiteConfigsRepository {

    SiteConfigs create(SiteConfigs entity);

    boolean removeById(UUID id);

    SiteConfigs update(SiteConfigs entity);

    SiteConfigs getById(UUID id);

    List<SiteConfigs> listAll();

    PageResult<SiteConfigs> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<SiteConfigs> entities);

    boolean existsById(UUID id);

    List<SiteConfigs> listByIds(List<UUID> ids);

    CursorResult<SiteConfigs> seek(UUID cursor, int limit);

    SiteConfigs findByKey(String configKey);

    List<SiteConfigs> listByKeyPrefix(String keyPrefix);
}
