package com.classification.domain_system.service.dq.evaluators;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.EvaluationContext;
import com.classification.domain_system.service.dq.RuleEvaluator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.*;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * CUSTOM_SPEL evaluator — executes SpEL expressions with sandboxed context.
 * Uses a strict MethodResolver whitelist to block reflection and arbitrary class access.
 * Allowed methods on String values: contains, startsWith, endsWith, equals, equalsIgnoreCase, length, matches, trim, toLowerCase, toUpperCase.
 */
@Component
@Slf4j
public class SpelEvaluator implements RuleEvaluator {

    @Getter
    @Setter
    @Value("${dq.engine.custom-spel.enabled}")
    private boolean enabled = true;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DqRuleType supports() {
        return DqRuleType.CUSTOM_SPEL;
    }

    @Override
    public Optional<String> evaluate(FieldDefinition field, DqRule rule, JsonNode value, EvaluationContext context) {
        if (!enabled) {
            return Optional.of("CUSTOM_SPEL rule evaluation is currently disabled.");
        }

        if (value == null || value.isNull()) {
            return Optional.empty();
        }

        try {
            JsonNode paramsNode = objectMapper.readTree(rule.getParams());
            String expressionStr = paramsNode.get("expression").asText();
            String valueStr = value.isTextual() ? value.asText() : value.toString();

            StandardEvaluationContext evalContext = new StandardEvaluationContext();

            // 1. Block T() type locator
            evalContext.setTypeLocator(typeName -> {
                throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
            });

            // 2. Strict safe method resolver whitelist (blocks getClass(), forName(), exec(), etc.)
            evalContext.setMethodResolvers(List.of(new SafeMethodResolver()));

            evalContext.setVariable("value", valueStr);
            evalContext.setVariable("fieldKey", field.getKey());

            if (context.getFullRecord() != null) {
                Map<String, Object> recordMap = objectMapper.convertValue(
                        context.getFullRecord(), new TypeReference<Map<String, Object>>() {});
                evalContext.setVariable("record", recordMap);
            }

            Expression exp = parser.parseExpression(expressionStr);
            Boolean result = exp.getValue(evalContext, Boolean.class);

            if (result == null || !result) {
                return Optional.of("Custom validation failed for expression: " + expressionStr);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("SpEL evaluation error for field {}: {}", field != null ? field.getKey() : "unknown", e.getMessage());
            return Optional.of("SpEL evaluation error: Evaluation failed or unsafe expression detected.");
        }
    }

    private static class SafeMethodResolver implements MethodResolver {
        private static final Set<String> ALLOWED_METHODS = Set.of(
                "contains", "startswith", "endswith", "equals", "equalsignorecase",
                "length", "matches", "trim", "tolowercase", "touppercase", "isempty", "isblank"
        );

        private final ReflectiveMethodResolver delegate = new ReflectiveMethodResolver();

        @Override
        public MethodExecutor resolve(org.springframework.expression.EvaluationContext context, Object targetObject, String name, List<TypeDescriptor> argumentTypes)
                throws AccessException {
            if (targetObject == null) {
                return null;
            }

            // Block reflection, Class object, System, Runtime, etc.
            if (targetObject instanceof Class<?> ||
                    targetObject.getClass().getName().startsWith("java.lang.reflect") ||
                    targetObject.getClass().getName().startsWith("java.lang.ClassLoader") ||
                    targetObject instanceof ProcessBuilder ||
                    targetObject instanceof Runtime) {
                throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, targetObject.getClass().getName());
            }

            // Only allow whitelisted methods on String targets
            if (targetObject instanceof String && ALLOWED_METHODS.contains(name.toLowerCase())) {
                return delegate.resolve(context, targetObject, name, argumentTypes);
            }

            throw new SpelEvaluationException(SpelMessage.METHOD_NOT_FOUND, name, targetObject.getClass().getName());
        }
    }
}

