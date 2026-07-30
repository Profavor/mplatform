package com.classification.domain_system.controller;

import com.classification.domain_system.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/trends")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getApprovalTrends() {
        return ResponseEntity.ok(dashboardService.getApprovalTrends());
    }

    @GetMapping("/domain-distribution")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, Object>>> getDomainDistribution() {
        return ResponseEntity.ok(dashboardService.getDomainDistribution());
    }
}
