package com.classification.domain_system.controller;

import com.classification.domain_system.dto.SystemDiagnosticsDto;
import com.classification.domain_system.service.GlobalSystemDiagnosticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/diagnostics")
@RequiredArgsConstructor
public class GlobalSystemDiagnosticsController {

    private final GlobalSystemDiagnosticsService globalSystemDiagnosticsService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'compliance:read')")
    public ResponseEntity<SystemDiagnosticsDto.GlobalSystemDiagnosticsResponse> getSystemDiagnostics() {
        return ResponseEntity.ok(globalSystemDiagnosticsService.diagnoseSystem());
    }
}
