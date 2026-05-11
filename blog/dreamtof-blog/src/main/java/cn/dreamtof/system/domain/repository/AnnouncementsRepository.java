package cn.dreamtof.system.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.system.domain.model.entity.Announcements;

import java.util.List;
import java.util.UUID;

public interface AnnouncementsRepository {

    Announcements create(Announcements entity);

    boolean removeById(UUID id);

    Announcements update(Announcements entity);

    Announcements getById(UUID id);

    List<Announcements> listAll();

    PageResult<Announcements> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Announcements> entities);

    boolean existsById(UUID id);

    List<Announcements> listByIds(List<UUID> ids);

    CursorResult<Announcements> seek(UUID cursor, int limit);

    List<Announcements> listActive();
}
