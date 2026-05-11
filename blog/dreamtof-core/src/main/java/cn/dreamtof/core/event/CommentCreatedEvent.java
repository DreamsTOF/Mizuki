package cn.dreamtof.core.event;

import java.util.UUID;

public class CommentCreatedEvent extends BaseDomainEvent {
    /**
     * 评论目标类型，如 POST、DIARY 等
     */
    private final String targetType;

    /**
     * 评论目标 ID
     */
    private final UUID targetId;

    public CommentCreatedEvent(String targetType, UUID targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }
}
