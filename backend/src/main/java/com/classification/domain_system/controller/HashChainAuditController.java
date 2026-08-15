package com.classification.domain_system.controller;

import com.classification.domain_system.dto.HashChainDto;
import com.classification.domain_system.service.HashChainAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/records/{recordId}/ledger")
@RequiredArgsConstructor
public class HashChainAuditController {

    private final HashChainAuditService hashChainAuditService;

    @GetMapping("/verify")
    @PreAuthorize("hasPermission(null, 'compliance:read') or hasPermission(null, 'record:read')")
    public ResponseEntity<HashChainDto.LedgerVerificationResponse> verifyLedger(
            @PathVariable UUID recordId) {
        return ResponseEntity.ok(hashChainAuditService.verifyRecordLedger(recordId));
    }
}
