package com.classification.domain_system.controller;

import com.classification.domain_system.dto.AutonomousCleansingDto;
import com.classification.domain_system.service.AutonomousCleansingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/dq/cleansing-proposals")
@RequiredArgsConstructor
public class AutonomousCleansingController {

    private final AutonomousCleansingService autonomousCleansingService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'domain:read') or hasPermission(null, 'record:read')")
    public ResponseEntity<AutonomousCleansingDto.CleansingProposalResponse> getCleansingProposals(
            @PathVariable UUID domainId) {
        return ResponseEntity.ok(autonomousCleansingService.getCleansingProposals(domainId));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'record:write')")
    public ResponseEntity<Integer> applyCleansing(
            @PathVariable UUID domainId,
            @RequestBody AutonomousCleansingDto.ApplyCleansingRequest request) {
        return ResponseEntity.ok(autonomousCleansingService.applyCleansing(domainId, request.getRecordCodes()));
    }
}
