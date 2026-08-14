package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SystemInstallRequest;
import com.classification.domain_system.dto.SystemInstallStatusResponse;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.service.AuthService;
import com.classification.domain_system.service.PermissionService;
import com.classification.domain_system.service.SystemInstallService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemInstallControllerTest {

    @Mock
    private SystemInstallService installService;

    @Mock
    private AuthService authService;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private SystemInstallController systemInstallController;

    @Test
    @DisplayName("시스템 설치 상태 조회 성공")
    void testGetInstallStatus_Success() {
        SystemInstallStatusResponse status = new SystemInstallStatusResponse(true, true);
        when(installService.getInstallStatus()).thenReturn(status);

        ResponseEntity<SystemInstallStatusResponse> response = systemInstallController.getInstallStatus();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isInstalled());
        assertTrue(response.getBody().isHasAdminAccount());
        verify(installService).getInstallStatus();
    }

    @Test
    @DisplayName("최초 시스템 설치 및 관리자 세팅 성공")
    void testInstallSystem_Success() {
        SystemInstallRequest request = new SystemInstallRequest();
        request.setAdminUsername("admin");
        request.setAdminPassword("admin1234!");

        User adminUser = new User();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setUsername("admin");
        adminUser.setRole("ROLE_ADMIN");

        when(installService.installSystem(any(SystemInstallRequest.class))).thenReturn(adminUser);
        when(authService.loginWithTokens(eq("admin"), eq("admin1234!"), eq("127.0.0.1"), anyString()))
                .thenReturn(Map.of("token", "jwt-token-123", "refreshToken", "refresh-token-123"));
        when(permissionService.getAuthoritiesForUser(eq("admin"), eq("ROLE_ADMIN")))
                .thenReturn(List.of(new SimpleGrantedAuthority("admin:read"), new SimpleGrantedAuthority("admin:write")));

        ResponseEntity<?> response = systemInstallController.installSystem(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> resultMap = (Map<?, ?>) response.getBody();
        assertEquals("jwt-token-123", resultMap.get("token"));
        assertEquals("admin", resultMap.get("username"));
        assertEquals("ROLE_ADMIN", resultMap.get("role"));
    }
}
