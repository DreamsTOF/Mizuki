package cn.dreamtof.content.domain.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.content.domain.model.entity.PostTags;

import java.util.List;
import java.util.UUID;

public interface PostTagsRepository {

    PostTags create(PostTags entity);

    boolean removeById(UUID id);

    PostTags update(PostTags entity);

    PostTags getById(UUID id);

    List<PostTags> listAll();

    PageResult<PostTags> page(PageReq pageReq);

    Boolean removeByIds(List<UUID> ids);

    boolean saveBatch(List<PostTags> entities);

    boolean existsById(UUID id);

    List<PostTags> listByIds(List<UUID> ids);

    CursorResult<PostTags> seek(UUID cursor, int limit);

    List<PostTags> listByPostId(UUID postId);

    List<PostTags> listByTagId(UUID tagId);

    void removeByPostId(UUID postId);

    boolean existsByPostIdAndTagId(UUID postId, UUID tagId);
}
