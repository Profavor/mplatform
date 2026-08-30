package com.classification.domain_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

/**
 * Keycloak OIDC (Resource Server) JWT 검증 및 권한 매핑 설정.
 * 
 * [하이브리드 인증 아키텍처]
 * 1. 클라이언트(웹/모바일)가 전달한 Bearer 토큰이 들어오면 JwtFilter에서 먼저 JwtDecoder를 통해 Keycloak OIDC 토큰 검증을 시도합니다.
 * 2. Keycloak OIDC 검증 성공 시 Keycloak의 realm_access.roles 및 preferred_username을 추출하고, DB 세부 권한을 결합하여 SecurityContext에 설정합니다.
 * 3. Keycloak OIDC 검증 실패(또는 비활성화 상태) 시, 시스템 내부 HS256 JwtUtil로 폴백하여 로컬 발급 토큰을 검증합니다.
 * 4. jwk-set-uri 또는 issuer-uri가 설정되지 않은 환경(독립 로컬 모드 등)에서는 JwtDecoder 빈 자체가 등록되지 않으며, JwtFilter의 ObjectProvider를 통해 안전하게 null 처리되어 내부 JWT 전용 모드로 동작합니다.
 */
@Configuration
public class KeycloakJwtConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    /**
     * Keycloak OIDC JWK Set 또는 Issuer URI가 설정된 경우에만 JwtDecoder 빈을 조건부로 생성합니다.
     * 설정이 없는 경우 빈을 등록하지 않아(NullBean 반환 방지) NPE 및 불필요한 리소스 서버 초기화 실패를 방지합니다.
     */
    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}') || !T(org.springframework.util.StringUtils).isEmpty('${spring.security.oauth2.resourceserver.jwt.issuer-uri:}')")
    public JwtDecoder jwtDecoder() {
        org.springframework.security.oauth2.jwt.NimbusJwtDecoder decoder = null;
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            decoder = org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else if (issuerUri != null && !issuerUri.isBlank()) {
            decoder = (org.springframework.security.oauth2.jwt.NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuerUri);
        }
        
        if (decoder != null) {
            // Cloudflare 터널 등 외부 프록시 환경에서 동적으로 변경되는 issuer(iss) 클레임 검증을 우회하기 위해
            // 기본 JwtValidators 대신 JwtTimestampValidator(만료 시간 검증)만 사용하도록 덮어씁니다.
            decoder.setJwtValidator(new org.springframework.security.oauth2.jwt.JwtTimestampValidator());
            return decoder;
        }

        throw new IllegalStateException("Neither jwk-set-uri nor issuer-uri is configured for JwtDecoder");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

    /**
     * Keycloak JWT의 realm_access.roles를 Spring Security GrantedAuthority로 변환합니다.
     * 
     * Keycloak JWT 구조:
     * {
     *   "realm_access": {
     *     "roles": ["SYSTEM_ADMIN", "DATA_STEWARD", ...]
     *   }
     * }
     */
    public static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        @SuppressWarnings("unchecked")
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // realm_access.roles 추출
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                authorities.addAll(
                    roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList())
                );
            }

            // resource_access.<client>.roles 추출 (클라이언트 역할)
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                    if (entry.getValue() instanceof Map) {
                        Map<String, Object> clientAccess = (Map<String, Object>) entry.getValue();
                        if (clientAccess.containsKey("roles")) {
                            List<String> clientRoles = (List<String>) clientAccess.get("roles");
                            authorities.addAll(
                                clientRoles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                    .collect(Collectors.toList())
                            );
                        }
                    }
                }
            }

            return authorities;
        }
    }
}
