package com.classification.domain_system.security;

import com.classification.domain_system.service.PermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.classification.domain_system.context.AuthContext;

class JwtFilterTest {

    private JwtUtil jwtUtil;
    private PermissionService permissionService;
    private AuthContext authContext;
    private JwtFilter jwtFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtUtil = new JwtUtil();
        String testSecret = "test_secret_key_for_jwt_which_must_be_at_least_256_bits_long_for_hs256";
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationSec", 1800L);
        jwtUtil.init();

        permissionService = mock(PermissionService.class);
        when(permissionService.getAuthoritiesForUser(anyString(), anyString()))
                .thenReturn(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));

        authContext = new AuthContext();
        jwtFilter = new JwtFilter(jwtUtil, permissionService, authContext);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("유효한 JWT 토큰 요청 시 IP 변경 여부와 무관하게 인증 성공")
    void doFilterInternal_Success() throws Exception {
        String token = jwtUtil.generateToken("user1", "ADMIN", "uuid-1");

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(request.getServletPath()).thenReturn("/api/data");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100"); // Different IP

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user1", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰 요청 시 인증되지 않음")
    void doFilterInternal_InvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(request.getServletPath()).thenReturn("/api/data");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("SSE 요청 등 URL query string token (?token=...) 으로 인증 성공")
    void doFilterInternal_QueryTokenSuccess() throws Exception {
        String token = jwtUtil.generateToken("user1", "ADMIN", "uuid-1");

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getParameter("token")).thenReturn(token);
        when(request.getServletPath()).thenReturn("/api/notifications/subscribe");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user1", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
