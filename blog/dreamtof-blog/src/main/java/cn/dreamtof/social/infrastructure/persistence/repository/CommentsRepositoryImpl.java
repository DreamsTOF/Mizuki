package cn.dreamtof.social.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.application.assembler.CommentsAssembler;
import cn.dreamtof.social.domain.model.entity.Comments;
import cn.dreamtof.social.domain.repository.CommentsRepository;
import cn.dreamtof.social.infrastructure.persistence.mapper.CommentsMapper;
import cn.dreamtof.social.infrastructure.persistence.po.CommentsPO;
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
public class CommentsRepositoryImpl extends ServiceImpl<CommentsMapper, CommentsPO> implements CommentsRepository {

    private final CommentsAssembler assembler;

    @Override
    public Comments create(Comments entity) {
        CommentsPO po = assembler.toPO(entity);
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
    public Comments update(Comments entity) {
        CommentsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Comments getById(UUID id) {
        CommentsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Comments> listAll() {
        List<CommentsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Comments> page(PageReq pageReq) {
        Page<CommentsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<CommentsPO> resultPage = super.page(flexPage, queryWrapper);
        List<Comments> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Comments> entities) {
        List<CommentsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(CommentsPO::getId).eq(id).exists();
    }

    @Override
    public List<Comments> listByIds(List<UUID> ids) {
        List<CommentsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Comments> seek(UUID cursor, int limit) {
        List<CommentsPO> poList = queryChain()
                .where(CommentsPO::getId).gt(cursor)
                .orderBy(CommentsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<CommentsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }

    @Override
    public List<Comments> listByTargetId(UUID targetId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(CommentsPO::getTargetId).eq(targetId);
        List<CommentsPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<Comments> listByTargetIdAndType(UUID targetId, String type) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(CommentsPO::getTargetId).eq(targetId);
        queryWrapper.and(CommentsPO::getTargetType).eq(type);
        List<CommentsPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }

    @Override
    public long countByTargetId(UUID targetId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(CommentsPO::getTargetId).eq(targetId);
        return super.count(queryWrapper);
    }

    @Override
    public long countByTargetIdAndType(UUID targetId, String type) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(CommentsPO::getTargetId).eq(targetId);
        queryWrapper.and(CommentsPO::getTargetType).eq(type);
        return super.count(queryWrapper);
    }

    @Override
    public List<Comments> listPending() {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(CommentsPO::getHasApproved).eq(false);
        List<CommentsPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }
}
