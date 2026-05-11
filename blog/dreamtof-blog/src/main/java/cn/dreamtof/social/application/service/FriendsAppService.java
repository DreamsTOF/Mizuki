package cn.dreamtof.social.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.domain.model.entity.Friends;
import cn.dreamtof.social.domain.repository.FriendsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendsAppService {

    private final FriendsRepository repository;

    public Friends create(Friends entity) {
        return repository.create(entity);
    }

    public Friends update(Friends entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public Friends getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<Friends> listAll() {
        return repository.listAll();
    }

    public PageResult<Friends> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<Friends> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }
}
