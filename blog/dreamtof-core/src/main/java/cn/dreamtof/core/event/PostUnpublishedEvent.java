package cn.dreamtof.core.event;

import java.util.UUID;

public class PostUnpublishedEvent extends BaseDomainEvent {
    private final UUID postId;

    public PostUnpublishedEvent(UUID postId) {
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}
