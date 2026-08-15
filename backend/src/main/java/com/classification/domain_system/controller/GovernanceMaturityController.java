package com.classification.domain_system.controller;

import com.classification.domain_system.dto.GovernanceMaturityDto;
import com.classification.domain_system.service.GovernanceMaturityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/governance/maturity-evaluation")
@RequiredArgsConstructor
public class GovernanceMaturityController {

    private final GovernanceMaturityService governanceMaturityService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'compliance:read')")
    public ResponseEntity<GovernanceMaturityDto.GovernanceMaturityReport> evaluateMaturity() {
        return ResponseEntity.ok(governanceMaturityService.evaluateMaturity());
    }
}
