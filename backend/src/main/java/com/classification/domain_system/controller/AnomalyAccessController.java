package com.classification.domain_system.controller;

import com.classification.domain_system.dto.AnomalyAccessDto;
import com.classification.domain_system.service.AnomalyAccessDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/anomaly-detection")
@RequiredArgsConstructor
public class AnomalyAccessController {

    private final AnomalyAccessDetectionService anomalyAccessDetectionService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'compliance:read') or hasPermission(null, 'admin:read')")
    public ResponseEntity<AnomalyAccessDto.AnomalyDetectionSummaryResponse> getAnomalyEvents() {
        return ResponseEntity.ok(anomalyAccessDetectionService.detectSecurityEvents());
    }

    @PostMapping("/block")
    @PreAuthorize("hasPermission(null, 'compliance:write') or hasPermission(null, 'admin:write')")
    public ResponseEntity<Boolean> blockActor(@RequestBody AnomalyAccessDto.BlockUserRequest request) {
        return ResponseEntity.ok(anomalyAccessDetectionService.blockSuspiciousActor(request.getUserId()));
    }
}
