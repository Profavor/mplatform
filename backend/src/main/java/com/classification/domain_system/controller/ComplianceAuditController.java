package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ComplianceAuditReportDto;
import com.classification.domain_system.service.ComplianceAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
public class ComplianceAuditController {

    private final ComplianceAuditService complianceAuditService;

    @GetMapping("/records/{recordId}/report")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'admin:read')")
    public ResponseEntity<ComplianceAuditReportDto> getRecordLifecycleReport(@PathVariable UUID recordId) {
        return ResponseEntity.ok(complianceAuditService.getRecordLifecycleReport(recordId));
    }
}
