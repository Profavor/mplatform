package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ColumnMaskingPolicyDto;
import com.classification.domain_system.dto.DataScopePermissionDto;
import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.dto.PermissionAuditLogDto;
import com.classification.domain_system.security.SecurityUtils;
import com.classification.domain_system.service.ColumnMaskingPolicyService;
import com.classification.domain_system.service.DataScopePermissionService;
import com.classification.domain_system.service.PermissionAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class DataScopePermissionController {

    private final DataScopePermissionService scopeService;
    private final ColumnMaskingPolicyService maskingPolicyService;
    private final PermissionAuditService auditService;
    private final SecurityUtils securityUtils;

    // 1. Data Scopes (도메인/노드 접근 범위 제어)
    @GetMapping("/users/{userId}/scopes")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'user:read')")
    public ResponseEntity<List<DataScopePermissionDto.Response>> getUserScopes(@PathVariable String userId) {
        return ResponseEntity.ok(scopeService.getUserScopes(userId));
    }

    @PostMapping("/users/{userId}/scopes")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'user:write')")
    public ResponseEntity<DataScopePermissionDto.Response> grantScope(
            @PathVariable String userId,
            @RequestBody DataScopePermissionDto.Request request) {
        String operator = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(scopeService.grantScope(userId, request, operator));
    }

    @DeleteMapping("/scopes/{scopeId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'user:write')")
    public ResponseEntity<Void> revokeScope(@PathVariable UUID scopeId) {
        String operator = securityUtils.getCurrentUserIdOrThrow();
        scopeService.revokeScope(scopeId, operator);
        return ResponseEntity.ok().build();
    }

    // 2. Permission Audit Logs (감사 로그 조회)
    @GetMapping("/audit-logs")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'user:read')")
    public ResponseEntity<PageResponse<PermissionAuditLogDto.Response>> getAuditLogs(
            @RequestParam(required = false) String targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Page<PermissionAuditLogDto.Response> result = auditService.getAuditLogs(
                targetUserId, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.of(result));
    }

    // 3. Column-Level Masking Policies (컬럼 수준 마스킹 정책 관리)
    @GetMapping("/masking-policies")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'user:read')")
    public ResponseEntity<List<ColumnMaskingPolicyDto.Response>> getMaskingPolicies(
            @RequestParam(required = false) UUID domainId) {
        return ResponseEntity.ok(maskingPolicyService.getPolicies(domainId));
    }

    @PostMapping("/masking-policies")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'user:write')")
    public ResponseEntity<ColumnMaskingPolicyDto.Response> createMaskingPolicy(
            @RequestBody ColumnMaskingPolicyDto.Request request) {
        String operator = securityUtils.getCurrentUserIdOrThrow();
        return ResponseEntity.ok(maskingPolicyService.createPolicy(request, operator));
    }

    @DeleteMapping("/masking-policies/{policyId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'user:write')")
    public ResponseEntity<Void> deleteMaskingPolicy(@PathVariable UUID policyId) {
        String operator = securityUtils.getCurrentUserIdOrThrow();
        maskingPolicyService.deletePolicy(policyId, operator);
        return ResponseEntity.ok().build();
    }
}
