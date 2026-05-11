package cn.dreamtof.core.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PostCreatedEvent extends BaseDomainEvent {
    private final UUID postId;
    private final String title;
    private final OffsetDateTime publishedAt;

    public PostCreatedEvent(UUID postId, String title, OffsetDateTime publishedAt) {
        this.postId = postId;
        this.title = title;
        this.publishedAt = publishedAt;
    }

    public UUID getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }
}
