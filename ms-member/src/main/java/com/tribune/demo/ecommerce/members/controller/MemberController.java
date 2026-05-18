package com.tribune.demo.ecommerce.members.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Demo REST controller showing how to access Keycloak token claims in Spring Boot.
 *
 * Full tutorial: https://www.iamdevbox.com/posts/keycloak-spring-boot-oauth2-integration-complete-guide/
 */
@RestController
public class MemberController {

    /**
     * Returns profile information from the Keycloak JWT token.
     * Requires ROLE_USER or ROLE_ADMIN authority.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Object> getProfile(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = realmAccess != null
                ? (List<String>) realmAccess.getOrDefault("roles", List.of())
                : List.of();

        return Map.of(
                "username", jwt.getClaim("preferred_username"),
                "email", jwt.getClaim("email") != null ? jwt.getClaim("email") : "not-provided",
                "subject", jwt.getSubject(),
                "realmRoles", roles,
                "tokenExpiry", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : "unknown"
        );
    }

    /**
     * Admin-only endpoint. Requires ROLE_ADMIN authority.
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "message", "Admin access granted",
                "adminUser", jwt.getClaim("preferred_username"),
                "note", "In production, query your user store here"
        );
    }

    /**
     * Returns full JWT token metadata. Any authenticated user.
     * Useful for debugging token claims during development.
     */
    @GetMapping("/token-info")
    public Map<String, Object> tokenInfo(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
                "subject", jwt.getSubject(),
                "issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : "unknown",
                "issuedAt", jwt.getIssuedAt() != null ? jwt.getIssuedAt().toString() : "unknown",
                "expiresAt", jwt.getExpiresAt() != null ? jwt.getExpiresAt().toString() : "unknown",
                "allClaims", jwt.getClaims()
        );
    }

    /**
     * Public health check — no authentication required.
     */
    @GetMapping("/public/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "keycloak-spring-boot-oauth2"
        );
    }
}
