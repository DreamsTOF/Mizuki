package cn.dreamtof.social.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.application.assembler.FriendTagsAssembler;
import cn.dreamtof.social.domain.model.entity.FriendTags;
import cn.dreamtof.social.domain.repository.FriendTagsRepository;
import cn.dreamtof.social.infrastructure.persistence.mapper.FriendTagsMapper;
import cn.dreamtof.social.infrastructure.persistence.po.FriendTagsPO;
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
public class FriendTagsRepositoryImpl extends ServiceImpl<FriendTagsMapper, FriendTagsPO> implements FriendTagsRepository {

    private final FriendTagsAssembler assembler;

    @Override
    public FriendTags create(FriendTags entity) {
        FriendTagsPO po = assembler.toPO(entity);
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
    public FriendTags update(FriendTags entity) {
        FriendTagsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public FriendTags getById(UUID id) {
        FriendTagsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<FriendTags> listAll() {
        List<FriendTagsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<FriendTags> page(PageReq pageReq) {
        Page<FriendTagsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<FriendTagsPO> resultPage = super.page(flexPage, queryWrapper);
        List<FriendTags> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<FriendTags> entities) {
        List<FriendTagsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(FriendTagsPO::getId).eq(id).exists();
    }

    @Override
    public List<FriendTags> listByIds(List<UUID> ids) {
        List<FriendTagsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<FriendTags> seek(UUID cursor, int limit) {
        List<FriendTagsPO> poList = queryChain()
                .where(FriendTagsPO::getId).gt(cursor)
                .orderBy(FriendTagsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<FriendTagsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }
}
