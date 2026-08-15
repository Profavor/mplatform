package com.classification.domain_system.controller;

import com.classification.domain_system.service.ApprovalEscalationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/approvals/escalate")
@RequiredArgsConstructor
public class ApprovalEscalationController {

    private final ApprovalEscalationService escalationService;

    @PostMapping("/scan")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'approval:write')")
    public ResponseEntity<Map<String, Object>> scanAndEscalate() {
        int escalatedCount = escalationService.scanAndEscalate();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "escalatedCount", escalatedCount,
                "message", "SLA escalation scan completed."
        ));
    }
}
