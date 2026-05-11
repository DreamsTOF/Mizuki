package cn.dreamtof.social.domain.service;

import cn.dreamtof.core.exception.Asserts;
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
public class CommentDomainService {

    private final CommentsRepository commentsRepository;

    public Comments createComment(Comments entity) {
        Asserts.notNull(entity, "评论实体不能为空");
        Asserts.notNull(entity.getTargetId(), "评论目标ID不能为空");
        Asserts.notBlank(entity.getContent(), "评论内容不能为空");
        return commentsRepository.create(entity);
    }

    public Comments approveComment(UUID id) {
        Comments comment = commentsRepository.getById(id);
        Asserts.notNull(comment, "评论不存在");
        comment.setHasApproved(true);
        Comments updated = commentsRepository.update(comment);
        log.info("评论审核通过, commentId={}", id);
        return updated;
    }

    public boolean deleteComment(UUID id) {
        return commentsRepository.removeById(id);
    }

    public Comments likeComment(UUID id) {
        Comments comment = commentsRepository.getById(id);
        Asserts.notNull(comment, "评论不存在");
        int currentLikes = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
        comment.setLikeCount(currentLikes + 1);
        return commentsRepository.update(comment);
    }

    public List<Comments> listByTargetId(UUID targetId) {
        return commentsRepository.listByTargetId(targetId);
    }

    public List<Comments> listPending() {
        return commentsRepository.listPending();
    }

    public long countByTargetId(UUID targetId) {
        return commentsRepository.countByTargetId(targetId);
    }
}
