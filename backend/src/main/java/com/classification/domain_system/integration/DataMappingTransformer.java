package com.classification.domain_system.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.MethodExecutor;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.MapAccessor;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DataMappingTransformer {

    private final ObjectMapper mapper = new ObjectMapper()
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    // rawPayload 파싱용 - 외부 시스템에서 따옴표 없는 키 등 비표준 JSON을 허용
    private final ObjectMapper lenientMapper = new ObjectMapper()
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

    private final ExpressionParser parser = new SpelExpressionParser();

    public String transform(Message<?> message) {
        String payloadJson = (String) message.getPayload();
        String mappingConfigStr = message.getHeaders().get("MAPPING_CONFIG", String.class);
        return transformPayload(payloadJson, mappingConfigStr);
    }

    public String transformPayload(String payloadJson, String mappingConfigStr) {
        try {
            if (mappingConfigStr == null || mappingConfigStr.isBlank()) {
                log.warn("[Mapping] mappingConfigJson is empty - returning original payload");
                return payloadJson;
            }

            log.debug("[Mapping] mappingConfigJson: {}", mappingConfigStr);

            // Read source payload (lenient: 따옴표 없는 키, 단일 따옴표 허용)
            // 파싱 실패 시 → 400 Bad Request 유도를 위해 예외 그대로 throw
            Map<String, Object> payload;
            try {
                payload = lenientMapper.readValue(payloadJson, new TypeReference<>() {});
            } catch (Exception parseEx) {
                log.error("[Mapping] Payload JSON parsing failed: {}", parseEx.getMessage());
                throw new IllegalArgumentException("The received payload is not a valid JSON format: " + parseEx.getMessage(), parseEx);
            }
            
            // Setup SpEL context (Sandboxed)
            Map<String, Object> rootContext = new HashMap<>();
            rootContext.put("payload", payload);
            StandardEvaluationContext context = createSandboxedContext(rootContext);

            // Read mapping config
            JsonNode mappingConfig = mapper.readTree(mappingConfigStr);
            JsonNode mappings = mappingConfig.get("mappings");

            if (mappings == null || !mappings.isArray() || mappings.isEmpty()) {
                log.warn("[Mapping] mappings array is null or empty - returning original payload");
                return payloadJson;
            }

            JsonNode rootPathNode = mappingConfig.get("rootPath");
            String rootPath = (rootPathNode != null && !rootPathNode.asText().isBlank()) ? rootPathNode.asText() : null;
            log.debug("[Mapping] rootPath={}, mappings count={}", rootPath, mappings.size());

            if (rootPath != null) {
                try {
                    Expression rootExp = parser.parseExpression(rootPath);
                    Object rootObj = rootExp.getValue(context);
                    log.debug("[Mapping] rootPath evaluated to type={}, value={}", rootObj == null ? "null" : rootObj.getClass().getSimpleName(), rootObj);

                    if (rootObj instanceof Iterable) {
                        java.util.List<Map<String, Object>> resultList = new java.util.ArrayList<>();
                        for (Object item : (Iterable<?>) rootObj) {
                            StandardEvaluationContext itemContext = createSandboxedContext(item);
                            itemContext.setVariable("payload", payload);

                            Map<String, Object> targetPayload = new HashMap<>();
                            for (JsonNode mapping : mappings) {
                                String targetField = mapping.get("targetField").asText();
                                String sourceExpression = mapping.get("sourceExpression").asText();
                                try {
                                    Expression exp = parser.parseExpression(sourceExpression);
                                    Object value = exp.getValue(itemContext);
                                    log.debug("[Mapping] {} = {} → {}", targetField, sourceExpression, value);
                                    targetPayload.put(targetField, value);
                                } catch (Exception e) {
                                    log.error("[Mapping] Expression eval failed: {} → {}: {}", targetField, sourceExpression, e.getMessage());
                                    targetPayload.put(targetField, null);
                                }
                            }
                            resultList.add(targetPayload);
                        }
                        return mapper.writeValueAsString(resultList);
                    }

                    if (rootObj instanceof Map) {
                        StandardEvaluationContext itemContext = createSandboxedContext(rootObj);
                        itemContext.setVariable("payload", payload);

                        Map<String, Object> targetPayload = new HashMap<>();
                        for (JsonNode mapping : mappings) {
                            String targetField = mapping.get("targetField").asText();
                            String sourceExpression = mapping.get("sourceExpression").asText();
                            try {
                                Expression exp = parser.parseExpression(sourceExpression);
                                Object value = exp.getValue(itemContext);
                                log.debug("[Mapping] {} = {} → {}", targetField, sourceExpression, value);
                                targetPayload.put(targetField, value);
                            } catch (Exception e) {
                                log.error("[Mapping] Expression eval failed: {} → {}: {}", targetField, sourceExpression, e.getMessage());
                                targetPayload.put(targetField, null);
                            }
                        }
                        return mapper.writeValueAsString(targetPayload);
                    }

                    log.warn("[Mapping] rootPath result is neither Iterable nor Map: {}", rootObj);
                } catch (Exception e) {
                    log.error("[Mapping] Error evaluating rootPath '{}': {}", rootPath, e.getMessage());
                }
            }

            // Single object processing
            Map<String, Object> targetPayload = new HashMap<>();
            
            for (JsonNode mapping : mappings) {
                String targetField = mapping.get("targetField").asText();
                String sourceExpression = mapping.get("sourceExpression").asText();
                
                try {
                    Expression exp = parser.parseExpression(sourceExpression);
                    Object value = exp.getValue(context);
                    targetPayload.put(targetField, value);
                } catch (Exception e) {
                    System.err.println("Error evaluating expression '" + sourceExpression + "': " + e.getMessage());
                    targetPayload.put(targetField, null);
                }
            }

            return mapper.writeValueAsString(targetPayload);
            
        } catch (IllegalArgumentException e) {
            // payload 파싱 실패 등 명시적 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("[Mapping] Unexpected error during mapping processing: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred during mapping processing: " + e.getMessage(), e);
        }
    }

    public StandardEvaluationContext createSandboxedContext(Object rootObject) {
        StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
        // T(...) 차단: 임의 클래스 로딩 및 정적 메소드(Runtime, System 등) 호출 원천 차단
        context.setTypeLocator(typeName -> {
            throw new SpelEvaluationException(SpelMessage.TYPE_NOT_FOUND, typeName);
        });
        context.setPropertyAccessors(java.util.List.of(new MapAccessor(), new SafePropertyAccessor()));
        context.setMethodResolvers(java.util.List.of(new SafeMappingMethodResolver()));
        return context;
    }

    public static class SafePropertyAccessor extends ReflectivePropertyAccessor {
        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            if ("class".equalsIgnoreCase(name) || "declaringClass".equalsIgnoreCase(name)) {
                return false;
            }
            return super.canRead(context, target, name);
        }
    }

    public static class SafeMappingMethodResolver implements MethodResolver {
        private final ReflectiveMethodResolver delegate = new ReflectiveMethodResolver();

        private static final java.util.Set<String> BLOCKED_METHODS = java.util.Set.of(
                "getClass", "wait", "notify", "notifyAll", "clone", "finalize"
        );

        private static final java.util.Set<Class<?>> ALLOWED_CLASSES = java.util.Set.of(
                String.class, Number.class, Integer.class, Long.class, Double.class,
                Float.class, Boolean.class, Map.class, java.util.List.class, java.util.Collection.class,
                Object[].class, Math.class
        );

        @Override
        public MethodExecutor resolve(
                EvaluationContext context,
                Object targetObject,
                String name,
                java.util.List<TypeDescriptor> argumentTypes) throws AccessException {
            if (targetObject == null) {
                return null;
            }
            if (BLOCKED_METHODS.contains(name)) {
                throw new AccessException("Method execution blocked: " + name);
            }
            boolean allowed = ALLOWED_CLASSES.stream().anyMatch(c -> c.isAssignableFrom(targetObject.getClass()));
            if (!allowed) {
                throw new AccessException("Method execution not allowed on class: " + targetObject.getClass().getName());
            }
            return delegate.resolve(context, targetObject, name, argumentTypes);
        }
    }
}
