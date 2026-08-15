package com.classification.domain_system.controller;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.dto.ApprovalDelegationDto;
import com.classification.domain_system.service.ApprovalDelegationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals/delegations")
@RequiredArgsConstructor
public class ApprovalDelegationController {

    private final ApprovalDelegationService delegationService;
    private final AuthContext authContext;

    @GetMapping("/my")
    public ResponseEntity<Map<String, List<ApprovalDelegationDto>>> getMyDelegations() {
        String currentUserId = authContext != null && authContext.getUserId() != null ? authContext.getUserId() : "admin";
        Map<String, List<ApprovalDelegationDto>> result = delegationService.getMyDelegations(currentUserId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ApprovalDelegationDto> createDelegation(@RequestBody ApprovalDelegationDto dto) {
        String currentUserId = authContext != null && authContext.getUserId() != null ? authContext.getUserId() : "admin";
        ApprovalDelegationDto created = delegationService.createDelegation(dto, currentUserId);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> revokeDelegation(@PathVariable UUID id) {
        String currentUserId = authContext != null && authContext.getUserId() != null ? authContext.getUserId() : "admin";
        delegationService.revokeDelegation(id, currentUserId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Delegation revoked successfully."));
    }
}
