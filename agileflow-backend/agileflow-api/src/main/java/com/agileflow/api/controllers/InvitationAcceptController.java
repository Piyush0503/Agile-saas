package com.agileflow.api.controllers;

import com.agileflow.api.service.MemberInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/invitations")
@RequiredArgsConstructor
public class InvitationAcceptController {

    private final MemberInvitationService invitationService;

    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(@RequestParam String token) {
        invitationService.acceptInvitation(token);
        return ResponseEntity.ok("Invitation accepted successfully");
    }
}
