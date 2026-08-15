package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DataAssetDto;
import com.classification.domain_system.service.DataAssetValuationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog/valuation")
@RequiredArgsConstructor
public class DataAssetValuationController {

    private final DataAssetValuationService dataAssetValuationService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'domain:read') or hasPermission(null, 'compliance:read')")
    public ResponseEntity<DataAssetDto.DataAssetSummaryResponse> getValuationSummary() {
        return ResponseEntity.ok(dataAssetValuationService.evaluateDataAssets());
    }
}
