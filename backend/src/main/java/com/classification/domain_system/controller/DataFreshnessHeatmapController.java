package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataFreshnessHeatmapDto;
import com.classification.domain_system.service.DataFreshnessHeatmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/freshness-heatmap")
@RequiredArgsConstructor
public class DataFreshnessHeatmapController {

    private final DataFreshnessHeatmapService dataFreshnessHeatmapService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<DataFreshnessHeatmapDto.FreshnessHeatmapResponse> getFreshnessHeatmap() {
        return ResponseEntity.ok(dataFreshnessHeatmapService.getFreshnessHeatmap());
    }
}
