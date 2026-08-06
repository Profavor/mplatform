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

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final PermissionService permissionService;
    private final AuthContext authContext;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public JwtFilter(JwtUtil jwtUtil, PermissionService permissionService, AuthContext authContext, ObjectProvider<UserRepository> userRepositoryProvider) {
        this.jwtUtil = jwtUtil;
        this.permissionService = permissionService;
        this.authContext = authContext;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    public static JwtFilter createForTest(JwtUtil jwtUtil, PermissionService permissionService, AuthContext authContext, UserRepository userRepository) {
        return new JwtFilter(jwtUtil, permissionService, authContext, new SimpleObjectProvider<>(userRepository));
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
        }

        if (jwt != null) {
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
}
