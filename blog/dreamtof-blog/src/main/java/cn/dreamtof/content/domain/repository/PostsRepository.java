package cn.dreamtof.content.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.model.entity.Posts;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PostsRepository {

    Posts create(Posts entity);

    boolean removeById(UUID id);

    Posts update(Posts entity);

    Posts getById(UUID id);

    List<Posts> listAll();

    PageResult<Posts> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<Posts> entities);

    boolean existsById(UUID id);

    List<Posts> listByIds(List<UUID> ids);

    CursorResult<Posts> seek(UUID cursor, int limit);

    Posts findBySlug(String slug);

    List<Posts> listPublished();

    List<Posts> listByCategory(String category);

    PageResult<Posts> searchByKeyword(String keyword, PageReq pageReq);

    long incrementViewCount(UUID id);
}
