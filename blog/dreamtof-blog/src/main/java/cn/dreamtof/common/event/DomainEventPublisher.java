package cn.dreamtof.common.event;

import cn.dreamtof.core.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器
 * <p>
 * 封装 Spring {@link ApplicationEventPublisher}，提供类型安全的领域事件发布方法。
 * 同步发布（QPS=5，基础设施永不崩溃，无需异步）。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    // ==================== 文章事件 ====================

    public void publishPostCreated(PostCreatedEvent event) {
        log.info("发布领域事件: PostCreatedEvent, postId={}", event.getPostId());
        eventPublisher.publishEvent(event);
    }

    public void publishPostUpdated(PostUpdatedEvent event) {
        log.info("发布领域事件: PostUpdatedEvent, postId={}", event.getPostId());
        eventPublisher.publishEvent(event);
    }

    public void publishPostDeleted(PostDeletedEvent event) {
        log.info("发布领域事件: PostDeletedEvent, postId={}", event.getPostId());
        eventPublisher.publishEvent(event);
    }

    public void publishPostPublished(PostPublishedEvent event) {
        log.info("发布领域事件: PostPublishedEvent, postId={}", event.getPostId());
        eventPublisher.publishEvent(event);
    }

    public void publishPostUnpublished(PostUnpublishedEvent event) {
        log.info("发布领域事件: PostUnpublishedEvent, postId={}", event.getPostId());
        eventPublisher.publishEvent(event);
    }

    // ==================== 评论事件 ====================

    public void publishCommentCreated(CommentCreatedEvent event) {
        log.info("发布领域事件: CommentCreatedEvent, targetType={}, targetId={}",
                event.getTargetType(), event.getTargetId());
        eventPublisher.publishEvent(event);
    }

    // ==================== 项目事件 ====================

    public void publishProjectCreated(ProjectCreatedEvent event) {
        log.info("发布领域事件: ProjectCreatedEvent, projectId={}", event.getProjectId());
        eventPublisher.publishEvent(event);
    }

    public void publishProjectDeleted(ProjectDeletedEvent event) {
        log.info("发布领域事件: ProjectDeletedEvent, projectId={}", event.getProjectId());
        eventPublisher.publishEvent(event);
    }

    // ==================== 日记事件 ====================

    public void publishDiaryCreated(DiaryCreatedEvent event) {
        log.info("发布领域事件: DiaryCreatedEvent, diaryId={}", event.getDiaryId());
        eventPublisher.publishEvent(event);
    }

    // ==================== 时间线事件 ====================

    public void publishTimelineEventCreated(TimelineEventCreatedEvent event) {
        log.info("发布领域事件: TimelineEventCreatedEvent, eventId={}", event.getEventId());
        eventPublisher.publishEvent(event);
    }
}
