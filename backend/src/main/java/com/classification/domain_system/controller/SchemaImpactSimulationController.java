package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SchemaImpactSimulationDto;
import com.classification.domain_system.service.SchemaImpactAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/schema")
@RequiredArgsConstructor
public class SchemaImpactSimulationController {

    private final SchemaImpactAnalysisService schemaImpactAnalysisService;

    @PostMapping("/simulate-impact")
    @PreAuthorize("hasPermission(null, 'schema:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<SchemaImpactSimulationDto.SimulationResponse> simulateImpact(
            @PathVariable UUID domainId,
            @RequestBody SchemaImpactSimulationDto.SimulationRequest request) {
        return ResponseEntity.ok(schemaImpactAnalysisService.simulateImpact(domainId, request));
    }
}
