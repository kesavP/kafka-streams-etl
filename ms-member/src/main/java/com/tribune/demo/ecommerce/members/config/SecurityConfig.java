package com.tribune.demo.ecommerce.members.config;

import com.tribune.demo.ecommerce.members.security.KeycloakRoleConverter;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Security configuration for Keycloak OAuth2 Resource Server.
 *
 * Key decisions:
 * - Stateless session (JWT-based, no server-side session)
 * - Custom JWT converter for Keycloak realm role extraction
 * - Audience validation to prevent cross-service token reuse
 *
 * Full tutorial: https://www.iamdevbox.com/posts/keycloak-spring-boot-oauth2-integration-complete-guide/
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final KeycloakRoleConverter keycloakRoleConverter;
    private final OAuth2ResourceServerProperties resourceServerProperties;

    public SecurityConfig(KeycloakRoleConverter keycloakRoleConverter,
                          OAuth2ResourceServerProperties resourceServerProperties) {
        this.keycloakRoleConverter = keycloakRoleConverter;
        this.resourceServerProperties = resourceServerProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Stateless REST API — no session cookies
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);
        return converter;
    }

    /**
     * JWT decoder with audience validation.
     *
     * Validates that the token's 'aud' claim contains the expected client ID.
     * This prevents a token issued for service-A from being used on service-B.
     *
     * To enable audience in Keycloak: Client -> Client Scopes -> Add Mapper
     * -> Audience -> include client ID in access token.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String issuerUri = resourceServerProperties.getJwt().getIssuerUri();
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuerUri);

        // Audience validation — optional but recommended for production
        // Comment out if you haven't configured audience in Keycloak yet
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD,
                aud -> aud == null || aud.contains("demo-app") // null = allow missing aud (dev mode)
        );

        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuerUri),
                audienceValidator
        );

        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }
}