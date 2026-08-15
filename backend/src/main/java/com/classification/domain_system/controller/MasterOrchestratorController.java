package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MasterOrchestratorDto;
import com.classification.domain_system.service.MasterOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/master-orchestrator")
@RequiredArgsConstructor
public class MasterOrchestratorController {

    private final MasterOrchestratorService masterOrchestratorService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read')")
    public ResponseEntity<MasterOrchestratorDto.MasterOrchestratorSummary> getOrchestratorStatus() {
        return ResponseEntity.ok(masterOrchestratorService.getOrchestratorStatus());
    }
}
