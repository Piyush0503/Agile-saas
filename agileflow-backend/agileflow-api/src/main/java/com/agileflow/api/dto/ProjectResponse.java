package com.agileflow.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String name;
    private String key;
    private String description;
    private String leadName;
    private UUID leadId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
