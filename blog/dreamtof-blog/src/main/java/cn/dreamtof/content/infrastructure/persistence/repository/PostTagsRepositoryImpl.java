package cn.dreamtof.content.infrastructure.persistence.repository;

import cn.dreamtof.content.application.assembler.PostTagsAssembler;
import cn.dreamtof.content.domain.model.entity.PostTags;
import cn.dreamtof.content.domain.repository.PostTagsRepository;
import cn.dreamtof.content.infrastructure.persistence.mapper.PostTagsMapper;
import cn.dreamtof.content.infrastructure.persistence.po.PostTagsPO;
import cn.dreamtof.content.infrastructure.persistence.po.table.PostTagsTableDef;
import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostTagsRepositoryImpl extends ServiceImpl<PostTagsMapper, PostTagsPO> implements PostTagsRepository {

    private final PostTagsAssembler assembler;

    private static final PostTagsTableDef T = PostTagsTableDef.POST_TAGS_PO;

    @Override
    public PostTags create(PostTags entity) {
        PostTagsPO po = assembler.toPO(entity);
        if (super.save(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public boolean removeById(UUID id) {
        return super.removeById(id);
    }

    @Override
    public PostTags update(PostTags entity) {
        PostTagsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public PostTags getById(UUID id) {
        PostTagsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<PostTags> listAll() {
        List<PostTagsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<PostTags> page(PageReq pageReq) {
        Page<PostTagsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        Page<PostTagsPO> resultPage = super.page(flexPage, qw);
        List<PostTags> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<PostTags> entities) {
        List<PostTagsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<PostTags> listByIds(List<UUID> ids) {
        List<PostTagsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<PostTags> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<PostTagsPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<PostTagsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<PostTags> listByPostId(UUID postId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.POST_ID.eq(postId));
        List<PostTagsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<PostTags> listByTagId(UUID tagId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.TAG_ID.eq(tagId));
        List<PostTagsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public void removeByPostId(UUID postId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.POST_ID.eq(postId));
        super.remove(qw);
    }

    @Override
    public boolean existsByPostIdAndTagId(UUID postId, UUID tagId) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.POST_ID.eq(postId));
        qw.where(T.TAG_ID.eq(tagId));
        return super.count(qw) > 0;
    }
}
