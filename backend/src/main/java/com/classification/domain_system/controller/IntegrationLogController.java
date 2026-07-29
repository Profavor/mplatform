package com.classification.domain_system.controller;

import com.classification.domain_system.entity.IntegrationLog;
import com.classification.domain_system.repository.IntegrationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/admin/integration/logs")
@RequiredArgsConstructor
public class IntegrationLogController {

    private final IntegrationLogRepository repository;
    private final com.classification.domain_system.integration.IntegrationLogService logService;
    private final com.classification.domain_system.integration.IntegrationRetryService retryService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'log:read')")
    public Page<IntegrationLog> getLogs(
            @RequestParam(required = false) UUID channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (channelId != null) {
            return repository.findByChannelId(channelId, pr);
        }
        return repository.findAll(pr);
    }

    @GetMapping("/by-record/{recordId}")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'log:read')")
    public java.util.List<IntegrationLog> getLogsByRecordId(@PathVariable UUID recordId) {
        return repository.findByRecordIdOrderByCreatedAtDesc(recordId);
    }

    @PostMapping("/{logId}/retry")
    @PreAuthorize("hasPermission(null, 'admin:write')")
    public org.springframework.http.ResponseEntity<Void> retryLog(@PathVariable UUID logId) {
        logService.retryLog(logId);
        return org.springframework.http.ResponseEntity.ok().build();
    }

    @GetMapping("/dead-letter")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'log:read')")
    public com.classification.domain_system.dto.PageResponse<IntegrationLog> getDeadLetters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return com.classification.domain_system.dto.PageResponse.of(retryService.getDeadLetters(page, size));
    }

    @PostMapping("/dead-letter/retry-all")
    @PreAuthorize("hasPermission(null, 'admin:write')")
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> retryAllDeadLetters() {
        int retriedCount = retryService.retryAllDeadLetters();
        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("retriedCount", retriedCount));
    }
}
