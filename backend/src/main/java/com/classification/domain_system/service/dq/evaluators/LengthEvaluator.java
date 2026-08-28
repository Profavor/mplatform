package com.classification.domain_system.service.dq.evaluators;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.EvaluationContext;
import com.classification.domain_system.service.dq.RuleEvaluator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LengthEvaluator implements RuleEvaluator {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DqRuleType supports() {
        return DqRuleType.LENGTH;
    }

    @Override
    public Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context) {
        if (value == null || value.isNull() || (value.isTextual() && value.asText().trim().isEmpty())) {
            return Optional.empty();
        }

        if (isMaskedOrEncrypted(value)) {
            return Optional.empty();
        }

        String textValue = value.asText();
        int length = textValue.length();

        try {
            JsonNode paramsNode = objectMapper.readTree(rule.getParams());
            JsonNode minLengthNode = paramsNode.get("minLength");
            JsonNode maxLengthNode = paramsNode.get("maxLength");

            int minLength = minLengthNode != null && !minLengthNode.isNull() ? minLengthNode.asInt() : 0;
            int maxLength = maxLengthNode != null && !maxLengthNode.isNull() ? maxLengthNode.asInt() : Integer.MAX_VALUE;

            if (length < minLength) {
                return Optional.of("Length must be at least " + minLength + " characters (current: " + length + ")");
            }
            if (length > maxLength) {
                return Optional.of("Length must be at most " + maxLength + " characters (current: " + length + ")");
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of("Length evaluation error: " + e.getMessage());
        }
    }
}
