package cn.dreamtof.core.event;

import java.util.UUID;

public class PostUpdatedEvent extends BaseDomainEvent {
    private final UUID postId;
    private final String title;

    public PostUpdatedEvent(UUID postId, String title) {
        this.postId = postId;
        this.title = title;
    }

    public UUID getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }
}
