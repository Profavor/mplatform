package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SchemaImpactAnalysisDto;
import com.classification.domain_system.service.SchemaImpactAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains")
public class SchemaImpactAnalysisController {

    private final SchemaImpactAnalysisService schemaImpactAnalysisService;

    public SchemaImpactAnalysisController(SchemaImpactAnalysisService schemaImpactAnalysisService) {
        this.schemaImpactAnalysisService = schemaImpactAnalysisService;
    }

    @PostMapping("/{domainId}/impact-analysis")
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'schema:write') or isAuthenticated()")
    public ResponseEntity<SchemaImpactAnalysisDto.ImpactAnalysisResponse> analyzeImpact(
            @PathVariable UUID domainId,
            @RequestBody SchemaImpactAnalysisDto.ImpactAnalysisRequest request) {
        SchemaImpactAnalysisDto.ImpactAnalysisResponse response = schemaImpactAnalysisService.analyzeImpact(domainId, request);
        return ResponseEntity.ok(response);
    }
}
