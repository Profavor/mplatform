package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RegulatoryComplianceDto;
import com.classification.domain_system.service.RegulatoryComplianceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compliance/regulatory-audit")
@RequiredArgsConstructor
public class RegulatoryComplianceController {

    private final RegulatoryComplianceService regulatoryComplianceService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'compliance:read')")
    public ResponseEntity<RegulatoryComplianceDto.ComplianceAuditReport> runAudit() {
        return ResponseEntity.ok(regulatoryComplianceService.runAudit());
    }
}
