package com.classification.domain_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakJwtConfigTest {

    @Test
    void testKeycloakRealmRoleConverter() {
        KeycloakJwtConfig.KeycloakRealmRoleConverter converter = new KeycloakJwtConfig.KeycloakRealmRoleConverter();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("system_admin", "data_steward")))
                .claim("resource_access", Map.of(
                        "mdm-frontend", Map.of("roles", List.of("viewer"))
                ))
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertEquals(3, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DATA_STEWARD")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER")));
    }

    @Test
    void testJwtDecoderCreationWithJwkSetUri() {
        KeycloakJwtConfig config = new KeycloakJwtConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(config, "jwkSetUri", "http://localhost:8081/realms/mplatform/protocol/openid-connect/certs");

        org.springframework.security.oauth2.jwt.JwtDecoder decoder = config.jwtDecoder();
        org.junit.jupiter.api.Assertions.assertNotNull(decoder);
    }

    @Test
    void testJwtAuthenticationConverter() {
        KeycloakJwtConfig config = new KeycloakJwtConfig();
        var authConverter = config.jwtAuthenticationConverter();
        org.junit.jupiter.api.Assertions.assertNotNull(authConverter);
    }
}
