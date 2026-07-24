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
public class MemberResponse {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private String avatarInitials;
    private String avatarColor;
    private String role;
    private String status;
    private OffsetDateTime joinedAt;
}
