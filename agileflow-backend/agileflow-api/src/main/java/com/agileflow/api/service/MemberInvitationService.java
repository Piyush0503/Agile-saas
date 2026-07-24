package com.agileflow.api.service;

import com.agileflow.api.dto.InvitationResponse;
import com.agileflow.api.dto.InviteMemberRequest;
import com.agileflow.core.domain.OrgInvitation;
import com.agileflow.core.domain.OrgMember;
import com.agileflow.core.domain.Organization;
import com.agileflow.core.domain.User;
import com.agileflow.core.domain.enums.OrgRole;
import com.agileflow.infrastructure.repository.OrgInvitationRepository;
import com.agileflow.infrastructure.repository.OrgMemberRepository;
import com.agileflow.infrastructure.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberInvitationService {

    private final OrgInvitationRepository invitationRepository;
    private final OrgMemberRepository orgMemberRepository;
    private final UserRepository userRepository;
    private final OrganizationService organizationService;
    private final JavaMailSender mailSender;

    @Transactional
    public InvitationResponse invite(String orgSlug, InviteMemberRequest request, UUID invitedByUserId) {
        Organization org = organizationService.findBySlug(orgSlug);
        organizationService.requireRole(org.getId(), invitedByUserId, OrgRole.OWNER, OrgRole.ADMIN);

        OrgRole role;
        try {
            role = OrgRole.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }

        if (role == OrgRole.OWNER) {
            throw new IllegalArgumentException("Cannot invite someone directly as OWNER. Add them first, then promote.");
        }

        // Check if user is already a member
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            boolean alreadyMember = orgMemberRepository.existsByOrgIdAndUserId(org.getId(), existingUser.get().getId());
            if (alreadyMember) {
                throw new IllegalStateException("User is already a member of this organization");
            }
        }

        // Check for existing pending invite
        Optional<OrgInvitation> existing = invitationRepository.findByOrganizationIdAndEmail(org.getId(), request.getEmail());
        if (existing.isPresent()) {
            // Revoke old one and create fresh
            invitationRepository.delete(existing.get());
        }

        User inviter = userRepository.findById(invitedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("Inviting user not found"));

        String token = UUID.randomUUID().toString();
        OrgInvitation invitation = OrgInvitation.builder()
                .organization(org)
                .email(request.getEmail())
                .role(role)
                .token(token)
                .expiresAt(OffsetDateTime.now().plusHours(48))
                .invitedBy(inviter)
                .build();

        invitationRepository.save(invitation);

        // Send email
        sendInvitationEmail(request.getEmail(), org.getName(), inviter.getName(), token);

        log.info("Invitation sent to {} for org '{}' with role {} by user {}", request.getEmail(), orgSlug, role, invitedByUserId);

        return toResponse(invitation);
    }

    public List<InvitationResponse> listPendingInvitations(String orgSlug, UUID currentUserId) {
        Organization org = organizationService.findBySlug(orgSlug);
        organizationService.requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN);

        return invitationRepository.findByOrganizationId(org.getId())
                .stream()
                .filter(inv -> inv.getExpiresAt().isAfter(OffsetDateTime.now()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void revokeInvitation(String orgSlug, UUID invitationId, UUID currentUserId) {
        Organization org = organizationService.findBySlug(orgSlug);
        organizationService.requireRole(org.getId(), currentUserId, OrgRole.OWNER, OrgRole.ADMIN);

        OrgInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (!invitation.getOrganization().getId().equals(org.getId())) {
            throw new IllegalArgumentException("Invitation does not belong to this organization");
        }

        invitationRepository.delete(invitation);
        log.info("Invitation {} revoked by user {}", invitationId, currentUserId);
    }

    @Transactional
    public void acceptInvitation(String token) {
        OrgInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid or expired invitation"));

        if (invitation.getExpiresAt().isBefore(OffsetDateTime.now())) {
            invitationRepository.delete(invitation);
            throw new IllegalStateException("Invitation has expired");
        }

        User user = userRepository.findByEmail(invitation.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Please register an account first, then accept the invitation"));

        // Add as member
        OrgMember member = OrgMember.builder()
                .orgId(invitation.getOrganization().getId())
                .userId(user.getId())
                .role(invitation.getRole())
                .build();
        orgMemberRepository.save(member);

        // Remove invitation
        invitationRepository.delete(invitation);

        log.info("User {} accepted invitation to org {}", user.getEmail(), invitation.getOrganization().getSlug());

        // TODO: Send WebSocket notification to org admins about new member
    }

    private void sendInvitationEmail(String toEmail, String orgName, String inviterName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("You've been invited to join " + orgName + " on AgileFlow");
            message.setText(
                    "Hi,\n\n" +
                    inviterName + " has invited you to join " + orgName + " on AgileFlow.\n\n" +
                    "Click the link below to accept the invitation:\n" +
                    "http://localhost:3000/invite/" + token + "\n\n" +
                    "This invitation expires in 48 hours.\n\n" +
                    "— The AgileFlow Team"
            );
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
            // Don't fail the whole operation if email fails
        }
    }

    private InvitationResponse toResponse(OrgInvitation inv) {
        return InvitationResponse.builder()
                .id(inv.getId())
                .email(inv.getEmail())
                .role(inv.getRole().name())
                .invitedByName(inv.getInvitedBy() != null ? inv.getInvitedBy().getName() : null)
                .expiresAt(inv.getExpiresAt())
                .createdAt(inv.getCreatedAt())
                .build();
    }
}
