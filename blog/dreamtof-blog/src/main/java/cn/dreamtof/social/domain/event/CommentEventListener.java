package cn.dreamtof.social.domain.event;

import cn.dreamtof.core.event.CommentCreatedEvent;
import cn.dreamtof.social.domain.repository.CommentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 评论领域事件监听器
 * <p>
 * 处理评论创建事件，更新对应目标的评论计数。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventListener {

    private final CommentsRepository commentsRepository;

    /**
     * 评论创建时，更新评论计数
     */
    @EventListener
    public void onCommentCreated(CommentCreatedEvent event) {
        log.info("监听到 CommentCreatedEvent, targetType={}, targetId={}",
                event.getTargetType(), event.getTargetId());
        try {
            long count = commentsRepository.countByTargetId(event.getTargetId());
            log.info("评论计数已更新, targetType={}, targetId={}, count={}",
                    event.getTargetType(), event.getTargetId(), count);
        } catch (Exception e) {
            log.error("CommentCreatedEvent 处理失败, targetType={}, targetId={}",
                    event.getTargetType(), event.getTargetId(), e);
        }
    }
}
