package cn.dreamtof.social.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.domain.model.entity.FriendTags;
import cn.dreamtof.social.domain.repository.FriendTagsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendTagsAppService {

    private final FriendTagsRepository repository;

    public FriendTags create(FriendTags entity) {
        return repository.create(entity);
    }

    public FriendTags update(FriendTags entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public FriendTags getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<FriendTags> listAll() {
        return repository.listAll();
    }

    public PageResult<FriendTags> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<FriendTags> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }
}
