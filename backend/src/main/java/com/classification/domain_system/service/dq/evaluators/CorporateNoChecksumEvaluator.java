package com.classification.domain_system.service.dq.evaluators;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.EvaluationContext;
import com.classification.domain_system.service.dq.RuleEvaluator;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CorporateNoChecksumEvaluator implements RuleEvaluator {

    private static final int[] WEIGHTS = {1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2};

    @Override
    public DqRuleType supports() {
        return DqRuleType.CORPORATE_NO_CHECKSUM;
    }

    @Override
    public Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context) {
        if (value == null || value.isNull() || (value.isTextual() && value.asText().trim().isEmpty())) {
            return Optional.empty();
        }

        String raw = value.asText().replaceAll("[^0-9]", "");
        if (raw.length() != 13) {
            return Optional.of("법인등록번호는 하이픈 제외 13자리 숫자여야 합니다. (입력값: " + value.asText() + ")");
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(raw.charAt(i)) * WEIGHTS[i];
        }

        int checkDigit = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(raw.charAt(12));

        if (checkDigit != lastDigit) {
            return Optional.of("유효하지 않은 법인등록번호 체크섬입니다. (입력값: " + value.asText() + ")");
        }

        return Optional.empty();
    }
}
