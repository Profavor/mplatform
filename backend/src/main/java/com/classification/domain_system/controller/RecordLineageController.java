package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RecordLineageDto;
import com.classification.domain_system.service.RecordLineageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/records")
public class RecordLineageController {

    private final RecordLineageService recordLineageService;

    public RecordLineageController(RecordLineageService recordLineageService) {
        this.recordLineageService = recordLineageService;
    }

    @GetMapping("/{recordId}/lineage")
    @PreAuthorize("hasPermission(null, 'record:read') or hasPermission(null, 'domain:read') or isAuthenticated()")
    public ResponseEntity<RecordLineageDto.RecordLineageResponse> getRecordLineage(@PathVariable UUID recordId) {
        RecordLineageDto.RecordLineageResponse lineage = recordLineageService.getRecordLineage(recordId);
        return ResponseEntity.ok(lineage);
    }
}
