package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DqEvaluationResponse;
import com.classification.domain_system.dto.DqRuleRequest;
import com.classification.domain_system.dto.DqRuleResponse;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqRuleEngine;
import com.classification.domain_system.service.dq.DqRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DqRuleController {

    private final com.classification.domain_system.service.dq.DqRuleService dqRuleService;
    private final DqRuleEngine dqRuleEngine;
    private final com.classification.domain_system.security.CustomPermissionEvaluator permissionEvaluator;

    // ─── CRUD for DQ Rules ───────────────────────────────────────────

    @GetMapping("/fields/{fieldId}/dq-rules")
    @PreAuthorize("hasPermission(null, 'dq:read')")
    public ResponseEntity<List<DqRuleResponse>> getRulesByField(@PathVariable UUID fieldId) {
        return ResponseEntity.ok(dqRuleService.getRulesByField(fieldId));
    }

    @PostMapping("/fields/{fieldId}/dq-rules")
    @PreAuthorize("hasPermission(null, 'dq:write')")
    public ResponseEntity<DqRuleResponse> createRule(@PathVariable UUID fieldId,
                                                      @RequestBody DqRuleRequest request) {
        checkAdminAccess();
        return ResponseEntity.ok(dqRuleService.createRule(fieldId, request));
    }

    @PutMapping("/dq-rules/{ruleId}")
    @PreAuthorize("hasPermission(null, 'dq:write')")
    public ResponseEntity<DqRuleResponse> updateRule(@PathVariable UUID ruleId,
                                                      @RequestBody DqRuleRequest request) {
        checkAdminAccess();
        return ResponseEntity.ok(dqRuleService.updateRule(ruleId, request));
    }

    @DeleteMapping("/dq-rules/{ruleId}")
    @PreAuthorize("hasPermission(null, 'dq:write')")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId) {
        checkAdminAccess();
        dqRuleService.deleteRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    private void checkAdminAccess() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (!permissionEvaluator.hasPermission(auth, null, "dq:write")) {
            throw new com.classification.domain_system.exception.CustomAccessDeniedException("Access denied. DQ write permission required.");
        }
    }

    // ─── Validation Preview ──────────────────────────────────────────

    @PostMapping("/dq-rules/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DqEvaluationResponse> validatePreview(
            @RequestParam UUID nodeId,
            @RequestParam(required = false) UUID recordId,
            @RequestBody Map<String, Object> body) {
        String data;
        Object rawData = body != null ? body.get("data") : null;
        if (rawData instanceof String strData) {
            data = strData;
        } else if (rawData != null) {
            try {
                data = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(rawData);
            } catch (Exception e) {
                data = "{}";
            }
        } else {
            data = "{}";
        }
        DqEvaluationResult engineResult = dqRuleEngine.evaluate(nodeId, data, recordId);
        return ResponseEntity.ok(toEvaluationResponse(engineResult));
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private DqEvaluationResponse toEvaluationResponse(DqEvaluationResult result) {
        DqEvaluationResponse resp = new DqEvaluationResponse();
        resp.setValid(result.isValid());

        resp.setErrors(result.getViolations().stream()
                .filter(v -> "ERROR".equals(v.getSeverity()))
                .map(this::toViolationItem)
                .collect(Collectors.toList()));

        resp.setWarnings(result.getViolations().stream()
                .filter(v -> "WARNING".equals(v.getSeverity()))
                .map(this::toViolationItem)
                .collect(Collectors.toList()));

        return resp;
    }

    private DqEvaluationResponse.ViolationItem toViolationItem(DqEvaluationResult.Violation v) {
        DqEvaluationResponse.ViolationItem item = new DqEvaluationResponse.ViolationItem();
        item.setFieldKey(v.getFieldKey());
        item.setRuleType(v.getRuleType());
        item.setSeverity(v.getSeverity());
        item.setMessage(v.getMessage());
        item.setActualValue(v.getActualValue());
        return item;
    }
}
