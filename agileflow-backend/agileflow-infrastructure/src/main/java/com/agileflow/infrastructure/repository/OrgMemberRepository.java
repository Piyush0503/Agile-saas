package com.agileflow.infrastructure.repository;

import com.agileflow.core.domain.OrgMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrgMemberRepository extends JpaRepository<OrgMember, OrgMember.OrgMemberId> {
    List<OrgMember> findByUserId(UUID userId);
    List<OrgMember> findByOrgId(UUID orgId);
}
