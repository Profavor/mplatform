package com.classification.domain_system.service.dq;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.service.DataMaskingService;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.FieldEncryptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DQ Rule Engine — orchestrates rule evaluation for record data.
 * Collects all RuleEvaluator implementations via Spring DI and dispatches
 * evaluation to the matching evaluator for each rule type.
 */
@Service
@Slf4j
public class DqRuleEngine {
    private final Map<DqRuleType, RuleEvaluator> evaluatorMap;
    private final FieldDefinitionService fieldDefinitionService;
    private final DqRuleRepository dqRuleRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final DqViolationRepository violationRepository;
    private final RecordRepository recordRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final FieldEncryptionService fieldEncryptionService;
    private final DataMaskingService dataMaskingService;
    private final ObjectMapper objectMapper;

    public DqRuleEngine(List<RuleEvaluator> evaluators,
                        FieldDefinitionService fieldDefinitionService,
                        DqRuleRepository dqRuleRepository,
                        ClassificationNodeRepository nodeRepository,
                        DqViolationRepository violationRepository,
                        RecordRepository recordRepository,
                        FieldDefinitionRepository fieldDefinitionRepository) {
        this(evaluators, fieldDefinitionService, dqRuleRepository, nodeRepository,
                violationRepository, recordRepository, fieldDefinitionRepository, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public DqRuleEngine(List<RuleEvaluator> evaluators,
                        FieldDefinitionService fieldDefinitionService,
                        DqRuleRepository dqRuleRepository,
                        ClassificationNodeRepository nodeRepository,
                        DqViolationRepository violationRepository,
                        RecordRepository recordRepository,
                        FieldDefinitionRepository fieldDefinitionRepository,
                        @org.springframework.beans.factory.annotation.Autowired(required = false) FieldEncryptionService fieldEncryptionService,
                        @org.springframework.beans.factory.annotation.Autowired(required = false) DataMaskingService dataMaskingService) {
        this.evaluatorMap = evaluators.stream()
                .collect(Collectors.toMap(RuleEvaluator::supports, e -> e));
        this.fieldDefinitionService = fieldDefinitionService;
        this.dqRuleRepository = dqRuleRepository;
        this.nodeRepository = nodeRepository;
        this.violationRepository = violationRepository;
        this.recordRepository = recordRepository;
        this.fieldDefinitionRepository = fieldDefinitionRepository;
        this.fieldEncryptionService = fieldEncryptionService;
        this.dataMaskingService = dataMaskingService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Evaluate all active DQ rules for a given node and JSON record data.
     * Returns a result containing all violations (both ERROR and WARNING).
     */
    public DqEvaluationResult evaluate(UUID nodeId, String jsonString) {
        return evaluate(nodeId, jsonString, null, null, null);
    }

    public DqEvaluationResult evaluate(UUID nodeId, String jsonString, UUID recordId) {
        return evaluate(nodeId, jsonString, recordId, null, null);
    }

    public DqEvaluationResult evaluate(UUID nodeId, String jsonString, UUID recordId, Map<UUID, List<FieldDefinition>> nodeFieldsCache) {
        return evaluate(nodeId, jsonString, recordId, nodeFieldsCache, null);
    }

    /**
     * Evaluate DQ rules for a given node and JSON record data.
     * @param targetFieldKeys if non-null and non-empty, only fields whose key is in this list will be evaluated.
     *                        If null or empty, all fields are evaluated.
     */
    public DqEvaluationResult evaluate(UUID nodeId, String jsonString, UUID recordId,
                                       Map<UUID, List<FieldDefinition>> nodeFieldsCache,
                                       List<String> targetFieldKeys) {
        DqEvaluationResult result = new DqEvaluationResult();

        JsonNode dataNode;
        try {
            dataNode = objectMapper.readTree(jsonString);
        } catch (Exception e) {
            result.addViolation("_json", "PARSE", "ERROR",
                    Map.of("en", "Invalid JSON format", "ko", "잘못된 JSON 형식입니다"),
                    jsonString != null ? jsonString.substring(0, Math.min(100, jsonString.length())) : "null");
            return result;
        }

        ClassificationNode node = nodeRepository.findById(nodeId).orElse(null);
        UUID domainId = node != null && node.getDomain() != null ? node.getDomain().getId() : null;

        List<FieldDefinition> effectiveFields;
        if (nodeFieldsCache != null && nodeFieldsCache.containsKey(nodeId)) {
            effectiveFields = nodeFieldsCache.get(nodeId);
        } else {
            effectiveFields = fieldDefinitionService.getEffectiveFields(nodeId);
            if (nodeFieldsCache != null && effectiveFields != null) {
                nodeFieldsCache.put(nodeId, effectiveFields);
            }
        }
        if (effectiveFields == null) {
            effectiveFields = Collections.emptyList();
        }

        List<UUID> fieldIds = effectiveFields.stream()
                .map(FieldDefinition::getId)
                .filter(Objects::nonNull)
                .toList();



        // Batch load all active rules for these fields in one query
        List<DqRule> allRules = fieldIds.isEmpty()
                ? Collections.emptyList()
                : dqRuleRepository.findByFieldDefinition_IdInAndIsActiveTrueOrderBySortOrderAsc(fieldIds);

        // Group rules by fieldDefinitionId for efficient lookup
        Map<UUID, List<DqRule>> rulesByField = allRules.stream()
                .collect(Collectors.groupingBy(r -> r.getFieldDefinition().getId()));

        // Decrypt encrypted fields into plaintext JsonNode for accurate DQ evaluation
        JsonNode plainDataNode = decryptDataNodeForDq(dataNode, effectiveFields);

        EvaluationContext context = new EvaluationContext(domainId, nodeId, plainDataNode, recordId);
        Domain currentDomain = node != null ? node.getDomain() : null;

        for (FieldDefinition field : effectiveFields) {
            if (field.getId() == null) continue;

            // Skip fields not in targetFieldKeys when workflow specifies editable fields
            if (targetFieldKeys != null && !targetFieldKeys.isEmpty()
                    && !targetFieldKeys.contains(field.getKey())) {
                continue;
            }

            // Skip DQ validation if field is hidden, read-only, or disabled by condition rules
            if (!shouldValidateDqForField(field, plainDataNode)) {
                log.debug("Skipping DQ validation for field {} due to condition rule", field.getKey());
                continue;
            }

            // Skip NOT_NULL / LENGTH / REGEX DQ rules for Auto-Numbering fields during creation
            boolean isAutoNumbering = isAutoNumberingField(currentDomain, field, effectiveFields);

            JsonNode valueNode = getValueNodeCaseInsensitive(plainDataNode, field.getKey());

            // Intrinsic validation for EMAIL field type (evaluated on decrypted plaintext)
            if ("EMAIL".equalsIgnoreCase(field.getType()) && valueNode != null && !valueNode.isNull()) {
                String textVal = valueNode.asText().trim();
                if (!textVal.isBlank() && !EMAIL_PATTERN.matcher(textVal).matches()) {
                    String displayActual = resolveDisplayActualValue(field, textVal, dataNode);
                    result.addViolation(
                            field.getKey(),
                            "EMAIL_FORMAT",
                            "ERROR",
                            Map.of("ko", "올바른 이메일 형식이 아닙니다.", "en", "Invalid email format."),
                            displayActual
                    );
                }
            }

            List<DqRule> fieldRules = rulesByField.getOrDefault(field.getId(), Collections.emptyList());
            if (fieldRules.isEmpty()) continue;

            for (DqRule rule : fieldRules) {
                if (isAutoNumbering && (rule.getRuleType() == DqRuleType.NOT_NULL || rule.getRuleType() == DqRuleType.LENGTH || rule.getRuleType() == DqRuleType.REGEX)) {
                    log.debug("Skipping DQ rule {} for auto-numbering field {}", rule.getRuleType(), field.getKey());
                    continue;
                }

                RuleEvaluator evaluator = evaluatorMap.get(rule.getRuleType());
                if (evaluator == null) {
                    log.warn("No evaluator found for rule type: {}", rule.getRuleType());
                    continue;
                }

                Optional<String> violation = evaluator.evaluate(field, rule, valueNode, context);
                if (violation.isPresent()) {
                    // Use rule's custom message if provided, otherwise use evaluator's default
                    Map<String, String> message = rule.getMessage() != null && !rule.getMessage().isEmpty()
                            ? rule.getMessage()
                            : Map.of("en", violation.get(), "ko", violation.get());

                    String rawActual = valueNode != null ? valueNode.asText() : "null";
                    String displayActual = resolveDisplayActualValue(field, rawActual, dataNode);
                    result.addViolation(
                            field.getKey(),
                            rule.getRuleType().name(),
                            rule.getSeverity().name(),
                            message,
                            displayActual,
                            rule.getId()
                    );
                }
            }
        }

        return result;
    }

    private JsonNode decryptDataNodeForDq(JsonNode dataNode, List<FieldDefinition> effectiveFields) {
        if (dataNode == null || !dataNode.isObject() || fieldEncryptionService == null) {
            return dataNode;
        }
        com.fasterxml.jackson.databind.node.ObjectNode plainNode = dataNode.deepCopy();
        Map<String, FieldDefinition> fieldMap = new HashMap<>();
        if (effectiveFields != null) {
            for (FieldDefinition f : effectiveFields) {
                if (f.getKey() != null) {
                    fieldMap.put(f.getKey().toLowerCase(), f);
                }
            }
        }

        var it = dataNode.fields();
        while (it.hasNext()) {
            var entry = it.next();
            String key = entry.getKey();
            // 메타 필드(_idx_, _mask_ 등)는 복호화 대상에서 제외
            if (key.startsWith("_")) {
                continue;
            }
            JsonNode valNode = entry.getValue();
            if (valNode.isTextual()) {
                String rawText = valNode.asText();
                FieldDefinition fd = fieldMap.get(key.toLowerCase());
                boolean isEncField = fd != null && Boolean.TRUE.equals(fd.getIsEncrypted());
                boolean isCipher = fieldEncryptionService.isEncrypted(rawText);

                if (isEncField || isCipher) {
                    try {
                        if (isCipher) {
                            String decrypted = fieldEncryptionService.decrypt(rawText);
                            plainNode.put(key, decrypted);
                        }
                    } catch (Exception e) {
                        log.warn("Field decryption failed for {}: {}", key, e.getMessage());
                    }
                }
            }
        }
        return plainNode;
    }

    private String resolveDisplayActualValue(FieldDefinition field, String rawActual, JsonNode dataNode) {
        if (rawActual == null || "null".equals(rawActual)) {
            return "null";
        }
        if (field == null) {
            return rawActual;
        }
        // 1. If dataNode contains cached masked value, use it
        if (dataNode != null && field.getKey() != null) {
            String maskKey = "_mask_" + field.getKey();
            JsonNode maskNode = dataNode.get(maskKey);
            if (maskNode != null && !maskNode.isNull() && !maskNode.asText().isBlank()) {
                return maskNode.asText();
            }
        }
        // 2. If field is encrypted or has masking pattern, apply masking
        if (Boolean.TRUE.equals(field.getIsEncrypted())) {
            if (fieldEncryptionService != null && fieldEncryptionService.isEncrypted(rawActual)) {
                return "******";
            }
            if (dataMaskingService != null && field.getMaskingPattern() != null) {
                return dataMaskingService.maskByPattern(field.getMaskingPattern(), rawActual);
            }
            return rawActual.length() <= 6 ? "******" : rawActual.substring(0, 2) + "****" + rawActual.substring(rawActual.length() - 2);
        }
        if (dataMaskingService != null && field.getMaskingPattern() != null) {
            return dataMaskingService.maskByPattern(field.getMaskingPattern(), rawActual);
        }
        return rawActual;
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private JsonNode getValueNodeCaseInsensitive(JsonNode dataNode, String fieldKey) {
        if (dataNode == null || fieldKey == null) return null;
        if (dataNode.has(fieldKey)) return dataNode.get(fieldKey);
        if (dataNode.has(fieldKey.toUpperCase())) return dataNode.get(fieldKey.toUpperCase());
        if (dataNode.has(fieldKey.toLowerCase())) return dataNode.get(fieldKey.toLowerCase());
        Iterator<String> it = dataNode.fieldNames();
        while (it.hasNext()) {
            String fn = it.next();
            if (fn.equalsIgnoreCase(fieldKey)) {
                return dataNode.get(fn);
            }
        }
        return null;
    }

    /**
     * Scan all existing records in a domain against all active DQ rules,
     * record any violations in the dq_violation table, and calculate DQ score.
     * Uses batch bulk deletion, node fields caching, and paged batch inserts for high performance.
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> runDomainDqScan(UUID domainId) {
        // Bulk delete existing violations for domain
        violationRepository.deleteByDomainId(domainId);

        // Node fields cache during scan session to prevent N+1 queries
        Map<UUID, List<FieldDefinition>> nodeFieldsCache = new HashMap<>();

        int pageSize = 500;
        int pageNumber = 0;
        org.springframework.data.domain.Page<com.classification.domain_system.entity.Record> recordPage;

        do {
            recordPage = recordRepository.findByDomainId(
                    domainId, org.springframework.data.domain.PageRequest.of(pageNumber, pageSize));
            List<DqViolation> batchViolations = new ArrayList<>();

            for (com.classification.domain_system.entity.Record record : recordPage.getContent()) {
                if (record.getNode() == null || record.getData() == null) continue;

                DqEvaluationResult evalResult = evaluate(record.getNode().getId(), record.getData(), record.getId(), nodeFieldsCache);

                for (DqEvaluationResult.Violation v : evalResult.getViolations()) {
                    DqViolation violation = new DqViolation();
                    violation.setRecordId(record.getId());
                    violation.setDqRuleId(v.getRuleId());
                    violation.setFieldKey(v.getFieldKey());
                    violation.setSeverity(v.getSeverity());
                    violation.setMessage(v.getMessage());
                    violation.setActualValue(v.getActualValue());
                    violation.setCheckedAt(java.time.LocalDateTime.now());
                    violation.setResolved(false);
                    batchViolations.add(violation);
                }
            }

            if (!batchViolations.isEmpty()) {
                violationRepository.saveAll(batchViolations);
            }
            pageNumber++;
        } while (recordPage.hasNext());

        return getDomainDqScore(domainId);
    }

    /**
     * Calculate DQ score for a domain without triggering a rescan (read-only).
     */
    public Map<String, Object> getDomainDqScore(UUID domainId) {
        Map<String, Object> scoreData = new HashMap<>();

        long totalRecords = recordRepository.findByDomainId(
                domainId, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();

        List<Object[]> violationsByField = violationRepository.countViolationsByFieldKeyForDomain(domainId);
        List<Object[]> violationsBySeverity = violationRepository.countViolationsBySeverityForDomain(domainId);

        long totalViolations = violationsByField.stream()
                .mapToLong(row -> (Long) row[1])
                .sum();

        double score = totalRecords > 0
                ? Math.round(Math.max(0.0, (1.0 - (double) totalViolations / totalRecords)) * 10000.0) / 100.0
                : 100.0;

        scoreData.put("domainId", domainId);
        scoreData.put("totalRecords", totalRecords);
        scoreData.put("totalViolations", totalViolations);
        scoreData.put("score", score);

        Map<String, Long> fieldViolations = new LinkedHashMap<>();
        for (Object[] row : violationsByField) {
            fieldViolations.put((String) row[0], (Long) row[1]);
        }
        scoreData.put("violationsByField", fieldViolations);

        Map<String, Long> severityViolations = new LinkedHashMap<>();
        for (Object[] row : violationsBySeverity) {
            severityViolations.put((String) row[0], (Long) row[1]);
        }
        scoreData.put("violationsBySeverity", severityViolations);

        return scoreData;
    }

    private boolean shouldValidateDqForField(FieldDefinition field, JsonNode dataNode) {
        if (field.getOptions() == null || field.getOptions().isBlank()) {
            return true;
        }
        try {
            JsonNode opts = objectMapper.readTree(field.getOptions());
            if (!opts.has("conditionRule")) return true;
            JsonNode ruleNode = opts.get("conditionRule");
            if (ruleNode == null || !ruleNode.has("enabled") || !ruleNode.get("enabled").asBoolean()) {
                return true;
            }

            List<String> actions = new ArrayList<>();
            if (ruleNode.has("action")) {
                JsonNode actNode = ruleNode.get("action");
                if (actNode.isArray()) {
                    for (JsonNode a : actNode) actions.add(a.asText().toUpperCase());
                } else if (actNode.isTextual() && !actNode.asText().isBlank()) {
                    actions.add(actNode.asText().toUpperCase());
                }
            }
            if (actions.isEmpty()) {
                actions.add("SHOW");
            }

            boolean isMatch = evaluateConditionRuleNode(ruleNode, dataNode);

            // 1. SHOW: If SHOW is configured and condition is NOT met -> hidden -> SKIP DQ!
            if (actions.contains("SHOW") && !isMatch) {
                return false;
            }
            // 2. READ_ONLY: If READ_ONLY is configured and condition IS met -> read-only -> SKIP DQ!
            if (actions.contains("READ_ONLY") && isMatch) {
                return false;
            }
            // 3. DISABLE: If DISABLE is configured and condition IS met -> disabled -> SKIP DQ!
            if ((actions.contains("DISABLE") || actions.contains("EDIT_FORBIDDEN")) && isMatch) {
                return false;
            }

        } catch (Exception e) {
            log.warn("Failed to parse conditionRule for field {}", field.getKey(), e);
        }
        return true;
    }

    private boolean evaluateConditionRuleNode(JsonNode ruleNode, JsonNode dataNode) {
        if (ruleNode.has("expression") && !ruleNode.get("expression").asText().isBlank()) {
            String expr = ruleNode.get("expression").asText();
            return evaluateExpression(expr, dataNode);
        }
        
        String depKey = ruleNode.has("dependsOnFieldKey") ? ruleNode.get("dependsOnFieldKey").asText() : "";
        if (depKey.isBlank()) return false;
        
        String op = ruleNode.has("operator") ? ruleNode.get("operator").asText() : "EQUALS";
        String val = ruleNode.has("value") ? ruleNode.get("value").asText() : "";
        
        JsonNode targetNode = dataNode.get(depKey);
        String targetVal = targetNode != null ? targetNode.asText() : "";
        
        return compareValues(targetVal, op, val);
    }

    private boolean evaluateExpression(String expr, JsonNode dataNode) {
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("#\\{([a-zA-Z0-9_]+)\\}").matcher(expr);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String key = matcher.group(1);
                JsonNode n = dataNode.get(key);
                String replacement;
                if (n == null || n.isNull()) {
                    replacement = "null";
                } else if (n.isNumber() || n.isBoolean()) {
                    replacement = n.asText();
                } else {
                    replacement = "'" + n.asText().replace("'", "\\'") + "'";
                }
                matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            String evalStr = sb.toString();

            return parseAndEvalBoolean(evalStr);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean parseAndEvalBoolean(String evalStr) {
        if (evalStr.contains("&&")) {
            String[] parts = evalStr.split("&&");
            for (String part : parts) {
                if (!parseAndEvalBoolean(part.trim())) return false;
            }
            return true;
        }
        if (evalStr.contains("||")) {
            String[] parts = evalStr.split("\\|\\|");
            for (String part : parts) {
                if (parseAndEvalBoolean(part.trim())) return true;
            }
            return false;
        }

        evalStr = evalStr.trim();
        if (evalStr.startsWith("(") && evalStr.endsWith(")")) {
            evalStr = evalStr.substring(1, evalStr.length() - 1).trim();
        }

        if (evalStr.contains("==")) {
            String[] p = evalStr.split("==");
            return cleanStr(p[0]).equalsIgnoreCase(cleanStr(p[1]));
        }
        if (evalStr.contains("!=")) {
            String[] p = evalStr.split("!=");
            return !cleanStr(p[0]).equalsIgnoreCase(cleanStr(p[1]));
        }
        if (evalStr.contains(">=")) {
            String[] p = evalStr.split(">=");
            return parseDouble(cleanStr(p[0])) >= parseDouble(cleanStr(p[1]));
        }
        if (evalStr.contains("<=")) {
            String[] p = evalStr.split("<=");
            return parseDouble(cleanStr(p[0])) <= parseDouble(cleanStr(p[1]));
        }
        if (evalStr.contains(">")) {
            String[] p = evalStr.split(">");
            return parseDouble(cleanStr(p[0])) > parseDouble(cleanStr(p[1]));
        }
        if (evalStr.contains("<")) {
            String[] p = evalStr.split("<");
            return parseDouble(cleanStr(p[0])) < parseDouble(cleanStr(p[1]));
        }
        return false;
    }

    private String cleanStr(String s) {
        return s == null ? "" : s.trim().replaceAll("^['\"]|['\"]$", "");
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }

    private boolean compareValues(String targetVal, String op, String val) {
        if ("EQUALS".equalsIgnoreCase(op) || "==".equals(op)) return targetVal.equalsIgnoreCase(val);
        if ("NOT_EQUALS".equalsIgnoreCase(op) || "!=".equals(op)) return !targetVal.equalsIgnoreCase(val);
        if ("CONTAINS".equalsIgnoreCase(op)) return targetVal.toLowerCase().contains(val.toLowerCase());
        if ("NOT_EMPTY".equalsIgnoreCase(op)) return !targetVal.isBlank();
        if ("EMPTY".equalsIgnoreCase(op)) return targetVal.isBlank();
        if ("GREATER_THAN".equalsIgnoreCase(op) || ">".equals(op)) return parseDouble(targetVal) > parseDouble(val);
        if ("GREATER_THAN_OR_EQUAL".equalsIgnoreCase(op) || ">=".equals(op)) return parseDouble(targetVal) >= parseDouble(val);
        if ("LESS_THAN".equalsIgnoreCase(op) || "<".equals(op)) return parseDouble(targetVal) < parseDouble(val);
        return false;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public com.classification.domain_system.dto.PageResponse<com.classification.domain_system.dto.DqViolationResponse> getDomainDqViolations(
            UUID domainId, String severity, String fieldKey, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<DqViolation> violationPage = violationRepository.findViolationsByDomainId(
                domainId, severity, fieldKey, pageable);

        List<com.classification.domain_system.dto.DqViolationResponse> responses = violationPage.getContent().stream().map(v -> {
            com.classification.domain_system.dto.DqViolationResponse dto = new com.classification.domain_system.dto.DqViolationResponse();
            dto.setId(v.getId());
            dto.setRecordId(v.getRecordId());
            dto.setDqRuleId(v.getDqRuleId());
            dto.setFieldKey(v.getFieldKey());
            dto.setSeverity(v.getSeverity());
            dto.setMessage(v.getMessage());
            dto.setActualValue(v.getActualValue());
            dto.setCheckedAt(v.getCheckedAt());
            dto.setResolved(v.getResolved());

            recordRepository.findById(v.getRecordId()).ifPresent(r -> {
                if (r.getNode() != null) {
                    dto.setNodeName(r.getNode().getName());
                }
                dto.setRecordIdentifier(extractRecordIdentifier(r));
            });

            return dto;
        }).collect(Collectors.toList());

        org.springframework.data.domain.Page<com.classification.domain_system.dto.DqViolationResponse> dtoPage =
                new org.springframework.data.domain.PageImpl<>(responses, pageable, violationPage.getTotalElements());
        return com.classification.domain_system.dto.PageResponse.of(dtoPage);
    }

    private String extractRecordIdentifier(com.classification.domain_system.entity.Record record) {
        if (record == null) {
            return "N/A";
        }
        if (record.getData() == null || record.getData().isBlank()) {
            return formatRecordCode(record.getId());
        }
        try {
            JsonNode node = objectMapper.readTree(record.getData());

            // 1. If Domain has configured identifierFieldId, prioritize that specific field
            if (record.getNode() != null && record.getNode().getDomain() != null) {
                Domain domain = record.getNode().getDomain();
                UUID idFieldId = domain.getIdentifierFieldId();
                if (idFieldId != null) {
                    Optional<FieldDefinition> idFd = fieldDefinitionRepository.findById(idFieldId);
                    if (idFd.isPresent() && idFd.get().getKey() != null) {
                        String idKey = idFd.get().getKey();
                        JsonNode valNode = getValueNodeCaseInsensitive(node, idKey);
                        if (valNode != null && !valNode.isNull()) {
                            String val = stripHtml(valNode.asText());
                            if (!val.isBlank()) return val;
                        }
                    }
                }
            }

            // 2. Strict ID/Code key preference list (NO name, title, description, memo)
            String[] preferredIdKeys = {
                    "customer_no", "CUSTOMER_NO", "material_code", "MATERIAL_CODE",
                    "sku_code", "SKU_CODE", "vendor_code", "VENDOR_CODE",
                    "emp_id", "EMP_ID", "emp_no", "EMP_NO",
                    "ticker", "TICKER", "item_code", "ITEM_CODE", "stock_code", "STOCK_CODE",
                    "code", "CODE", "no", "NO", "id", "ID"
            };

            for (String k : preferredIdKeys) {
                JsonNode valNode = getValueNodeCaseInsensitive(node, k);
                if (valNode != null && !valNode.isNull()) {
                    String val = stripHtml(valNode.asText());
                    if (!val.isBlank()) return val;
                }
            }
        } catch (Exception e) {
            // fallback
        }
        return formatRecordCode(record.getId());
    }

    private String stripHtml(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
    }

    private String formatRecordCode(UUID id) {
        if (id == null) return "N/A";
        String idStr = id.toString().replace("-", "");
        return "REC-" + idStr.substring(0, Math.min(8, idStr.length())).toUpperCase();
    }

    private boolean isAutoNumberingField(Domain domain, FieldDefinition field, List<FieldDefinition> effectiveFields) {
        if (domain == null || domain.getIdentifierFieldId() == null || field == null) {
            return false;
        }
        if (domain.getNumberingPattern() == null || domain.getNumberingPattern().isBlank()) {
            return false;
        }
        String domIdStr = domain.getIdentifierFieldId().toString();
        String fieldIdStr = field.getId() != null ? field.getId().toString() : "";
        if (domIdStr.equalsIgnoreCase(fieldIdStr)) {
            return true;
        }
        String targetKey = effectiveFields.stream()
                .filter(f -> f.getId() != null && domIdStr.equalsIgnoreCase(f.getId().toString()))
                .map(FieldDefinition::getKey)
                .findFirst()
                .orElse(null);
        return field.getKey() != null && field.getKey().equalsIgnoreCase(targetKey);
    }
}

