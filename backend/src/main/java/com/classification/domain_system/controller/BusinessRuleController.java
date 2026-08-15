package com.classification.domain_system.controller;

import com.classification.domain_system.dto.BusinessRuleDto;
import com.classification.domain_system.service.BusinessRuleEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/business-rules")
@RequiredArgsConstructor
public class BusinessRuleController {

    private final BusinessRuleEngineService businessRuleEngineService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'domain:read') or hasPermission(null, 'record:read')")
    public ResponseEntity<List<BusinessRuleDto.BusinessRuleItem>> getRules(@PathVariable UUID domainId) {
        return ResponseEntity.ok(businessRuleEngineService.getRules(domainId));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'domain:write') or hasPermission(null, 'record:write')")
    public ResponseEntity<BusinessRuleDto.BusinessRuleItem> saveRule(
            @PathVariable UUID domainId,
            @RequestBody BusinessRuleDto.BusinessRuleItem rule) {
        return ResponseEntity.ok(businessRuleEngineService.saveRule(domainId, rule));
    }

    @PostMapping("/evaluate")
    @PreAuthorize("hasPermission(null, 'domain:read') or hasPermission(null, 'record:read')")
    public ResponseEntity<List<BusinessRuleDto.RuleEvaluationResult>> evaluateRules(@PathVariable UUID domainId) {
        return ResponseEntity.ok(businessRuleEngineService.evaluateRules(domainId));
    }
}
