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
public class BusinessNoChecksumEvaluator implements RuleEvaluator {

    private static final int[] WEIGHTS = {1, 3, 7, 1, 3, 7, 1, 3, 5};

    @Override
    public DqRuleType supports() {
        return DqRuleType.BUSINESS_NO_CHECKSUM;
    }

    @Override
    public Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context) {
        if (value == null || value.isNull() || (value.isTextual() && value.asText().trim().isEmpty())) {
            return Optional.empty();
        }

        String raw = value.asText().replaceAll("[^0-9]", "");
        if (raw.length() != 10) {
            return Optional.of("사업자등록번호는 하이픈 제외 10자리 숫자여야 합니다. (입력값: " + value.asText() + ")");
        }

        int sum = 0;
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(raw.charAt(i)) * WEIGHTS[i];
        }

        int d8 = Character.getNumericValue(raw.charAt(8));
        sum += (d8 * 5) / 10;
        sum += (d8 * 5) % 10;

        int checkDigit = (10 - (sum % 10)) % 10;
        int lastDigit = Character.getNumericValue(raw.charAt(9));

        if (checkDigit != lastDigit) {
            return Optional.of("유효하지 않은 사업자등록번호 체크섬입니다. (입력값: " + value.asText() + ")");
        }

        return Optional.empty();
    }
}
