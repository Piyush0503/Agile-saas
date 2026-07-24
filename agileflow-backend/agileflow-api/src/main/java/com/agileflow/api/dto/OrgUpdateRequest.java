package com.agileflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrgUpdateRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
}
