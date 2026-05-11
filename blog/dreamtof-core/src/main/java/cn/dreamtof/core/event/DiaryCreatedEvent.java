package cn.dreamtof.core.event;

import java.util.UUID;

public class DiaryCreatedEvent extends BaseDomainEvent {
    private final UUID diaryId;

    public DiaryCreatedEvent(UUID diaryId) {
        this.diaryId = diaryId;
    }

    public UUID getDiaryId() {
        return diaryId;
    }
}
