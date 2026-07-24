package com.agileflow.api.controllers;

import com.agileflow.api.dto.AuthResponse;
import com.agileflow.api.dto.LoginRequest;
import com.agileflow.api.dto.RefreshRequest;
import com.agileflow.api.dto.RegisterRequest;
import com.agileflow.api.dto.TokenRefreshResponse;
import com.agileflow.core.domain.RefreshToken;
import com.agileflow.core.domain.User;
import com.agileflow.api.security.JwtService;
import com.agileflow.api.service.AuthService;
import com.agileflow.infrastructure.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.registerUser(request.getEmail(), request.getPassword(), request.getName());
            return ResponseEntity.ok("User registered successfully. Please check email for verification.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticateUser(request.getEmail(), request.getPassword());
        
        // At login, if orgSlug is provided, we can fetch their role. Otherwise base roles.
        // For simplicity, just issuing ROLE_USER if they don't specify org slug
        List<String> roles = List.of("ROLE_USER");
        
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), null, request.getOrgSlug(), roles);
        RefreshToken refreshToken = authService.createRefreshToken(user);

        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .build();

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken(), userDto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(authService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Rotate the refresh token
                    authService.deleteRefreshTokenByUserId(user.getId());
                    RefreshToken newRefreshToken = authService.createRefreshToken(user);
                    
                    String token = jwtService.generateAccessToken(user.getId(), user.getEmail(), null, null, List.of("ROLE_USER"));
                    return ResponseEntity.ok(new TokenRefreshResponse(token, newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is not in database!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, @RequestBody RefreshRequest refreshRequest) {
        String bearerToken = request.getHeader("Authorization");
        String accessToken = "";
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            accessToken = bearerToken.substring(7);
        }
        
        authService.logout(accessToken, refreshRequest.getRefreshToken());
        return ResponseEntity.ok("Log out successful");
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        // TODO: Implement verify email logic
        return ResponseEntity.ok("Email verified");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        // TODO: Implement forgot password logic
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        // TODO: Implement reset password logic
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @GetMapping("/oauth2/google")
    public ResponseEntity<?> googleLogin() {
        // In a real OAuth setup, you'd integrate Spring Security OAuth2 Client 
        // and redirect to Google's authorization endpoint here or handled by Spring automatically.
        return ResponseEntity.status(302).header("Location", "/oauth2/authorization/google").build();
    }

    @GetMapping("/oauth2/github")
    public ResponseEntity<?> githubLogin() {
        return ResponseEntity.status(302).header("Location", "/oauth2/authorization/github").build();
    }
}
