package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.OrgMember;
import com.agileflow.core.domain.enums.OrgRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrgMemberRepository extends JpaRepository<OrgMember, OrgMember.OrgMemberId> {
    List<OrgMember> findByUserId(UUID userId);
    List<OrgMember> findByOrgId(UUID orgId);
    Optional<OrgMember> findByOrgIdAndUserId(UUID orgId, UUID userId);
    long countByOrgIdAndRole(UUID orgId, OrgRole role);

    @Query("SELECT om FROM OrgMember om JOIN FETCH om.user WHERE om.orgId = :orgId")
    List<OrgMember> findByOrgIdWithUser(@Param("orgId") UUID orgId);

    boolean existsByOrgIdAndUserId(UUID orgId, UUID userId);
}
