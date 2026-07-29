package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.MatchCandidate;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.MatchCandidateService;
import com.classification.domain_system.service.RecordMergeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MatchCandidateController {

    private final MatchCandidateService candidateService;

    @GetMapping("/api/domains/{domainId}/match-candidates")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<PageResponse<MatchCandidate>> getCandidates(
            @PathVariable UUID domainId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(candidateService.getCandidatesByDomain(domainId, status, page, size));
    }

    @PostMapping("/api/match-candidates/{id}/confirm")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<MatchCandidate> confirm(
            @PathVariable UUID id,
            @RequestBody(required = false) RecordMergeService.MergeRequest mergeReq,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(candidateService.confirmCandidate(id, mergeReq, username));
    }

    @PostMapping("/api/match-candidates/{id}/reject")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<Record> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(candidateService.rejectCandidate(id, username));
    }

    @PostMapping("/api/match-candidates/{id}/ignore")
    @PreAuthorize("hasPermission(null, 'domain:write')")
    public ResponseEntity<MatchCandidate> ignore(
            @PathVariable UUID id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(candidateService.ignoreCandidate(id, username));
    }
}
