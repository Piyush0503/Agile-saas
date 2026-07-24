package com.agileflow.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {
    @NotNull
    private String role; // OWNER, ADMIN, MEMBER, VIEWER
}
