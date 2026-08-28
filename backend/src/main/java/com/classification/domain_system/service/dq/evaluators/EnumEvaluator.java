package com.classification.domain_system.service.dq.evaluators;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.EvaluationContext;
import com.classification.domain_system.service.dq.RuleEvaluator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class EnumEvaluator implements RuleEvaluator {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DqRuleType supports() {
        return DqRuleType.ENUM;
    }

    @Override
    public Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context) {
        if (value == null || value.isNull() || (value.isTextual() && value.asText().trim().isEmpty())) {
            return Optional.empty();
        }

        if (isMaskedOrEncrypted(value)) {
            return Optional.empty();
        }

        try {
            JsonNode paramsNode = objectMapper.readTree(rule.getParams());
            JsonNode valuesNode = paramsNode.get("values");

            if (valuesNode == null || !valuesNode.isArray()) {
                return Optional.of("ENUM rule misconfigured: 'values' array is missing");
            }

            List<String> allowedValues = new ArrayList<>();
            for (JsonNode v : valuesNode) {
                allowedValues.add(v.asText());
            }

            String textValue = value.asText();
            if (!allowedValues.contains(textValue)) {
                return Optional.of("Value must be one of: " + allowedValues);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of("Enum evaluation error: " + e.getMessage());
        }
    }
}
