package cn.dreamtof.content.infrastructure.persistence.repository;

import cn.dreamtof.content.application.assembler.PostsAssembler;
import cn.dreamtof.content.domain.model.entity.Posts;
import cn.dreamtof.content.domain.repository.PostsRepository;
import cn.dreamtof.content.infrastructure.persistence.mapper.PostsMapper;
import cn.dreamtof.content.infrastructure.persistence.po.PostsPO;
import cn.dreamtof.content.infrastructure.persistence.po.table.PostsTableDef;
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
public class PostsRepositoryImpl extends ServiceImpl<PostsMapper, PostsPO> implements PostsRepository {

    private final PostsAssembler assembler;

    private static final PostsTableDef T = PostsTableDef.POSTS_PO;

    @Override
    public Posts create(Posts entity) {
        PostsPO po = assembler.toPO(entity);
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
    public Posts update(Posts entity) {
        PostsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Posts getById(UUID id) {
        PostsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Posts> listAll() {
        List<PostsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Posts> page(PageReq pageReq) {
        Page<PostsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy(T.CREATED_AT.desc());
        Page<PostsPO> resultPage = super.page(flexPage, qw);
        List<Posts> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Posts> entities) {
        List<PostsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.ID.eq(id));
        return super.count(qw) > 0;
    }

    @Override
    public List<Posts> listByIds(List<UUID> ids) {
        List<PostsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Posts> seek(UUID cursor, int limit) {
        QueryWrapper qw = QueryWrapper.create();
        if (cursor != null) {
            qw.where(T.ID.gt(cursor));
        }
        qw.orderBy(T.ID.asc());
        qw.limit(limit + 1);
        List<PostsPO> poList = super.list(qw);
        boolean hasNext = poList.size() > limit;
        List<PostsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public Posts findBySlug(String slug) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.SLUG.eq(slug));
        qw.limit(1);
        PostsPO po = super.getOne(qw);
        return assembler.toEntity(po);
    }

    @Override
    public List<Posts> listPublished() {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.DRAFT.eq(false));
        qw.orderBy(T.PUBLISHED.desc());
        List<PostsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<Posts> listByCategory(String category) {
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.CATEGORY.eq(category));
        qw.where(T.DRAFT.eq(false));
        qw.orderBy(T.PUBLISHED.desc());
        List<PostsPO> poList = super.list(qw);
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Posts> searchByKeyword(String keyword, PageReq pageReq) {
        Page<PostsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper qw = QueryWrapper.create();
        qw.where(T.TITLE.like(keyword));
        qw.or(T.CONTENT.like(keyword));
        qw.or(T.DESCRIPTION.like(keyword));
        qw.where(T.DRAFT.eq(false));
        qw.orderBy(T.PUBLISHED.desc());
        Page<PostsPO> resultPage = super.page(flexPage, qw);
        List<Posts> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(), resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public long incrementViewCount(UUID id) {
        PostsPO po = super.getById(id);
        if (po == null) {
            return 0;
        }
        long newCount = po.getViewCount() != null ? po.getViewCount() + 1 : 1;
        po.setViewCount(newCount);
        super.updateById(po);
        return newCount;
    }
}
