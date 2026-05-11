package cn.dreamtof.core.event;

import java.util.UUID;

public class TimelineEventCreatedEvent extends BaseDomainEvent {
    private final UUID eventId;

    public TimelineEventCreatedEvent(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }
}
