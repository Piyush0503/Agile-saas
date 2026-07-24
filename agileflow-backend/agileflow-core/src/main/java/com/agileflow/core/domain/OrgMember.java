package com.agileflow.core.domain;

import com.agileflow.core.domain.enums.OrgRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "org_members")
@IdClass(OrgMember.OrgMemberId.class)
@Data
@NoArgsConstructor @AllArgsConstructor @Builder
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "orgId", type = UUID.class)})
@Filter(name = "tenantFilter", condition = "org_id = :orgId")
public class OrgMember {

    @Id
    @Column(name = "org_id")
    private UUID orgId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrgRole role; // OWNER, ADMIN, MEMBER, VIEWER

    @CreationTimestamp
    @Column(name = "joined_at", updatable = false)
    private OffsetDateTime joinedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrgMemberId implements Serializable {
        private UUID orgId;
        private UUID userId;
    }
}
