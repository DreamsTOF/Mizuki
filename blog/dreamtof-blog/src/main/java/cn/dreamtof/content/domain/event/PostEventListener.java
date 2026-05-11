package cn.dreamtof.content.domain.event;

import cn.dreamtof.content.domain.model.entity.Archives;
import cn.dreamtof.content.domain.repository.ArchivesRepository;
import cn.dreamtof.core.event.PostCreatedEvent;
import cn.dreamtof.core.event.PostDeletedEvent;
import cn.dreamtof.core.event.PostPublishedEvent;
import cn.dreamtof.core.event.PostUnpublishedEvent;
import cn.dreamtof.core.event.PostUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 文章领域事件监听器
 * <p>
 * 处理文章创建/删除/发布等事件，更新归档统计、标签关联等。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final ArchivesRepository archivesRepository;

    /**
     * 文章创建/发布时，更新归档和标签
     */
    @EventListener
    public void onPostCreated(PostCreatedEvent event) {
        log.info("监听到 PostCreatedEvent, postId={}, title={}", event.getPostId(), event.getTitle());
        try {
            if (event.getPublishedAt() != null) {
                // 更新归档统计（按月）
                int year = event.getPublishedAt().getYear();
                int month = event.getPublishedAt().getMonthValue();
                Archives archive = archivesRepository.findByYearAndMonth(year, month);
                if (archive != null) {
                    archive.addPost(event.getPostId());
                    archivesRepository.update(archive);
                } else {
                    Archives newArchive = Archives.create(year, month);
                    newArchive.addPost(event.getPostId());
                    archivesRepository.create(newArchive);
                }
                log.info("归档统计已更新, year={}, month={}", year, month);
            }
        } catch (Exception e) {
            log.error("PostCreatedEvent 处理失败, postId={}", event.getPostId(), e);
        }
    }

    /**
     * 文章更新时处理
     */
    @EventListener
    public void onPostUpdated(PostUpdatedEvent event) {
        log.debug("监听到 PostUpdatedEvent, postId={}", event.getPostId());
        // 预留：标签同步等扩展点
    }

    /**
     * 文章删除时，更新归档统计
     */
    @EventListener
    public void onPostDeleted(PostDeletedEvent event) {
        log.info("监听到 PostDeletedEvent, postId={}", event.getPostId());
        // 归档扣除逻辑由业务侧在删除时同步处理，此处仅记录
        log.info("文章已删除, postId={}, 归档将在下次全量重建时同步", event.getPostId());
    }

    /**
     * 文章发布时处理
     */
    @EventListener
    public void onPostPublished(PostPublishedEvent event) {
        log.info("监听到 PostPublishedEvent, postId={}", event.getPostId());
        // 预留：发送通知、刷新缓存等
    }

    /**
     * 文章下架时处理
     */
    @EventListener
    public void onPostUnpublished(PostUnpublishedEvent event) {
        log.info("监听到 PostUnpublishedEvent, postId={}", event.getPostId());
        // 预留：清理缓存等
    }
}
