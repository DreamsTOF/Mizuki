package cn.dreamtof.social.application.service;

import cn.dreamtof.core.base.CursorResult;
import cn.dreamtof.core.base.PageReq;
import cn.dreamtof.core.base.PageResult;
import cn.dreamtof.social.domain.model.entity.Comments;
import cn.dreamtof.social.domain.repository.CommentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentsAppService {

    private final CommentsRepository repository;

    public Comments create(Comments entity) {
        return repository.create(entity);
    }

    public Comments update(Comments entity) {
        return repository.update(entity);
    }

    public boolean removeById(UUID id) {
        return repository.removeById(id);
    }

    public Comments getDetail(UUID id) {
        return repository.getById(id);
    }

    public List<Comments> listAll() {
        return repository.listAll();
    }

    public PageResult<Comments> page(PageReq pageReq) {
        return repository.page(pageReq);
    }

    public CursorResult<Comments> seek(UUID cursor, int limit) {
        return repository.seek(cursor, limit);
    }

    public List<Comments> listByTargetId(UUID targetId) {
        return repository.listByTargetId(targetId);
    }

    public List<Comments> listByTargetIdAndType(UUID targetId, String type) {
        return repository.listByTargetIdAndType(targetId, type);
    }

    public long countByTargetId(UUID targetId) {
        return repository.countByTargetId(targetId);
    }

    public List<Comments> listPending() {
        return repository.listPending();
    }
}
