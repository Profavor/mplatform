package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ReferenceIntegrityDto;
import com.classification.domain_system.service.ReferenceIntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/integrity")
@RequiredArgsConstructor
public class ReferenceIntegrityController {

    private final ReferenceIntegrityService referenceIntegrityService;

    @PostMapping("/scan")
    @PreAuthorize("hasPermission(null, 'dq:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<ReferenceIntegrityDto.IntegrityReportResponse> scanDomainIntegrity(
            @PathVariable UUID domainId) {
        return ResponseEntity.ok(referenceIntegrityService.scanDomainIntegrity(domainId));
    }
}
