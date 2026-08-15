package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PipelineScheduleDto;
import com.classification.domain_system.service.CrossDomainSyncPipelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/integration/pipelines")
@RequiredArgsConstructor
public class CrossDomainPipelineController {

    private final CrossDomainSyncPipelineService crossDomainSyncPipelineService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'integration:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<PipelineScheduleDto.SyncPipelineItem>> getPipelines() {
        return ResponseEntity.ok(crossDomainSyncPipelineService.getPipelines());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'integration:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<PipelineScheduleDto.SyncPipelineItem> createPipeline(
            @RequestBody PipelineScheduleDto.CreatePipelineRequest request) {
        return ResponseEntity.ok(crossDomainSyncPipelineService.createPipeline(request));
    }

    @PostMapping("/{pipelineId}/trigger")
    @PreAuthorize("hasPermission(null, 'integration:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<PipelineScheduleDto.PipelineTriggerResponse> triggerPipeline(
            @PathVariable String pipelineId) {
        return ResponseEntity.ok(crossDomainSyncPipelineService.triggerPipeline(pipelineId));
    }
}
