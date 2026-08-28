package com.classification.domain_system.service.dq.evaluators;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.EvaluationContext;
import com.classification.domain_system.service.dq.RuleEvaluator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Component
@Slf4j
public class RegexEvaluator implements RuleEvaluator {

    private static final int MAX_PATTERN_LENGTH = 250;
    private static final long TIMEOUT_MS = 500;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DqRuleType supports() {
        return DqRuleType.REGEX;
    }

    @Override
    public Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context) {
        // Skip null/empty values — NOT_NULL handles that
        if (value == null || value.isNull() || (value.isTextual() && value.asText().trim().isEmpty())) {
            return Optional.empty();
        }

        // Skip masked or encrypted values to prevent false positive DQ errors
        if (isMaskedOrEncrypted(value)) {
            return Optional.empty();
        }

        try {
            JsonNode paramsNode = objectMapper.readTree(rule.getParams());
            JsonNode patternNode = paramsNode.has("pattern") ? paramsNode.get("pattern") : paramsNode.get("regex");
            if (patternNode == null || patternNode.isNull() || patternNode.asText().isBlank()) {
                log.warn("Regex pattern missing in DQ rule params for field {}", field.getKey());
                return Optional.empty();
            }
            String patternStr = patternNode.asText();
            String textValue = value.asText();

            if (patternStr.length() > MAX_PATTERN_LENGTH) {
                log.warn("Regex pattern for field {} exceeds maximum length limit: {} chars", field.getKey(), patternStr.length());
                return Optional.of("Regex pattern exceeds maximum length limit (" + MAX_PATTERN_LENGTH + " chars).");
            }

            Pattern pattern = Pattern.compile(patternStr);

            Future<Boolean> future = executor.submit(() -> pattern.matcher(textValue).matches());
            Boolean matches;
            try {
                matches = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                log.warn("Regex evaluation timed out for field {} with pattern: {}", field.getKey(), patternStr);
                return Optional.of("Regex evaluation timed out (ReDoS protection).");
            }

            if (Boolean.FALSE.equals(matches)) {
                return Optional.of("Value does not match pattern: " + patternStr);
            }
            return Optional.empty();
        } catch (PatternSyntaxException e) {
            log.warn("Invalid regex pattern syntax for field {}: {}", field.getKey(), e.getMessage());
            return Optional.of("Invalid regex pattern format.");
        } catch (Exception e) {
            log.warn("Regex evaluation error for field {}: {}", field.getKey(), e.getMessage());
            return Optional.of("Regex evaluation error occurred.");
        }
    }
}

