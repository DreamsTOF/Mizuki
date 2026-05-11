package cn.dreamtof.core.event;

import java.util.UUID;

public class ProjectDeletedEvent extends BaseDomainEvent {
    private final UUID projectId;

    public ProjectDeletedEvent(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getProjectId() {
        return projectId;
    }
}
