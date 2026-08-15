package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MultiRegionConflictDto;
import com.classification.domain_system.service.MultiRegionConflictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/multi-region-conflicts")
@RequiredArgsConstructor
public class MultiRegionConflictController {

    private final MultiRegionConflictService multiRegionConflictService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'integration:read')")
    public ResponseEntity<MultiRegionConflictDto.RegionSyncReport> getConflictReport() {
        return ResponseEntity.ok(multiRegionConflictService.getConflictReport());
    }

    @PostMapping("/{conflictId}/resolve")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'integration:write')")
    public ResponseEntity<Boolean> resolveConflict(
            @PathVariable String conflictId,
            @RequestBody MultiRegionConflictDto.ResolveConflictRequest request) {
        return ResponseEntity.ok(multiRegionConflictService.resolveConflict(conflictId, request.getChosenValue()));
    }
}
