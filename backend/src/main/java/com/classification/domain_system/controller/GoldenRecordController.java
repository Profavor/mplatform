package com.classification.domain_system.controller;

import com.classification.domain_system.dto.GoldenRecordDto;
import com.classification.domain_system.service.GoldenRecordBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/records/golden-record")
@RequiredArgsConstructor
public class GoldenRecordController {

    private final GoldenRecordBuilderService goldenRecordBuilderService;

    @PostMapping("/preview")
    @PreAuthorize("hasPermission(null, 'record:write') or hasPermission(null, 'domain:read')")
    public ResponseEntity<GoldenRecordDto.GoldenRecordPreviewResponse> previewGoldenRecord(
            @RequestBody GoldenRecordDto.GoldenRecordBuildRequest request) {
        return ResponseEntity.ok(goldenRecordBuilderService.buildGoldenRecord(request));
    }
}
