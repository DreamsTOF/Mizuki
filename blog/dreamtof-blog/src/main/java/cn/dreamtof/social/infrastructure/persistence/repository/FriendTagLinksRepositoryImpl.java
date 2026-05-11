package cn.dreamtof.social.infrastructure.persistence.repository;

import cn.dreamtof.social.application.assembler.FriendTagLinksAssembler;
import cn.dreamtof.social.domain.model.entity.FriendTagLinks;
import cn.dreamtof.social.domain.repository.FriendTagLinksRepository;
import cn.dreamtof.social.infrastructure.persistence.mapper.FriendTagLinksMapper;
import cn.dreamtof.social.infrastructure.persistence.po.FriendTagLinksPO;
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
public class FriendTagLinksRepositoryImpl extends ServiceImpl<FriendTagLinksMapper, FriendTagLinksPO> implements FriendTagLinksRepository {

    private final FriendTagLinksAssembler assembler;

    @Override
    public FriendTagLinks create(FriendTagLinks entity) {
        FriendTagLinksPO po = assembler.toPO(entity);
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
    public List<FriendTagLinks> listByFriendId(UUID friendId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(FriendTagLinksPO::getFriendId).eq(friendId);
        List<FriendTagLinksPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }

    @Override
    public List<FriendTagLinks> listByTagId(UUID tagId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(FriendTagLinksPO::getTagId).eq(tagId);
        List<FriendTagLinksPO> poList = super.list(queryWrapper);
        return assembler.toEntityList(poList);
    }

    @Override
    public boolean removeByFriendId(UUID friendId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(FriendTagLinksPO::getFriendId).eq(friendId);
        return super.remove(queryWrapper);
    }

    @Override
    public boolean removeByFriendIdAndTagId(UUID friendId, UUID tagId) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(FriendTagLinksPO::getFriendId).eq(friendId);
        queryWrapper.and(FriendTagLinksPO::getTagId).eq(tagId);
        return super.remove(queryWrapper);
    }

    @Override
    public boolean saveBatch(List<FriendTagLinks> entities) {
        List<FriendTagLinksPO> pos = assembler.toPOList(entities);
        return super.saveBatch(pos);
    }
}
