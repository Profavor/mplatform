package com.classification.domain_system.controller;

import com.classification.domain_system.dto.MultilingualSyncDto;
import com.classification.domain_system.service.MultilingualSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/multilingual")
@RequiredArgsConstructor
public class MultilingualSyncController {

    private final MultilingualSyncService multilingualSyncService;

    @GetMapping("/scan")
    @PreAuthorize("hasPermission(null, 'schema:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<MultilingualSyncDto.SyncPlanResponse> scanMissingLocales(
            @PathVariable UUID domainId) {
        return ResponseEntity.ok(multilingualSyncService.scanMissingLocales(domainId));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasPermission(null, 'schema:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<MultilingualSyncDto.ApplySyncResult> syncFromGlossary(
            @PathVariable UUID domainId) {
        return ResponseEntity.ok(multilingualSyncService.syncFromGlossary(domainId));
    }
}
