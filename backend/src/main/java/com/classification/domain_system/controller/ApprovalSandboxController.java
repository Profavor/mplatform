package com.classification.domain_system.controller;

import com.classification.domain_system.dto.ApprovalSandboxDto;
import com.classification.domain_system.service.ApprovalSandboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/approvals/{requestId}/sandbox-preview")
@RequiredArgsConstructor
public class ApprovalSandboxController {

    private final ApprovalSandboxService approvalSandboxService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'workflow:approve') or hasPermission(null, 'workflow:read')")
    public ResponseEntity<ApprovalSandboxDto.SandboxPreviewResponse> getSandboxPreview(
            @PathVariable UUID requestId) {
        return ResponseEntity.ok(approvalSandboxService.generateSandboxPreview(requestId));
    }
}
