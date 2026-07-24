package com.agileflow.infrastructure.repository.projections;

import java.util.UUID;

public interface IssueProjection {
    UUID getId();
    String getTitle();
    String getStatus();
    String getType();
    String getPriority();
    Double getPosition();
    
    AssigneeProjection getAssignee();
    
    interface AssigneeProjection {
        UUID getId();
        String getName();
        String getAvatarUrl();
    }
}
