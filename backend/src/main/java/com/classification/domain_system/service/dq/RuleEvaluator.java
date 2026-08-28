package com.classification.domain_system.service.dq;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Strategy interface for DQ rule evaluation.
 * Each implementation handles a specific DqRuleType.
 */
public interface RuleEvaluator {
    /** Which rule type this evaluator handles */
    DqRuleType supports();

    /**
     * Evaluate a single rule against a field value.
     * @return Optional.empty() if valid, Optional.of(errorMessage) if violated
     */
    Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context);

    /**
     * Checks if the value is masked (e.g. contains '*') or encrypted (e.g. 'vault:', 'ENC(')
     * to prevent false positive DQ violations.
     */
    default boolean isMaskedOrEncrypted(JsonNode value) {
        if (value == null || value.isNull() || !value.isTextual()) {
            return false;
        }
        String text = value.asText().trim();
        return text.contains("*") || text.startsWith("vault:") || text.startsWith("ENC(");
    }
}
