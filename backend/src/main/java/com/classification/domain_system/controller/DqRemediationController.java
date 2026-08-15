package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DqRemediationDto;
import com.classification.domain_system.service.DqRemediationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/dq/remediation")
@RequiredArgsConstructor
public class DqRemediationController {

    private final DqRemediationService remediationService;

    @PostMapping("/scan")
    @PreAuthorize("hasPermission(null, 'dq:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<DqRemediationDto.RemediationProposal>> scanAndPropose(@PathVariable UUID domainId) {
        return ResponseEntity.ok(remediationService.scanAndPropose(domainId));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasPermission(null, 'dq:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<DqRemediationDto.RemediationApplyResult> applyRemediations(
            @PathVariable UUID domainId,
            @RequestBody DqRemediationDto.RemediationApplyRequest request) {
        return ResponseEntity.ok(remediationService.applyRemediations(domainId, request));
    }
}
