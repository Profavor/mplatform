package com.classification.domain_system.controller;

import com.classification.domain_system.entity.BatchJob;
import com.classification.domain_system.service.BatchImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchImportService batchImportService;

    @PostMapping("/import")
    public ResponseEntity<BatchJob> createBatch(
            @RequestParam UUID domainId,
            @RequestParam UUID nodeId,
            @RequestParam String sourceSystem,
            @RequestParam String createdBy,
            @RequestBody List<Map<String, Object>> records) {
        return ResponseEntity.ok(batchImportService.createBatch(domainId, nodeId, records, sourceSystem, createdBy));
    }

    @PostMapping("/{batchId}/validate")
    public ResponseEntity<Void> validateBatch(@PathVariable UUID batchId) {
        batchImportService.validateBatch(batchId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{batchId}/commit")
    public ResponseEntity<Void> commitBatch(@PathVariable UUID batchId) {
        batchImportService.commitBatch(batchId);
        return ResponseEntity.ok().build();
    }
}
