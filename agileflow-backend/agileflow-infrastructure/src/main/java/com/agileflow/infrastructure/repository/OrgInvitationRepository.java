package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.OrgInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgInvitationRepository extends JpaRepository<OrgInvitation, UUID> {
    List<OrgInvitation> findByOrganizationId(UUID orgId);
    Optional<OrgInvitation> findByToken(String token);
    Optional<OrgInvitation> findByOrganizationIdAndEmail(UUID orgId, String email);
    void deleteByExpiresAtBefore(OffsetDateTime now);
}
