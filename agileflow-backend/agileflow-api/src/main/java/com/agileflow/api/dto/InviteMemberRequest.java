package com.agileflow.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteMemberRequest {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private String role; // ADMIN, MEMBER, VIEWER (not OWNER)
}
