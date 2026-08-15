package com.classification.domain_system.controller;

import com.classification.domain_system.dto.RejectionAnalyticsDto;
import com.classification.domain_system.service.RejectionAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals/analytics/rejections")
@RequiredArgsConstructor
public class RejectionAnalyticsController {

    private final RejectionAnalyticsService rejectionAnalyticsService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'approval:read') or hasPermission(null, 'compliance:read')")
    public ResponseEntity<RejectionAnalyticsDto.RejectionAnalysisResponse> getRejectionAnalysis() {
        return ResponseEntity.ok(rejectionAnalyticsService.analyzeRejections());
    }
}
