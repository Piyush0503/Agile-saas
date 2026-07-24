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
public class InvitationResponse {
    private UUID id;
    private String email;
    private String role;
    private String invitedByName;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
}
