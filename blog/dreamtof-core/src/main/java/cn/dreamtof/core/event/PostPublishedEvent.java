package cn.dreamtof.core.event;

import java.util.UUID;

public class PostPublishedEvent extends BaseDomainEvent {
    private final UUID postId;

    public PostPublishedEvent(UUID postId) {
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}
