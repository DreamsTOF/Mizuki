package cn.dreamtof.core.event;

import java.util.UUID;

public class PostDeletedEvent extends BaseDomainEvent {
    private final UUID postId;

    public PostDeletedEvent(UUID postId) {
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}
