package cn.dreamtof.portfolio.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.portfolio.domain.model.entity.Skills;

import java.util.List;
import java.util.UUID;

public interface SkillsRepository {

    Skills create(Skills entity);

    boolean removeById(UUID id);

    Skills update(Skills entity);

    Skills getById(UUID id);

    List<Skills> listAll();

    PageResult<Skills> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Skills> entities);

    boolean existsById(UUID id);

    List<Skills> listByIds(List<UUID> ids);

    CursorResult<Skills> seek(UUID cursor, int limit);

    List<Skills> listByCategory(String category);

    List<String> listCategories();
}
