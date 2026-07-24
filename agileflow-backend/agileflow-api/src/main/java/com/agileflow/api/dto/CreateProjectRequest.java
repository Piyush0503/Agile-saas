package com.agileflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateProjectRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[A-Z][A-Z0-9]*$", message = "Key must be uppercase alphanumeric, starting with a letter (e.g., PROJ, AF1)")
    private String key;

    private String description;
    private UUID leadId;
}
