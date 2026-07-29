package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ClassificationAxisRequest;
import com.classification.domain_system.dto.ClassificationAxisResponse;
import com.classification.domain_system.service.ClassificationAxisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/axes")
@RequiredArgsConstructor
public class ClassificationAxisController {

    private final ClassificationAxisService axisService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<ClassificationAxisResponse>> getAxes(@PathVariable UUID domainId) {
        return ResponseEntity.ok(axisService.getAxesByDomain(domainId));
    }

    @GetMapping("/{axisId}")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<ClassificationAxisResponse> getAxis(@PathVariable UUID domainId, @PathVariable UUID axisId) {
        return ResponseEntity.ok(axisService.getAxisById(axisId));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<ClassificationAxisResponse> createAxis(@PathVariable UUID domainId, @RequestBody ClassificationAxisRequest request) {
        return ResponseEntity.ok(axisService.createAxis(domainId, request));
    }

    @PutMapping("/{axisId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<ClassificationAxisResponse> updateAxis(@PathVariable UUID domainId, @PathVariable UUID axisId, @RequestBody ClassificationAxisRequest request) {
        return ResponseEntity.ok(axisService.updateAxis(axisId, request));
    }

    @DeleteMapping("/{axisId}")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> deleteAxis(@PathVariable UUID axisId) {
        axisService.deleteAxis(axisId);
        return ResponseEntity.ok().build();
    }
}
