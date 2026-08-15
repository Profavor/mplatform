package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataRetentionDto;
import com.classification.domain_system.service.DataRetentionPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/retention")
@RequiredArgsConstructor
public class DataRetentionController {

    private final DataRetentionPolicyService dataRetentionPolicyService;

    @GetMapping("/scan")
    @PreAuthorize("hasPermission(null, 'compliance:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<DataRetentionDto.ExpiredRecordScanResponse> scanExpired(
            @PathVariable UUID domainId,
            @RequestParam(defaultValue = "3") int years) {
        return ResponseEntity.ok(dataRetentionPolicyService.scanExpiredRecords(domainId, years));
    }

    @PostMapping("/purge")
    @PreAuthorize("hasPermission(null, 'compliance:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<DataRetentionDto.PurgeExecutionResponse> purgeRecords(
            @PathVariable UUID domainId,
            @RequestBody DataRetentionDto.PurgeRequest request) {
        return ResponseEntity.ok(dataRetentionPolicyService.purgeRecords(domainId, request.getRetentionYears(), request.getPurgeType()));
    }
}
