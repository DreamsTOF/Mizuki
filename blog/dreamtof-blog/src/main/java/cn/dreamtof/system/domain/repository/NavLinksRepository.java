package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.NavLinks;

import java.util.List;
import java.util.UUID;

public interface NavLinksRepository {

    NavLinks create(NavLinks entity);

    boolean removeById(UUID id);

    NavLinks update(NavLinks entity);

    NavLinks getById(UUID id);

    List<NavLinks> listAll();

    PageResult<NavLinks> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<NavLinks> entities);

    boolean existsById(UUID id);

    List<NavLinks> listByIds(List<UUID> ids);

    List<NavLinks> listByPosition(String position);

    List<NavLinks> listByParentId(UUID parentId);

    List<NavLinks> listEnabled();
}
