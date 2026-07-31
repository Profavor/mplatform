package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.SurvivorshipRule;
import com.classification.domain_system.service.ApprovalService;
import com.classification.domain_system.service.RecordMergeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordMergeController {

    private final RecordMergeService recordMergeService;
    private final ApprovalService approvalService;

    @PostMapping("/merge")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<ApprovalRequest> mergeRecords(
            @RequestBody RecordMergeService.MergeRequest request,
            @AuthenticationPrincipal String username) {
        ApprovalRequest approval = approvalService.requestRecordMerge(request, username != null ? username : "SYSTEM");
        return ResponseEntity.ok(approval);
    }

    @PostMapping("/merge/auto")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<ApprovalRequest> autoMergeRecords(
            @RequestBody RecordMergeService.MergeRequest request,
            @AuthenticationPrincipal String username) {
        RecordMergeService.MergeRequest mergeReq = recordMergeService.buildSurvivorshipMergeRequest(request.survivorRecordId, request.mergedRecordIds);
        ApprovalRequest approval = approvalService.requestRecordMerge(mergeReq, username != null ? username : "SYSTEM");
        return ResponseEntity.ok(approval);
    }

    @GetMapping("/domains/{domainId}/survivorship-rules")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<List<SurvivorshipRule>> getSurvivorshipRules(@PathVariable UUID domainId) {
        return ResponseEntity.ok(recordMergeService.getSurvivorshipRules(domainId));
    }

    @PutMapping("/domains/{domainId}/survivorship-rules")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> updateSurvivorshipRules(@PathVariable UUID domainId, @RequestBody List<SurvivorshipRule> rules) {
        recordMergeService.updateSurvivorshipRules(domainId, rules);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unmerge")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<Record> unmergeRecord(
            @PathVariable UUID id,
            @AuthenticationPrincipal String username) {
        Record unmerged = recordMergeService.unmergeRecord(id, username != null ? username : "SYSTEM");
        return ResponseEntity.ok(unmerged);
    }
}
