package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RecordLineageDto;
import com.classification.domain_system.service.RecordLineageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/lineage")
@RequiredArgsConstructor
public class DataLineageController {

    private final RecordLineageService lineageService;

    @GetMapping("/domains/{domainId}")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<RecordLineageDto.DomainLineageResponse> getDomainLineage(@PathVariable UUID domainId) {
        return ResponseEntity.ok(lineageService.getDomainLineage(domainId));
    }

    @GetMapping("/records/{recordId}")
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<RecordLineageDto.RecordLineageResponse> getRecordLineage(@PathVariable UUID recordId) {
        return ResponseEntity.ok(lineageService.getRecordLineage(recordId));
    }
}
