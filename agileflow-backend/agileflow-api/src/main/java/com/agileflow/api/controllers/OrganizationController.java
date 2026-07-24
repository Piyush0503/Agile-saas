package com.agileflow.api.controllers;

import com.agileflow.api.dto.*;
import com.agileflow.api.service.MemberInvitationService;
import com.agileflow.api.service.OrganizationService;
import com.agileflow.core.domain.enums.OrgRole;
import com.agileflow.core.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orgs/{orgSlug}")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final MemberInvitationService invitationService;

    // ─── Organization CRUD ───────────────────────────────────

    @GetMapping
    public ResponseEntity<OrgResponse> getOrg(@PathVariable String orgSlug) {
        return ResponseEntity.ok(organizationService.getOrgDetails(orgSlug));
    }

    @PutMapping
    public ResponseEntity<OrgResponse> updateOrg(
            @PathVariable String orgSlug,
            @Valid @RequestBody OrgUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(organizationService.updateOrg(orgSlug, request, principal.getId()));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteOrg(
            @PathVariable String orgSlug,
            @AuthenticationPrincipal UserPrincipal principal) {
        organizationService.deleteOrg(orgSlug, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ─── Members ─────────────────────────────────────────────

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> listMembers(@PathVariable String orgSlug) {
        return ResponseEntity.ok(organizationService.listMembers(orgSlug));
    }

    @PostMapping("/members/invite")
    public ResponseEntity<InvitationResponse> inviteMember(
            @PathVariable String orgSlug,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(invitationService.invite(orgSlug, request, principal.getId()));
    }

    @PutMapping("/members/{userId}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable String orgSlug,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        OrgRole role;
        try {
            role = OrgRole.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        organizationService.changeRole(orgSlug, userId, role, principal.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String orgSlug,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        organizationService.removeMember(orgSlug, userId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ─── Invitations ─────────────────────────────────────────

    @GetMapping("/invitations")
    public ResponseEntity<List<InvitationResponse>> listInvitations(
            @PathVariable String orgSlug,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(invitationService.listPendingInvitations(orgSlug, principal.getId()));
    }

    @DeleteMapping("/invitations/{invitationId}")
    public ResponseEntity<Void> revokeInvitation(
            @PathVariable String orgSlug,
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        invitationService.revokeInvitation(orgSlug, invitationId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
