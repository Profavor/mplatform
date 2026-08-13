package com.classification.domain_system.controller;

import com.classification.domain_system.entity.DomainPermission;
import com.classification.domain_system.entity.DomainAccessRequest;
import com.classification.domain_system.service.DomainPermissionService;
import com.classification.domain_system.service.UserService;
import com.classification.domain_system.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class DomainPermissionController {

    private final DomainPermissionService permissionService;
    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'user:read')")
    public ResponseEntity<org.springframework.data.domain.Page<User>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(userService.searchUsers(search, pageable));
    }

    @GetMapping("/users/{userId}/domains")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'user:read')")
    public ResponseEntity<List<DomainPermission>> getUserPermissions(@PathVariable String userId) {
        return ResponseEntity.ok(permissionService.getUserPermissions(userId));
    }

    @GetMapping("/domains/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<com.classification.domain_system.entity.Domain>> getAvailableDomains() {
        String userId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(userId);
        return ResponseEntity.ok(permissionService.getAvailableDomains(user.getId()));
    }

    @PostMapping("/users/{userId}/domains/{domainId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'user:write')")
    public ResponseEntity<Void> grantPermission(@PathVariable String userId, @PathVariable UUID domainId) {
        permissionService.grantPermission(userId, domainId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/domains/{domainId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'user:write')")
    public ResponseEntity<Void> revokePermission(@PathVariable String userId, @PathVariable UUID domainId) {
        permissionService.revokePermission(userId, domainId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DomainAccessRequest> requestAccess(@RequestBody Map<String, String> payload) {
        String userId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(userId);
        UUID domainId = UUID.fromString(payload.get("domainId"));
        return ResponseEntity.ok(permissionService.requestAccess(user.getId(), domainId));
    }

    @GetMapping("/requests/pending")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<DomainAccessRequest>> getPendingRequests() {
        return ResponseEntity.ok(permissionService.getPendingRequests());
    }

    @PostMapping("/requests/{requestId}/approve")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> approveRequest(@PathVariable UUID requestId) {
        permissionService.approveRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{requestId}/reject")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> rejectRequest(@PathVariable UUID requestId) {
        permissionService.rejectRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/requests/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelRequest(@PathVariable UUID requestId) {
        String userId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(userId);
        permissionService.cancelRequest(requestId, user != null ? user.getId() : userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasPermission(null, 'org:write')")
    public ResponseEntity<Void> updateUserRole(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        String role = payload.get("role");
        userService.updateUserRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/tenant-info")
    @PreAuthorize("hasPermission(null, 'org:write')")
    public ResponseEntity<Void> updateUserTenantInfo(@PathVariable String userId, @RequestBody Map<String, Object> payload) {
        String role = (String) payload.get("role");
        String orgIdStr = (String) payload.get("organizationId");
        String deptIdStr = (String) payload.get("departmentId");
        String teamIdStr = (String) payload.get("teamId");
        String email = (String) payload.get("email");

        UUID orgId = (orgIdStr != null && !orgIdStr.trim().isEmpty()) ? UUID.fromString(orgIdStr) : null;
        UUID deptId = (deptIdStr != null && !deptIdStr.trim().isEmpty()) ? UUID.fromString(deptIdStr) : null;
        UUID teamId = (teamIdStr != null && !teamIdStr.trim().isEmpty()) ? UUID.fromString(teamIdStr) : null;

        User updateReq = new User();
        updateReq.setRole(role);
        updateReq.setOrganizationId(orgId);
        updateReq.setDepartmentId(deptId);
        updateReq.setTeamId(teamId);
        if (email != null && !email.trim().isEmpty()) {
            updateReq.setEmail(email.trim());
        }

        userService.updateUserInfo(userId, updateReq);
        return ResponseEntity.ok().build();
    }
}
