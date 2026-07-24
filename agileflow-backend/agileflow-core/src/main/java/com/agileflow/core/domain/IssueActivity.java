package com.agileflow.core.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;


import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "issue_activity")
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@Filter(name = "tenantFilter", condition = "issue_id IN (SELECT i.id FROM issues i JOIN projects p ON i.project_id = p.id WHERE p.org_id = :orgId)")
public class IssueActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Issue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User actor;

    @Column(nullable = false)
    private String action;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
