package cn.dreamtof.content.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.model.entity.Tags;

import java.util.List;
import java.util.UUID;

public interface TagsRepository {

    Tags create(Tags entity);

    boolean removeById(UUID id);

    Tags update(Tags entity);

    Tags getById(UUID id);

    List<Tags> listAll();

    PageResult<Tags> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Tags> entities);

    boolean existsById(UUID id);

    List<Tags> listByIds(List<UUID> ids);

    CursorResult<Tags> seek(UUID cursor, int limit);

    Tags findBySlug(String slug);

    Tags findByName(String name);
}
