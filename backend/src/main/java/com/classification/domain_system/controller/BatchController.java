package com.classification.domain_system.controller;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.BatchJob;
import com.classification.domain_system.service.BatchImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchImportService batchImportService;
    private final AuthContext authContext;

    @PostMapping("/import")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<BatchJob> createBatch(
            @RequestParam UUID domainId,
            @RequestParam UUID nodeId,
            @RequestParam String sourceSystem,
            @RequestBody List<Map<String, Object>> records) {
        String createdBy = authContext.getUserId();
        return ResponseEntity.ok(batchImportService.createBatch(domainId, nodeId, records, sourceSystem, createdBy));
    }

    @PostMapping("/{batchId}/validate")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<Void> validateBatch(@PathVariable UUID batchId) {
        batchImportService.validateBatch(batchId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{batchId}/commit")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<BatchJob> commitBatch(@PathVariable UUID batchId) {
        BatchJob job = batchImportService.commitBatch(batchId);
        return ResponseEntity.ok(job);
    }
}
