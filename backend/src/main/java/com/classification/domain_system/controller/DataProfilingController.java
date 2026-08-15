package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataProfilingReportDto;
import com.classification.domain_system.service.DataProfilingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/profiling")
@RequiredArgsConstructor
public class DataProfilingController {

    private final DataProfilingService dataProfilingService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<java.util.List<com.classification.domain_system.dto.DataProfilingResponse>> getDomainProfiling(@PathVariable UUID domainId) {
        return ResponseEntity.ok(dataProfilingService.profileDomainData(domainId));
    }

    @GetMapping("/report")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<DataProfilingReportDto> getProfilingReport(@PathVariable UUID domainId) {
        return ResponseEntity.ok(dataProfilingService.getProfilingReport(domainId));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<DataProfilingReportDto> runProfilingScan(@PathVariable UUID domainId) {
        return ResponseEntity.ok(dataProfilingService.getProfilingReport(domainId));
    }
}
