package com.tribune.demo.ecommerce.members.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts Keycloak JWT token claims to Spring Security GrantedAuthority objects.
 *
 * Keycloak places realm roles inside realm_access.roles (nested JSON), not in
 * the standard OAuth2 scope claim. This converter bridges that gap.
 *
 * Full explanation: https://www.iamdevbox.com/posts/keycloak-spring-boot-oauth2-integration-complete-guide/
 */
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    // System roles assigned to all Keycloak users — typically not meaningful for app authorization
    private static final Set<String> KEYCLOAK_SYSTEM_ROLES = Set.of(
            "offline_access", "uma_authorization", "default-roles-demo"
    );

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Extract realm-level roles from realm_access.roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.getOrDefault("roles", Collections.emptyList());
            realmRoles.stream()
                    .filter(role -> !KEYCLOAK_SYSTEM_ROLES.contains(role))
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .forEach(authorities::add);
        }

        // 2. Extract client-level roles from resource_access.{clientId}.roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            resourceAccess.values().stream()
                    .filter(v -> v instanceof Map)
                    .flatMap(v -> {
                        Map<String, Object> clientAccess = (Map<String, Object>) v;
                        List<String> clientRoles = (List<String>) clientAccess.getOrDefault("roles", Collections.emptyList());
                        return clientRoles.stream();
                    })
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .forEach(authorities::add);
        }

        return authorities;
    }
}