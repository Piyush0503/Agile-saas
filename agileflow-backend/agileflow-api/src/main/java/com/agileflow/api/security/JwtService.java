package com.agileflow.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    // Using an ephemeral in-memory RSA key pair as requested (RS256).
    // In production, this should be loaded from a keystore or configuration to survive restarts.
    private static final KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();

    private final StringRedisTemplate redisTemplate;

    @Value("${app.jwt.expiration-ms:900000}")
    private long jwtExpirationMs; // Default 15 minutes

    public String generateAccessToken(UUID userId, String email, UUID orgId, String orgSlug, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("orgId", orgId != null ? orgId.toString() : null)
                .claim("orgSlug", orgSlug)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();
    }

    public UUID getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromJWT(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public UUID getOrgIdFromJWT(String token) {
        String orgIdStr = extractAllClaims(token).get("orgId", String.class);
        return orgIdStr != null ? UUID.fromString(orgIdStr) : null;
    }

    public String getOrgSlugFromJWT(String token) {
        return extractAllClaims(token).get("orgSlug", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJWT(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    public boolean validateToken(String authToken) {
        try {
            if (isTokenBlacklisted(authToken)) {
                log.error("JWT token is blacklisted");
                return false;
            }
            Jwts.parser()
                    .verifyWith(keyPair.getPublic())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (Exception ex) {
            log.error("Invalid JWT token", ex);
        }
        return false;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            long expirationTime = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();
            long ttl = expirationTime - currentTime;
            if (ttl > 0) {
                redisTemplate.opsForValue().set("jwt:blacklist:" + token, "blacklisted", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (ExpiredJwtException e) {
            // Token is already expired, no need to blacklist
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
        }
    }

    private boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + token));
    }
}
