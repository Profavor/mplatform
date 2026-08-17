package com.classification.domain_system.security;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.service.PermissionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final AuthContext authContext;
    private final ObjectProvider<UserRepository> userRepositoryProvider;
    private final ObjectProvider<JwtDecoder> jwtDecoderProvider;

    public JwtFilter(JwtUtil jwtUtil, PermissionService permissionService, AuthContext authContext, 
                     ObjectProvider<UserRepository> userRepositoryProvider,
                     ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
        this.authContext = authContext;
        this.userRepositoryProvider = userRepositoryProvider;
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    public static JwtFilter createForTest(JwtUtil jwtUtil, PermissionService permissionService, AuthContext authContext, UserRepository userRepository) {
        return new JwtFilter(jwtUtil, permissionService, authContext, new SimpleObjectProvider<>(userRepository), new SimpleObjectProvider<>(null));
    }

    public static JwtFilter createForTest(JwtUtil jwtUtil, PermissionService permissionService, AuthContext authContext, UserRepository userRepository, JwtDecoder jwtDecoder) {
        return new JwtFilter(jwtUtil, permissionService, authContext, new SimpleObjectProvider<>(userRepository), new SimpleObjectProvider<>(jwtDecoder));
    }

    private static class SimpleObjectProvider<T> implements ObjectProvider<T> {
        private final T instance;
        SimpleObjectProvider(T instance) { this.instance = instance; }
        @Override public T getObject() { return instance; }
        @Override public T getObject(Object... args) { return instance; }
        @Override public T getIfAvailable() { return instance; }
        @Override public T getIfUnique() { return instance; }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Inbound Webhook 경로는 채널 자체 시크릿 토큰으로 인증하므로 JWT 필터 완전 제외
        return path.startsWith("/api/integration/inbound/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        } else if (request.getParameter("token") != null && !request.getParameter("token").isBlank()) {
            jwt = request.getParameter("token");
        } else if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName()) || "auth_token".equals(cookie.getName()) || "jwt".equals(cookie.getName())) {
                    if (cookie.getValue() != null && !cookie.getValue().isBlank()) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (jwt != null) {
            // 1. Keycloak JWT 시도 (issuer 기반 판별)
            if (tryKeycloakAuth(jwt, request)) {
                chain.doFilter(request, response);
                return;
            }
            // 2. 기존 자체 JWT 시도
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.debug("Failed to extract username from token", e);
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.isTokenValid(jwt)) {
                Claims claims = jwtUtil.extractAllClaims(jwt);
                String roleStr = claims.get("role", String.class);
                String userId = claims.get("userId", String.class);
                if (userId == null) {
                    userId = claims.get("uuid", String.class);
                }

                // 단일 세션(activeSessionId) 검증
                UserRepository repo = userRepositoryProvider != null ? userRepositoryProvider.getIfAvailable() : null;
                if (repo != null) {
                    var userOpt = repo.findByUsername(username);
                    if (userOpt.isPresent()) {
                        String activeSessionId = userOpt.get().getActiveSessionId();
                        String tokenSessionId = (String) claims.get("sessionId");
                        log.debug("[JwtFilter Check] User: {}, DB activeSessionId: {}, Token sessionId: {}", username, activeSessionId, tokenSessionId);
                        // DB에 activeSessionId가 설정되어 있는 경우, 토큰의 sessionId가 없거나 다르면 기존 세션이므로 즉시 차단
                        if (activeSessionId != null && !activeSessionId.equals(tokenSessionId)) {
                            log.warn("Session invalidated due to concurrent login for user: {}", username);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired due to login from another device.");
                            return;
                        }
                    }
                }

                authContext.setUserId(userId != null ? userId : username);

                Collection<GrantedAuthority> authorities = permissionService.getAuthoritiesForUser(username, roleStr);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null, authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private boolean tryKeycloakAuth(String token, HttpServletRequest request) {
        JwtDecoder decoder = jwtDecoderProvider != null ? jwtDecoderProvider.getIfAvailable() : null;
        if (decoder == null) return false;

        try {
            Jwt jwt = decoder.decode(token);
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            String sub = jwt.getSubject();
            if (preferredUsername == null) return false;

            UserRepository repo = userRepositoryProvider != null ? userRepositoryProvider.getIfAvailable() : null;
            String effectiveUserId = sub;
            if (repo != null) {
                var userOpt = repo.findByUsername(preferredUsername);
                if (userOpt.isPresent() && userOpt.get().getId() != null) {
                    effectiveUserId = userOpt.get().getId();
                }
            }

            authContext.setUserId(effectiveUserId != null ? effectiveUserId : preferredUsername);

            // realm_access.roles → GrantedAuthority
            List<GrantedAuthority> authorities = new java.util.ArrayList<>();
            List<String> keycloakRoles = new java.util.ArrayList<>();
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                keycloakRoles = (List<String>) realmAccess.get("roles");
                authorities.addAll(keycloakRoles.stream()
                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                    .collect(Collectors.toList()));
            }

            // DB 기반 세부 권한(domain:read, admin:write 등) 로딩
            String roleStr = keycloakRoles.stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining(","));
            Collection<GrantedAuthority> dbAuthorities = permissionService.getAuthoritiesForUser(preferredUsername, roleStr);
            authorities.addAll(dbAuthorities);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    preferredUsername, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Keycloak JWT authenticated: {} with {} authorities", preferredUsername, authorities.size());
            return true;
        } catch (Exception e) {
            log.debug("Not a valid Keycloak JWT, falling back to internal JWT", e);
            return false;
        }
    }
}
