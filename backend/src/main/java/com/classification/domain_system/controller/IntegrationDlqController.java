package com.classification.domain_system.controller;

import com.classification.domain_system.dto.IntegrationDlqDto;
import com.classification.domain_system.service.IntegrationDlqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/integration/dlq")
@RequiredArgsConstructor
public class IntegrationDlqController {

    private final IntegrationDlqService dlqService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'integration:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<IntegrationDlqDto.DlqItemResponse>> getDlqItems(
            @RequestParam(required = false) UUID channelId) {
        return ResponseEntity.ok(dlqService.getDlqItems(channelId));
    }

    @PostMapping("/retry")
    @PreAuthorize("hasPermission(null, 'integration:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<IntegrationDlqDto.DlqRetryResult> retryDlqItems(
            @RequestBody IntegrationDlqDto.DlqRetryRequest request) {
        return ResponseEntity.ok(dlqService.retryDlqItems(request));
    }
}
