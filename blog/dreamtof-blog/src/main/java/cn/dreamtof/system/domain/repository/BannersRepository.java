package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.Banners;

import java.util.List;
import java.util.UUID;

public interface BannersRepository {

    Banners create(Banners entity);

    boolean removeById(UUID id);

    Banners update(Banners entity);

    Banners getById(UUID id);

    List<Banners> listAll();

    PageResult<Banners> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Banners> entities);

    boolean existsById(UUID id);

    List<Banners> listByIds(List<UUID> ids);

    List<Banners> listByPosition(String position);

    List<Banners> listEnabled();

    List<Banners> listCarousel();
}
