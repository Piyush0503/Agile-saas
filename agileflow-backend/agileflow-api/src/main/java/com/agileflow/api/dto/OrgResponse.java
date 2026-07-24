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
public class OrgResponse {
    private UUID id;
    private String name;
    private String slug;
    private String plan;
    private int memberCount;
    private OffsetDateTime createdAt;
}
