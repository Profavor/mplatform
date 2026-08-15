package com.classification.domain_system.controller;

import com.classification.domain_system.dto.VolumeAnomalyDto;
import com.classification.domain_system.service.VolumeAnomalyRadarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/volume-radar")
@RequiredArgsConstructor
public class VolumeAnomalyController {

    private final VolumeAnomalyRadarService volumeAnomalyRadarService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'integration:read')")
    public ResponseEntity<VolumeAnomalyDto.VolumeRadarResponse> getVolumeRadarData() {
        return ResponseEntity.ok(volumeAnomalyRadarService.getVolumeRadarData());
    }
}
