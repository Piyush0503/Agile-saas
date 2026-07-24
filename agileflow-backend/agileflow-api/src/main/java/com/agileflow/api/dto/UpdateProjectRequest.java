package com.agileflow.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProjectRequest {
    @Size(min = 2, max = 100)
    private String name;

    private String description;
    private UUID leadId;
}
