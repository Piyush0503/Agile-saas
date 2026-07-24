package com.agileflow.api.service;

import com.agileflow.api.dto.MemberResponse;
import com.agileflow.api.dto.OrgResponse;
import com.agileflow.api.dto.OrgUpdateRequest;
import com.agileflow.core.domain.OrgMember;
import com.agileflow.core.domain.Organization;
import com.agileflow.core.domain.User;
import com.agileflow.core.domain.enums.OrgRole;
import com.agileflow.infrastructure.repository.OrgMemberRepository;
import com.agileflow.infrastructure.repository.OrganizationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrgMemberRepository orgMemberRepository;

    public Organization findBySlug(String slug) {
        return organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + slug));
    }

    public OrgResponse getOrgDetails(String slug) {
        Organization org = findBySlug(slug);
        int memberCount = orgMemberRepository.findByOrgId(org.getId()).size();
        return toOrgResponse(org, memberCount);
    }

    @Transactional
    public OrgResponse updateOrg(String slug, OrgUpdateRequest request, UUID currentUserId) {
        Organization org = findBySlug(slug);
        requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN);

        org.setName(request.getName());
        organizationRepository.save(org);

        int memberCount = orgMemberRepository.findByOrgId(org.getId()).size();
        log.info("Organization '{}' updated by user {}", slug, currentUserId);
        return toOrgResponse(org, memberCount);
    }

    @Transactional
    public void deleteOrg(String slug, UUID currentUserId) {
        Organization org = findBySlug(slug);
        requireRole(org.getId(), currentUserId, OrgRole.OWNER);

        log.info("Organization '{}' deleted by user {}", slug, currentUserId);
        organizationRepository.delete(org);
    }

    public List<MemberResponse> listMembers(String slug) {
        Organization org = findBySlug(slug);
        List<OrgMember> members = orgMemberRepository.findByOrgIdWithUser(org.getId());
        return members.stream().map(this::toMemberResponse).toList();
    }

    @Transactional
    public void changeRole(String slug, UUID targetUserId, OrgRole newRole, UUID currentUserId) {
        Organization org = findBySlug(slug);
        requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN);

        OrgMember target = orgMemberRepository.findByOrgIdAndUserId(org.getId(), targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found in organization"));

        // Prevent demoting last OWNER
        if (target.getRole() == OrgRole.OWNER && newRole != OrgRole.OWNER) {
            long ownerCount = orgMemberRepository.countByOrgIdAndRole(org.getId(), OrgRole.OWNER);
            if (ownerCount <= 1) {
                throw new IllegalStateException("Cannot demote the last owner of the organization");
            }
        }

        // Only OWNER can promote to OWNER or demote an existing OWNER
        if (newRole == OrgRole.OWNER || target.getRole() == OrgRole.OWNER) {
            requireRole(org.getId(), currentUserId, OrgRole.OWNER);
        }

        log.info("Role changed for user {} in org '{}': {} -> {} (by user {})",
                targetUserId, slug, target.getRole(), newRole, currentUserId);
        target.setRole(newRole);
        orgMemberRepository.save(target);
    }

    @Transactional
    public void removeMember(String slug, UUID targetUserId, UUID currentUserId) {
        Organization org = findBySlug(slug);
        requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN);

        OrgMember target = orgMemberRepository.findByOrgIdAndUserId(org.getId(), targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found in organization"));

        // Prevent removing self if you're the last OWNER
        if (target.getRole() == OrgRole.OWNER) {
            long ownerCount = orgMemberRepository.countByOrgIdAndRole(org.getId(), OrgRole.OWNER);
            if (ownerCount <= 1) {
                throw new IllegalStateException("Cannot remove the last owner of the organization");
            }
            // Only OWNER can remove another OWNER
            requireRole(org.getId(), currentUserId, OrgRole.OWNER);
        }

        log.info("Member {} removed from org '{}' by user {}", targetUserId, slug, currentUserId);
        orgMemberRepository.delete(target);
    }

    // --- Helpers ---

    public void requireRole(UUID orgId, UUID userId, OrgRole... allowedRoles) {
        OrgMember member = orgMemberRepository.findByOrgIdAndUserId(orgId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));

        for (OrgRole allowed : allowedRoles) {
            if (member.getRole() == allowed) return;
        }
        throw new AccessDeniedException("You do not have the required role for this action");
    }

    private OrgResponse toOrgResponse(Organization org, int memberCount) {
        return OrgResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .plan(org.getPlan())
                .memberCount(memberCount)
                .createdAt(org.getCreatedAt())
                .build();
    }

    private MemberResponse toMemberResponse(OrgMember member) {
        User user = member.getUser();
        String initials = getInitials(user.getName());
        String color = getAvatarColor(user.getName());

        return MemberResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .avatarInitials(initials)
                .avatarColor(color)
                .role(member.getRole().name())
                .status("ACTIVE") // TODO: integrate WebSocket presence tracking
                .joinedAt(member.getJoinedAt())
                .build();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarColor(String name) {
        // Deterministic color from name hash
        String[] colors = {"#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899", "#06b6d4", "#f97316"};
        int hash = Math.abs(name.hashCode());
        return colors[hash % colors.length];
    }
}
