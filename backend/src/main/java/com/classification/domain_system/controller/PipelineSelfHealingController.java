package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PipelineSelfHealingDto;
import com.classification.domain_system.service.PipelineSelfHealingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/pipeline-healing")
@RequiredArgsConstructor
public class PipelineSelfHealingController {

    private final PipelineSelfHealingService pipelineSelfHealingService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'integration:read')")
    public ResponseEntity<PipelineSelfHealingDto.PipelineHealingReport> getHealingReport() {
        return ResponseEntity.ok(pipelineSelfHealingService.getHealingReport());
    }

    @PostMapping("/trigger")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'integration:write')")
    public ResponseEntity<Boolean> triggerHealing(
            @RequestBody PipelineSelfHealingDto.TriggerHealingRequest request) {
        return ResponseEntity.ok(pipelineSelfHealingService.triggerHealing(request.getPipelineChannel()));
    }
}
