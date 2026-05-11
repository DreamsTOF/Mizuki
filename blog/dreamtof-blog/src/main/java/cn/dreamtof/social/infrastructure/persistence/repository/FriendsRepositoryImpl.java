package cn.dreamtof.social.infrastructure.persistence.repository;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.application.assembler.FriendsAssembler;
import cn.dreamtof.social.domain.model.entity.Friends;
import cn.dreamtof.social.domain.repository.FriendsRepository;
import cn.dreamtof.social.infrastructure.persistence.mapper.FriendsMapper;
import cn.dreamtof.social.infrastructure.persistence.po.FriendsPO;
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
public class FriendsRepositoryImpl extends ServiceImpl<FriendsMapper, FriendsPO> implements FriendsRepository {

    private final FriendsAssembler assembler;

    @Override
    public Friends create(Friends entity) {
        FriendsPO po = assembler.toPO(entity);
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
    public Friends update(Friends entity) {
        FriendsPO po = assembler.toPO(entity);
        if (super.updateById(po)) {
            return assembler.toEntity(po);
        }
        return null;
    }

    @Override
    public Friends getById(UUID id) {
        FriendsPO po = super.getById(id);
        return assembler.toEntity(po);
    }

    @Override
    public List<Friends> listAll() {
        List<FriendsPO> poList = super.list();
        return assembler.toEntityList(poList);
    }

    @Override
    public PageResult<Friends> page(PageReq pageReq) {
        Page<FriendsPO> flexPage = Page.of(pageReq.getPageNum(), pageReq.getPageSize());
        QueryWrapper queryWrapper = QueryWrapper.create();
        Page<FriendsPO> resultPage = super.page(flexPage, queryWrapper);
        List<Friends> entityList = assembler.toEntityList(resultPage.getRecords());
        return PageResult.of(entityList, resultPage.getTotalRow(), resultPage.getTotalPage(),
                resultPage.getPageNumber(), resultPage.getPageSize());
    }

    @Override
    public Boolean removeByIds(List<UUID> ids) {
        return super.removeByIds(ids);
    }

    @Override
    public boolean saveBatch(List<Friends> entities) {
        List<FriendsPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }

    @Override
    public boolean existsById(UUID id) {
        return queryChain().where(FriendsPO::getId).eq(id).exists();
    }

    @Override
    public List<Friends> listByIds(List<UUID> ids) {
        List<FriendsPO> poList = super.listByIds(ids);
        return assembler.toEntityList(poList);
    }

    @Override
    public CursorResult<Friends> seek(UUID cursor, int limit) {
        List<FriendsPO> poList = queryChain()
                .where(FriendsPO::getId).gt(cursor)
                .orderBy(FriendsPO::getId).asc()
                .limit(limit + 1)
                .list();
        boolean hasNext = poList.size() > limit;
        List<FriendsPO> resultList = hasNext ? poList.subList(0, limit) : poList;
        UUID nextCursor = null;
        if (!resultList.isEmpty()) {
            nextCursor = resultList.get(resultList.size() - 1).getId();
        }
        return new CursorResult<>(assembler.toEntityList(resultList), nextCursor, hasNext);
    }
}
