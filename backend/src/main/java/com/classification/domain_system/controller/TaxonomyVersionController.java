package com.classification.domain_system.controller;

import com.classification.domain_system.entity.TaxonomyVersion;
import com.classification.domain_system.service.TaxonomyVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/domains/{domainId}/taxonomy-versions")
@RequiredArgsConstructor
public class TaxonomyVersionController {

    private final TaxonomyVersionService taxonomyVersionService;

    @PostMapping
    public ResponseEntity<TaxonomyVersion> createSnapshot(
            @PathVariable UUID domainId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(taxonomyVersionService.createSnapshot(domainId, request.get("label"), request.get("publishedBy")));
    }

    @GetMapping
    public ResponseEntity<List<TaxonomyVersion>> getVersions(@PathVariable UUID domainId) {
        return ResponseEntity.ok(taxonomyVersionService.getVersions(domainId));
    }

    @GetMapping("/{versionId}/data")
    public ResponseEntity<String> getSnapshotData(@PathVariable UUID domainId, @PathVariable UUID versionId) {
        return ResponseEntity.ok(taxonomyVersionService.getSnapshotData(versionId));
    }
}
