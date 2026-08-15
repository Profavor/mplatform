package com.classification.domain_system.controller;

import com.classification.domain_system.dto.BusinessTermDto;
import com.classification.domain_system.service.BusinessTermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-terms")
@RequiredArgsConstructor
public class BusinessTermController {

    private final BusinessTermService businessTermService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'schema:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<BusinessTermDto.BusinessTermResponse>> getAllTerms(
            @RequestParam(required = false) UUID domainId) {
        return ResponseEntity.ok(businessTermService.getAllTerms(domainId));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'schema:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<BusinessTermDto.BusinessTermResponse> createTerm(
            @RequestBody BusinessTermDto.BusinessTermCreateRequest request) {
        return ResponseEntity.ok(businessTermService.createTerm(request));
    }

    @GetMapping("/recommend")
    @PreAuthorize("hasPermission(null, 'schema:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<BusinessTermDto.TermRecommendation>> recommendTerms(
            @RequestParam String keyword) {
        return ResponseEntity.ok(businessTermService.recommendTerms(keyword));
    }
}
