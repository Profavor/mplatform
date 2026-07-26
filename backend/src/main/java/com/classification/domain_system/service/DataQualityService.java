package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

/**
 * DataQualityService — backward-compatible wrapper around DqRuleEngine.
 * 
 * Delegates to DqRuleEngine for DQ rule-based evaluation, and also performs
 * legacy checks (Required + Type) that are not yet migrated to DQ rules.
 * Once all fields have DQ rules configured, the legacy checks can be removed.
 */
@Service
public class DataQualityService {

    private final FieldDefinitionService fieldDefinitionService;
    private final ClassificationNodeRepository nodeRepository;
    private final DqRuleEngine dqRuleEngine;
    private final ObjectMapper objectMapper;

    public DataQualityService(FieldDefinitionService fieldDefinitionService,
                              ClassificationNodeRepository nodeRepository,
                              DqRuleEngine dqRuleEngine) {
        this.fieldDefinitionService = fieldDefinitionService;
        this.nodeRepository = nodeRepository;
        this.dqRuleEngine = dqRuleEngine;
        this.objectMapper = new ObjectMapper();
    }

    public static class DQResult {
        public boolean isValid;
        public List<String> errors = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
    }

    public DQResult validateData(UUID nodeId, String jsonString) {
        return validateData(nodeId, jsonString, null, null);
    }

    public DQResult validateData(UUID nodeId, String jsonString, UUID recordId) {
        return validateData(nodeId, jsonString, recordId, null);
    }

    public DQResult validateData(UUID nodeId, String jsonString, UUID recordId, List<String> targetFieldKeys) {
        DQResult result = new DQResult();

        // 1. Run new DQ Rule Engine
        DqEvaluationResult engineResult = dqRuleEngine.evaluate(nodeId, jsonString, recordId);

        // Add engine errors (filtered by targetFieldKeys if provided)
        if (targetFieldKeys != null && !targetFieldKeys.isEmpty()) {
            engineResult.getViolations().stream()
                    .filter(v -> targetFieldKeys.contains(v.getFieldKey()))
                    .forEach(v -> {
                        String msg = v.getMessage() != null && !v.getMessage().isEmpty()
                                ? v.getMessage().getOrDefault("en", v.getMessage().values().iterator().next())
                                : "Validation failed";
                        if ("ERROR".equalsIgnoreCase(v.getSeverity())) {
                            result.errors.add(msg);
                        } else if ("WARNING".equalsIgnoreCase(v.getSeverity())) {
                            result.warnings.add(msg);
                        }
                    });
        } else {
            result.errors.addAll(engineResult.getErrorMessages());
            result.warnings.addAll(engineResult.getWarningMessages());
        }

        // 2. Legacy checks for fields that don't yet have DQ rules
        runLegacyChecks(nodeId, jsonString, engineResult, result, targetFieldKeys);

        result.isValid = result.errors.isEmpty();
        return result;
    }

    /**
     * Legacy Required + Type checks.
     * These are only applied for fields that have NO DQ rules configured,
     * ensuring backward compatibility during the migration period.
     */
    private void runLegacyChecks(UUID nodeId, String jsonString,
                                 DqEvaluationResult engineResult, DQResult result,
                                 List<String> targetFieldKeys) {
        List<FieldDefinition> effectiveFields = fieldDefinitionService.getEffectiveFields(nodeId);

        if (targetFieldKeys != null && !targetFieldKeys.isEmpty()) {
            effectiveFields = effectiveFields.stream()
                    .filter(f -> targetFieldKeys.contains(f.getKey()))
                    .toList();
        }

        ClassificationNode node = nodeRepository.findById(nodeId).orElse(null);
        Domain dom = node != null ? node.getDomain() : null;

        JsonNode dataNode;
        try {
            dataNode = objectMapper.readTree(jsonString);
        } catch (Exception e) {
            // Already handled by DqRuleEngine
            if (result.errors.isEmpty()) {
                result.errors.add("Invalid JSON format");
            }
            return;
        }

        // Collect field keys that already have engine violations to avoid duplicates
        java.util.Set<String> engineCheckedFields = engineResult.getViolations().stream()
                .map(DqEvaluationResult.Violation::getFieldKey)
                .collect(java.util.stream.Collectors.toSet());

        for (FieldDefinition field : effectiveFields) {
            // Skip fields that already have DQ rule violations
            if (engineCheckedFields.contains(field.getKey())) {
                continue;
            }

            JsonNode valueNode = dataNode.get(field.getKey());

            // 1. Required Check
            if (field.getRequired() != null && field.getRequired()) {
                boolean isAutoNumbering = false;
                if (dom != null && dom.getIdentifierFieldId() != null) {
                    String domIdStr = dom.getIdentifierFieldId().toString();
                    String fieldIdStr = field.getId() != null ? field.getId().toString() : "";
                    boolean matchesId = domIdStr.equalsIgnoreCase(fieldIdStr);

                    String targetKey = effectiveFields.stream()
                        .filter(f -> f.getId() != null && domIdStr.equalsIgnoreCase(f.getId().toString()))
                        .map(FieldDefinition::getKey)
                        .findFirst()
                        .orElse(null);
                    boolean matchesKey = field.getKey() != null && field.getKey().equalsIgnoreCase(targetKey);

                    if (matchesId || matchesKey) {
                        if (dom.getNumberingPattern() != null && !dom.getNumberingPattern().isBlank()) {
                            isAutoNumbering = true;
                        }
                    }
                }

                if (!isAutoNumbering) {
                    if (valueNode == null || valueNode.isNull() || (valueNode.isTextual() && valueNode.asText().trim().isEmpty())) {
                        result.errors.add("Field '" + getFieldName(field) + "' is required.");
                        continue;
                    }
                }
            }

            // 2. Type Check (if present)
            if (valueNode != null && !valueNode.isNull()) {
                String type = field.getType();
                if ("NUMBER".equalsIgnoreCase(type)) {
                    if (!valueNode.isNumber() && !isNumeric(valueNode.asText())) {
                        result.errors.add("Field '" + getFieldName(field) + "' must be a number.");
                    }
                } else if ("BOOLEAN".equalsIgnoreCase(type) || "CHECKBOX".equalsIgnoreCase(type)) {
                    if (!valueNode.isBoolean() && !valueNode.asText().equalsIgnoreCase("true") && !valueNode.asText().equalsIgnoreCase("false")) {
                        result.errors.add("Field '" + getFieldName(field) + "' must be a boolean.");
                    }
                }
            }
        }
    }

    private String getFieldName(FieldDefinition field) {
        if (field.getName() != null) {
            return field.getName().getOrDefault("en", field.getName().getOrDefault("ko", field.getKey()));
        }
        return field.getKey();
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
