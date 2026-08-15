package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ColdStorageArchiveDto;
import com.classification.domain_system.service.ColdStorageArchiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/archives")
@RequiredArgsConstructor
public class ColdStorageArchiveController {

    private final ColdStorageArchiveService coldStorageArchiveService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'compliance:read')")
    public ResponseEntity<List<ColdStorageArchiveDto.ArchivePackageInfo>> getArchives() {
        return ResponseEntity.ok(coldStorageArchiveService.getArchives());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'compliance:write')")
    public ResponseEntity<ColdStorageArchiveDto.ArchivePackageInfo> createArchive(
            @RequestBody ColdStorageArchiveDto.CreateArchiveRequest request) {
        return ResponseEntity.ok(coldStorageArchiveService.createArchive(request));
    }

    @PostMapping("/{archiveId}/dr-simulate")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'compliance:write')")
    public ResponseEntity<ColdStorageArchiveDto.DrSimulationResult> simulateDrRestore(
            @PathVariable String archiveId) {
        return ResponseEntity.ok(coldStorageArchiveService.simulateDrRestore(archiveId));
    }
}
