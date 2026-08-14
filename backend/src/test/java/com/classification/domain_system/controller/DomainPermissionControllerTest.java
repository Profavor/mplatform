package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.DomainAccessRequest;
import com.classification.domain_system.entity.DomainPermission;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.security.SecurityUtils;
import com.classification.domain_system.service.DomainPermissionService;
import com.classification.domain_system.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainPermissionControllerTest {

    @Mock
    private DomainPermissionService permissionService;

    @Mock
    private UserService userService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private DomainPermissionController domainPermissionController;

    @Test
    @DisplayName("사용자 검색 성공")
    void testSearchUsers_Success() {
        User user = new User();
        user.setId("u-123");
        user.setUsername("user01");

        Page<User> page = new PageImpl<>(List.of(user));
        when(userService.searchUsers(eq("user01"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<User>> response = domainPermissionController.searchUsers("user01", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    @DisplayName("사용자 도메인 권한 목록 조회 성공")
    void testGetUserPermissions_Success() {
        User user = new User();
        user.setId("u-123");
        DomainPermission perm = new DomainPermission();
        perm.setUser(user);

        when(permissionService.getUserPermissions("u-123")).thenReturn(List.of(perm));

        ResponseEntity<List<DomainPermission>> response = domainPermissionController.getUserPermissions("u-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("가용 도메인 목록 조회 성공")
    void testGetAvailableDomains_Success() {
        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user01");
        User user = new User();
        user.setId("u-123");
        user.setUsername("user01");
        when(userService.findByUsername("user01")).thenReturn(user);

        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());
        domain.setName(Map.of("ko", "금융"));

        when(permissionService.getAvailableDomains("u-123")).thenReturn(List.of(domain));

        ResponseEntity<List<Domain>> response = domainPermissionController.getAvailableDomains();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("도메인 권한 부여 및 회수 성공")
    void testGrantAndRevokePermission_Success() {
        UUID domainId = UUID.randomUUID();

        ResponseEntity<Void> grantResponse = domainPermissionController.grantPermission("u-123", domainId);
        assertEquals(HttpStatus.OK, grantResponse.getStatusCode());
        verify(permissionService).grantPermission("u-123", domainId);

        ResponseEntity<Void> revokeResponse = domainPermissionController.revokePermission("u-123", domainId);
        assertEquals(HttpStatus.OK, revokeResponse.getStatusCode());
        verify(permissionService).revokePermission("u-123", domainId);
    }

    @Test
    @DisplayName("도메인 접근 요청 및 승인/거절 성공")
    void testAccessRequestFlow_Success() {
        UUID domainId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();

        when(securityUtils.getCurrentUserIdOrThrow()).thenReturn("user01");
        User user = new User();
        user.setId("u-123");
        when(userService.findByUsername("user01")).thenReturn(user);

        DomainAccessRequest accessRequest = new DomainAccessRequest();
        accessRequest.setId(requestId);
        when(permissionService.requestAccess("u-123", domainId)).thenReturn(accessRequest);

        ResponseEntity<DomainAccessRequest> reqResponse = domainPermissionController.requestAccess(Map.of("domainId", domainId.toString()));
        assertEquals(HttpStatus.OK, reqResponse.getStatusCode());
        assertNotNull(reqResponse.getBody());

        when(permissionService.getPendingRequests()).thenReturn(List.of(accessRequest));
        ResponseEntity<List<DomainAccessRequest>> pendingResponse = domainPermissionController.getPendingRequests();
        assertEquals(HttpStatus.OK, pendingResponse.getStatusCode());
        assertEquals(1, pendingResponse.getBody().size());

        ResponseEntity<Void> approveResponse = domainPermissionController.approveRequest(requestId);
        assertEquals(HttpStatus.OK, approveResponse.getStatusCode());
        verify(permissionService).approveRequest(requestId);

        ResponseEntity<Void> rejectResponse = domainPermissionController.rejectRequest(requestId);
        assertEquals(HttpStatus.OK, rejectResponse.getStatusCode());
        verify(permissionService).rejectRequest(requestId);
    }
}
