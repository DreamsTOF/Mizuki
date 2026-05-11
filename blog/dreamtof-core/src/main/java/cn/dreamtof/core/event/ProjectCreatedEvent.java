package cn.dreamtof.core.event;

import java.util.UUID;

public class ProjectCreatedEvent extends BaseDomainEvent {
    private final UUID projectId;
    private final String category;

    public ProjectCreatedEvent(UUID projectId, String category) {
        this.projectId = projectId;
        this.category = category;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getCategory() {
        return category;
    }
}
