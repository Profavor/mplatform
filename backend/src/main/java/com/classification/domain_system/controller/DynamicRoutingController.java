package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ApprovalRoutingDto;
import com.classification.domain_system.service.DynamicRoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals/routing-templates")
@RequiredArgsConstructor
public class DynamicRoutingController {

    private final DynamicRoutingService dynamicRoutingService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'workflow:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<ApprovalRoutingDto.TemplateResponse>> getTemplates(
            @RequestParam(required = false) UUID domainId) {
        return ResponseEntity.ok(dynamicRoutingService.getTemplates(domainId));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'workflow:write') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApprovalRoutingDto.TemplateResponse> createTemplate(
            @RequestBody ApprovalRoutingDto.TemplateCreateRequest request) {
        return ResponseEntity.ok(dynamicRoutingService.createTemplate(request));
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasPermission(null, 'workflow:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<ApprovalRoutingDto.EvaluateRouteResponse> evaluateRoute(
            @RequestBody ApprovalRoutingDto.EvaluateRouteRequest request) {
        return ResponseEntity.ok(dynamicRoutingService.evaluateRoute(request));
    }
}
