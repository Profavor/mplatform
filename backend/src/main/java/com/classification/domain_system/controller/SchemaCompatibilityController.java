package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SchemaCompatibilityDto;
import com.classification.domain_system.service.SchemaCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/schema/compatibility-check")
@RequiredArgsConstructor
public class SchemaCompatibilityController {

    private final SchemaCompatibilityService schemaCompatibilityService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'schema:write')")
    public ResponseEntity<SchemaCompatibilityDto.SchemaCompatibilityReport> checkCompatibility(
            @PathVariable UUID domainId,
            @RequestBody SchemaCompatibilityDto.SchemaChangeSimulationRequest request) {
        return ResponseEntity.ok(schemaCompatibilityService.analyzeCompatibility(domainId, request.getProposedChanges()));
    }
}
