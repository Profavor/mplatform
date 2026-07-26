package com.classification.domain_system.controller;

import com.classification.domain_system.dto.DqEvaluationResponse;
import com.classification.domain_system.dto.DqRuleRequest;
import com.classification.domain_system.dto.DqRuleResponse;
import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.DqSeverity;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    private final DqRuleRepository dqRuleRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final DqRuleEngine dqRuleEngine;

    private final com.classification.domain_system.security.CustomPermissionEvaluator permissionEvaluator;

    // ─── CRUD for DQ Rules ───────────────────────────────────────────

    @GetMapping("/fields/{fieldId}/dq-rules")
    @PreAuthorize("hasPermission(null, 'dq:read')")
    public ResponseEntity<List<DqRuleResponse>> getRulesByField(@PathVariable UUID fieldId) {
        List<DqRule> rules = dqRuleRepository.findByFieldDefinition_IdOrderBySortOrderAsc(fieldId);
        return ResponseEntity.ok(rules.stream().map(this::toResponse).toList());
    }

    @PostMapping("/fields/{fieldId}/dq-rules")
    @PreAuthorize("hasPermission(null, 'dq:write')")
    @Transactional
    public ResponseEntity<DqRuleResponse> createRule(@PathVariable UUID fieldId,
                                                      @RequestBody DqRuleRequest request) {
        checkAdminAccess();
        FieldDefinition field = fieldDefinitionRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found: " + fieldId));

        DqRule rule = new DqRule();
        rule.setFieldDefinition(field);
        
        UUID domainId = request.getDomainId();
        if (domainId == null && field.getDomain() != null) {
            domainId = field.getDomain().getId();
        }
        if (domainId == null && field.getDefinedAtNode() != null && field.getDefinedAtNode().getDomain() != null) {
            domainId = field.getDefinedAtNode().getDomain().getId();
        }
        if (domainId == null && field.getFieldGroup() != null) {
            if (field.getFieldGroup().getDomain() != null) {
                domainId = field.getFieldGroup().getDomain().getId();
            } else if (field.getFieldGroup().getSector() != null && field.getFieldGroup().getSector().getDomain() != null) {
                domainId = field.getFieldGroup().getSector().getDomain().getId();
            }
        }
        rule.setDomainId(domainId);

        UUID nodeId = request.getNodeId();
        if (nodeId == null && field.getDefinedAtNode() != null) {
            nodeId = field.getDefinedAtNode().getId();
        }
        rule.setNodeId(nodeId);

        rule.setRuleType(DqRuleType.valueOf(request.getRuleType()));
        rule.setSeverity(request.getSeverity() != null
                ? DqSeverity.valueOf(request.getSeverity()) : DqSeverity.ERROR);
        rule.setParams(request.getParams());
        rule.setMessage(request.getMessage());
        rule.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        rule.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        DqRule saved = dqRuleRepository.save(rule);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/dq-rules/{ruleId}")
    @PreAuthorize("hasPermission(null, 'dq:write')")
    @Transactional
    public ResponseEntity<DqRuleResponse> updateRule(@PathVariable UUID ruleId,
                                                      @RequestBody DqRuleRequest request) {
        checkAdminAccess();
        DqRule rule = dqRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("DQ Rule not found: " + ruleId));

        if (request.getRuleType() != null) {
            rule.setRuleType(DqRuleType.valueOf(request.getRuleType()));
        }
        if (request.getSeverity() != null) {
            rule.setSeverity(DqSeverity.valueOf(request.getSeverity()));
        }
        if (request.getParams() != null) {
            rule.setParams(request.getParams());
        }
        if (request.getMessage() != null) {
            rule.setMessage(request.getMessage());
        }
        if (request.getSortOrder() != null) {
            rule.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }

        DqRule saved = dqRuleRepository.save(rule);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/dq-rules/{ruleId}")
    @PreAuthorize("hasPermission(null, 'dq:write')")
    @Transactional
    public ResponseEntity<Void> deleteRule(@PathVariable UUID ruleId) {
        checkAdminAccess();
        if (!dqRuleRepository.existsById(ruleId)) {
            throw new RuntimeException("DQ Rule not found: " + ruleId);
        }
        dqRuleRepository.deleteById(ruleId);
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

    private DqRuleResponse toResponse(DqRule rule) {
        DqRuleResponse resp = new DqRuleResponse();
        resp.setId(rule.getId());
        resp.setFieldDefinitionId(rule.getFieldDefinitionId());
        resp.setDomainId(rule.getDomainId());
        resp.setNodeId(rule.getNodeId());
        resp.setRuleType(rule.getRuleType().name());
        resp.setSeverity(rule.getSeverity().name());
        resp.setParams(rule.getParams());
        resp.setMessage(rule.getMessage());
        resp.setSortOrder(rule.getSortOrder());
        resp.setIsActive(rule.getIsActive());
        resp.setCreatedAt(rule.getCreatedAt());
        resp.setUpdatedAt(rule.getUpdatedAt());

        FieldDefinition field = rule.getFieldDefinition();
        if (field != null) {
            resp.setFieldKey(field.getKey());
            resp.setFieldName(field.getName());
        }
        return resp;
    }

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
