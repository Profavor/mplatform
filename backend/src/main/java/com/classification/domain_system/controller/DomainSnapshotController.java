package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DomainSnapshotDto;
import com.classification.domain_system.service.DomainSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/domains")
@RequiredArgsConstructor
public class DomainSnapshotController {

    private final DomainSnapshotService domainSnapshotService;

    @GetMapping("/{domainId}/snapshots")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<DomainSnapshotDto.SnapshotResponse>> getSnapshots(
            @PathVariable UUID domainId) {
        return ResponseEntity.ok(domainSnapshotService.getSnapshots(domainId));
    }

    @PostMapping("/{domainId}/snapshots")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<DomainSnapshotDto.SnapshotResponse> createSnapshot(
            @PathVariable UUID domainId,
            @RequestBody DomainSnapshotDto.SnapshotCreateRequest request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "USER";
        return ResponseEntity.ok(domainSnapshotService.createSnapshot(domainId, request, username));
    }

    @PostMapping("/snapshots/{snapshotId}/restore")
    @PreAuthorize("hasPermission(null, 'domain:write') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<DomainSnapshotDto.SnapshotRestoreResponse> restoreSnapshot(
            @PathVariable UUID snapshotId) {
        return ResponseEntity.ok(domainSnapshotService.restoreSnapshot(snapshotId));
    }
}
