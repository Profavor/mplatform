package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MultiTenantRouterDto;
import com.classification.domain_system.service.MultiTenantRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/multi-tenant/routing-rules")
@RequiredArgsConstructor
public class MultiTenantRouterController {

    private final MultiTenantRouterService multiTenantRouterService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read')")
    public ResponseEntity<MultiTenantRouterDto.TenantRoutingResponse> getRoutingRules() {
        return ResponseEntity.ok(multiTenantRouterService.getRoutingRules());
    }

    @PostMapping("/{tenantCode}/toggle")
    @PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Boolean> toggleRule(
            @PathVariable String tenantCode,
            @RequestParam boolean active) {
        return ResponseEntity.ok(multiTenantRouterService.toggleRule(tenantCode, active));
    }
}
