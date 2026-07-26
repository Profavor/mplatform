package com.classification.domain_system.controller;

import com.classification.domain_system.entity.MatchingRule;
import com.classification.domain_system.repository.MatchingRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/domains/{domainId}/matching-rules")
@RequiredArgsConstructor
public class MatchingRuleController {

    private final MatchingRuleRepository matchingRuleRepository;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'domain:read')")
    public ResponseEntity<List<MatchingRule>> getRules(@PathVariable UUID domainId) {
        return ResponseEntity.ok(matchingRuleRepository.findByDomainId(domainId));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<MatchingRule> createRule(@PathVariable UUID domainId, @RequestBody MatchingRule rule) {
        rule.setDomainId(domainId);
        return ResponseEntity.ok(matchingRuleRepository.save(rule));
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<MatchingRule> updateRule(@PathVariable UUID domainId, @PathVariable UUID ruleId, @RequestBody MatchingRule rule) {
        MatchingRule existing = matchingRuleRepository.findById(ruleId).orElseThrow(() -> new RuntimeException("Rule not found"));
        existing.setRuleName(rule.getRuleName());
        existing.setTargetFieldKeys(rule.getTargetFieldKeys());
        existing.setMatchType(rule.getMatchType());
        existing.setActive(rule.isActive());
        return ResponseEntity.ok(matchingRuleRepository.save(existing));
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'domain:write')")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID domainId, @PathVariable UUID ruleId) {
        matchingRuleRepository.deleteById(ruleId);
        return ResponseEntity.ok().build();
    }
}
