package com.classification.domain_system.controller;

import com.classification.domain_system.dto.BulkImportDto;
import com.classification.domain_system.service.BulkImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/records/bulk-import")
@RequiredArgsConstructor
public class BulkImportController {

    private final BulkImportService bulkImportService;

    @PostMapping("/jobs")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<BulkImportDto.Progress> startImportJob(@RequestBody BulkImportDto.Request request) {
        String currentUserId = "system";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            currentUserId = auth.getName();
        }
        return ResponseEntity.ok(bulkImportService.startImportJob(request, currentUserId));
    }

    @GetMapping("/jobs/{jobId}/progress")
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<BulkImportDto.Progress> getJobProgress(@PathVariable UUID jobId) {
        return ResponseEntity.ok(bulkImportService.getJobProgress(jobId));
    }
}
