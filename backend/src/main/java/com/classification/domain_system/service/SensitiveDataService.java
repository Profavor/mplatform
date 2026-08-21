package com.classification.domain_system.service;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.SensitiveDataAccessLog;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.dto.SensitiveDataAccessLogDto;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.SensitiveDataAccessLogRepository;
import com.classification.domain_system.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.classification.domain_system.dto.SensitiveDataStatsDto;
import com.classification.domain_system.entity.enums.ApprovalTargetType;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensitiveDataService {

    private final FieldEncryptionService encryptionService;
    private final FieldDefinitionService fieldDefinitionService;
    private final RecordRepository recordRepository;
    private final com.classification.domain_system.repository.RecordHistoryRepository recordHistoryRepository;
    private final ApprovalRequestRepository approvalRepository;
    private final SensitiveDataAccessLogRepository accessLogRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final UserRepository userRepository;
    private final AuthContext authContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'log:read')")
    public SensitiveDataStatsDto getStatistics() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<SensitiveDataAccessLog> logs = accessLogRepository.findByAccessedAtAfter(sevenDaysAgo);

        // 1. Daily Trends
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> dailyTrends = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getAccessedAt().format(formatter),
                        Collectors.counting()
                ));

        // 2. Target Type Ratios
        Map<String, Long> targetTypeRatios = logs.stream()
                .collect(Collectors.groupingBy(
                        SensitiveDataAccessLog::getTargetType,
                        Collectors.counting()
                ));

        // 3. Top Users
        Map<String, Long> topUsers = logs.stream()
                .filter(log -> log.getUserId() != null && !log.getUserId().isEmpty())
                .collect(Collectors.groupingBy(
                        log -> {
                            if (log.getUsername() != null && !log.getUsername().isEmpty()) {
                                return log.getUsername();
                            }
                            if (userRepository != null) {
                                return userRepository.findById(log.getUserId())
                                        .map(u -> u.getUsername() != null ? u.getUsername() : log.getUserId())
                                        .orElse(log.getUserId());
                            }
                            return log.getUserId();
                        },
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));

        return SensitiveDataStatsDto.builder()
                .dailyTrends(dailyTrends)
                .targetTypeRatios(targetTypeRatios)
                .topUsers(topUsers)
                .build();
    }

    public Page<SensitiveDataAccessLogDto> getAccessLogs(String userId, UUID targetId, Pageable pageable) {
        Page<SensitiveDataAccessLog> logs;
        if (userId != null && !userId.isBlank()) {
            logs = accessLogRepository.findByUserIdOrderByAccessedAtDesc(userId, pageable);
        } else if (targetId != null) {
            logs = accessLogRepository.findByTargetIdOrderByAccessedAtDesc(targetId, pageable);
        } else {
            logs = accessLogRepository.findAllByOrderByAccessedAtDesc(pageable);
        }
        return logs.map(this::convertToDto);
    }

    public SensitiveDataAccessLogDto convertToDto(SensitiveDataAccessLog logEntity) {
        if (logEntity == null) return null;
        String normalizedIp = logEntity.getIpAddress();
        if ("0:0:0:0:0:0:0:1".equals(normalizedIp) || "::1".equals(normalizedIp)) {
            normalizedIp = "127.0.0.1";
        }

        String userDisplayName = logEntity.getUserId();
        if (userRepository != null && logEntity.getUserId() != null) {
            userDisplayName = userRepository.findById(logEntity.getUserId())
                    .map(u -> u.getUsername() != null ? u.getUsername() : logEntity.getUserId())
                    .orElse(logEntity.getUserId());
        }
        if (userDisplayName != null && userDisplayName.length() >= 32 && userDisplayName.contains("-")) {
            userDisplayName = "USER-" + logEntity.getUserId().substring(0, 8);
        }

        String formattedTargetId = null;
        if (logEntity.getTargetId() != null) {
            String uuidStr = logEntity.getTargetId().toString();
            String prefix = "APPROVAL_REQUEST".equalsIgnoreCase(logEntity.getTargetType()) ? "REQ-" : "REC-";
            formattedTargetId = prefix + uuidStr.substring(0, 8);
        }

        String domainName = null;
        String classificationName = null;
        String idAttribute = null;
        String nameAttribute = null;

        if (logEntity.getTargetId() != null) {
            ClassificationNode node = null;
            String jsonPayload = null;

            if ("APPROVAL_REQUEST".equalsIgnoreCase(logEntity.getTargetType()) && approvalRepository != null) {
                var appOpt = approvalRepository.findById(logEntity.getTargetId());
                if (appOpt.isPresent()) {
                    var app = appOpt.get();
                    node = app.getClassificationNode();
                    jsonPayload = app.getChanges();
                }
            } else if ("RECORD_HISTORY".equalsIgnoreCase(logEntity.getTargetType()) && recordHistoryRepository != null) {
                var histOpt = recordHistoryRepository.findById(logEntity.getTargetId());
                if (histOpt.isPresent()) {
                    var hist = histOpt.get();
                    if (hist.getRecord() != null) {
                        node = hist.getRecord().getNode();
                    }
                    try {
                        Map<String, Object> combined = new HashMap<>();
                        if (hist.getPreviousData() != null) {
                            combined.put("before", objectMapper.readValue(hist.getPreviousData(), new TypeReference<Map<String, Object>>() {}));
                        }
                        if (hist.getNewData() != null) {
                            combined.put("after", objectMapper.readValue(hist.getNewData(), new TypeReference<Map<String, Object>>() {}));
                        }
                        jsonPayload = objectMapper.writeValueAsString(combined);
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        log.error("Failed to parse history data", e);
                    }
                }
            } else if (recordRepository != null) {
                var recOpt = recordRepository.findById(logEntity.getTargetId());
                if (recOpt.isPresent()) {
                    var rec = recOpt.get();
                    node = rec.getNode();
                    jsonPayload = rec.getData();
                }
            }

            if (node != null) {
                classificationName = extractMapName(node.getName());
                Domain domain = node.getDomain();
                if (domain != null) {
                    domainName = extractMapName(domain.getName());
                    
                    String idKey = domain.getIdentifierField() != null ? domain.getIdentifierField().getKey() : null;
                    if (idKey == null && domain.getIdentifierFieldId() != null && fieldDefinitionRepository != null) {
                        idKey = fieldDefinitionRepository.findById(domain.getIdentifierFieldId()).map(FieldDefinition::getKey).orElse(null);
                    }
                    
                    String nameKey = domain.getDisplayNameField() != null ? domain.getDisplayNameField().getKey() : null;
                    if (nameKey == null && domain.getDisplayNameFieldId() != null && fieldDefinitionRepository != null) {
                        nameKey = fieldDefinitionRepository.findById(domain.getDisplayNameFieldId()).map(FieldDefinition::getKey).orElse(null);
                    }

                    if (idKey != null) idAttribute = extractJsonValue(jsonPayload, idKey);
                    if (nameKey != null) nameAttribute = extractJsonValue(jsonPayload, nameKey);
                }
            }
        }

        String formattedFieldLabels = logEntity.getFieldKeys();
        if (fieldDefinitionRepository != null && logEntity.getFieldKeys() != null && !logEntity.getFieldKeys().isBlank()) {
            List<String> keys = Arrays.asList(logEntity.getFieldKeys().split(","));
            List<String> labels = new ArrayList<>();
            for (String k : keys) {
                String trimmed = k.trim();
                List<FieldDefinition> matched = fieldDefinitionRepository.findByKey(trimmed);
                String label = trimmed;
                if (!matched.isEmpty() && matched.get(0).getName() != null) {
                    String extracted = extractMapName(matched.get(0).getName());
                    if (extracted != null) {
                        label = extracted;
                    }
                }
                labels.add(label);
            }
            formattedFieldLabels = String.join(", ", labels);
        }

        return SensitiveDataAccessLogDto.builder()
                .id(logEntity.getId())
                .userId(logEntity.getUserId())
                .userDisplayName(userDisplayName)
                .targetType(logEntity.getTargetType())
                .targetId(logEntity.getTargetId())
                .formattedTargetId(formattedTargetId)
                .domainName(domainName)
                .classificationName(classificationName)
                .idAttribute(idAttribute)
                .nameAttribute(nameAttribute)
                .fieldKeys(logEntity.getFieldKeys())
                .formattedFieldLabels(formattedFieldLabels)
                .accessReason(logEntity.getAccessReason())
                .ipAddress(normalizedIp)
                .accessedAt(logEntity.getAccessedAt())
                .build();
    }

    private String extractMapName(Map<String, String> nameMap) {
        if (nameMap == null || nameMap.isEmpty()) return null;
        String locale = org.springframework.context.i18n.LocaleContextHolder.getLocale().getLanguage();
        if (locale != null && !locale.isEmpty() && nameMap.containsKey(locale) && nameMap.get(locale) != null) {
            return nameMap.get(locale);
        }
        if (nameMap.containsKey("ko") && nameMap.get("ko") != null) return nameMap.get("ko");
        if (nameMap.containsKey("en") && nameMap.get("en") != null) return nameMap.get("en");
        return nameMap.values().iterator().next();
    }

    private String extractJsonValue(String jsonStr, String key) {
        if (jsonStr == null || jsonStr.isBlank() || key == null || key.isBlank()) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(jsonStr);
            return extractJsonValueFromNode(root, key);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {}
        return null;
    }

    private String extractStringOrI18nValue(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isValueNode()) {
            return node.asText().trim();
        }
        if (node.isObject()) {
            String locale = org.springframework.context.i18n.LocaleContextHolder.getLocale().getLanguage();
            if (locale != null && !locale.isEmpty() && node.has(locale) && !node.get(locale).isNull()) {
                return node.get(locale).asText().trim();
            }
            if (node.has("ko") && !node.get("ko").isNull()) return node.get("ko").asText().trim();
            if (node.has("en") && !node.get("en").isNull()) return node.get("en").asText().trim();
            java.util.Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> it = node.fields();
            if (it.hasNext()) {
                com.fasterxml.jackson.databind.JsonNode valNode = it.next().getValue();
                return valNode != null && valNode.isValueNode() ? valNode.asText().trim() : null;
            }
        }
        return null;
    }

    private String findValueIgnoreCaseDeep(com.fasterxml.jackson.databind.JsonNode node, String key) {
        if (node == null || key == null) return null;

        if (node.isObject()) {
            // 현재 레벨 검색
            if (node.has(key) && !node.get(key).isNull()) {
                String val = extractStringOrI18nValue(node.get(key));
                if (val != null && !val.isEmpty() && !val.equalsIgnoreCase("null")) {
                    return val;
                }
            }
            java.util.Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry = fields.next();
                if (entry.getKey().equalsIgnoreCase(key) && !entry.getValue().isNull()) {
                    String val = extractStringOrI18nValue(entry.getValue());
                    if (val != null && !val.isEmpty() && !val.equalsIgnoreCase("null")) {
                        return val;
                    }
                }
            }
            // 자식 노드 재귀 검색
            fields = node.fields();
            while (fields.hasNext()) {
                com.fasterxml.jackson.databind.JsonNode child = fields.next().getValue();
                if (child != null && (child.isObject() || child.isArray())) {
                    String val = findValueIgnoreCaseDeep(child, key);
                    if (val != null) return val;
                }
            }
        } else if (node.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode child : node) {
                if (child != null && (child.isObject() || child.isArray())) {
                    String val = findValueIgnoreCaseDeep(child, key);
                    if (val != null) return val;
                }
            }
        }
        return null;
    }

    private String extractJsonValueFromNode(com.fasterxml.jackson.databind.JsonNode root, String key) {
        if (root == null || key == null) return null;
        
        // 우선순위 1: after 속성 내부 깊은 탐색
        if (root.has("after")) {
            String val = findValueIgnoreCaseDeep(root.get("after"), key);
            if (val != null) return val;
        }
        // 우선순위 2: data 속성 내부 깊은 탐색
        if (root.has("data")) {
            String val = findValueIgnoreCaseDeep(root.get("data"), key);
            if (val != null) return val;
        }
        // 우선순위 3: 루트 레벨 깊은 탐색
        String val = findValueIgnoreCaseDeep(root, key);
        if (val != null) return val;
        
        // 우선순위 4: before 속성 (최후의 수단)
        if (root.has("before")) {
            val = findValueIgnoreCaseDeep(root.get("before"), key);
            if (val != null) return val;
        }
        return null;
    }

    @Transactional
    @PreAuthorize("hasPermission(null, 'record:unmask')")
    public Map<String, String> decryptApprovalFields(UUID approvalId, List<String> fieldKeys, String accessReason, String ipAddress) {
        ApprovalRequest approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest not found"));

        ClassificationNode node = approval.getClassificationNode();
        if (node == null && approval.getTargetId() != null) {
            node = recordRepository.findById(approval.getTargetId()).map(Record::getNode).orElse(null);
        }
        if (node == null) {
            return Collections.emptyMap();
        }

        List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(node.getId());
        Map<String, String> decryptedMap = decryptFromDataJson(approval.getChanges(), fields, fieldKeys);

        if (!decryptedMap.isEmpty()) {
            saveAccessLog("APPROVAL_REQUEST", approvalId, extractLoggedKeys(fields, fieldKeys, decryptedMap), accessReason, ipAddress);
        }

        return decryptedMap;
    }

    @Transactional
    @PreAuthorize("hasPermission(null, 'record:unmask')")
    public Map<String, String> decryptRecordFields(UUID recordId, List<String> fieldKeys, String accessReason, String ipAddress) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));

        if (record.getNode() == null) {
            return Collections.emptyMap();
        }

        List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(record.getNode().getId());
        Map<String, String> decryptedMap = decryptFromDataJson(record.getData(), fields, fieldKeys);

        if (!decryptedMap.isEmpty()) {
            saveAccessLog(ApprovalTargetType.RECORD.name(), recordId, extractLoggedKeys(fields, fieldKeys, decryptedMap), accessReason, ipAddress);
        }

        return decryptedMap;
    }

    @Transactional
    @PreAuthorize("hasPermission(null, 'record:unmask')")
    public Map<String, String> decryptHistoryFields(UUID historyId, List<String> fieldKeys, String accessReason, String ipAddress) {
        com.classification.domain_system.entity.RecordHistory history = 
            recordHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("History not found"));

        if (history.getRecord() == null || history.getRecord().getNode() == null) {
            return Collections.emptyMap();
        }

        List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(history.getRecord().getNode().getId());
        
        // combine previousData and newData to decrypt both
        Map<String, Object> combined = new HashMap<>();
        try {
            if (history.getPreviousData() != null) {
                combined.put("before", objectMapper.readValue(history.getPreviousData(), new TypeReference<Map<String, Object>>() {}));
            }
            if (history.getNewData() != null) {
                combined.put("after", objectMapper.readValue(history.getNewData(), new TypeReference<Map<String, Object>>() {}));
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse history data", e);
        }

        Map<String, String> decryptedMap = new HashMap<>();
        try {
            String combinedJson = objectMapper.writeValueAsString(combined);
            decryptedMap = decryptFromDataJson(combinedJson, fields, fieldKeys);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to serialize combined history data: {}", e.getMessage());
        }

        if (!decryptedMap.isEmpty()) {
            saveAccessLog("RECORD_HISTORY", historyId, extractLoggedKeys(fields, fieldKeys, decryptedMap), accessReason, ipAddress);
        }

        return decryptedMap;
    }

    private List<String> extractLoggedKeys(List<FieldDefinition> fields, List<String> requestedKeys, Map<String, String> decryptedMap) {
        if (requestedKeys != null && !requestedKeys.isEmpty()) {
            return requestedKeys;
        }
        if (fields == null) return new ArrayList<>(decryptedMap.keySet());
        return fields.stream()
                .map(FieldDefinition::getKey)
                .filter(k -> k != null && decryptedMap.containsKey(k))
                .distinct()
                .toList();
    }

    private Map<String, String> decryptFromDataJson(String jsonStr, List<FieldDefinition> fields, List<String> filterKeys) {
        if (jsonStr == null || jsonStr.isBlank() || fields == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();
        try {
            Map<String, Object> dataMap = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            
            // If before/after structure (e.g. ApprovalRequest changes)
            if (dataMap.containsKey("before") || dataMap.containsKey("after")) {
                Map<String, Object> merged = new HashMap<>();
                if (dataMap.get("before") instanceof Map<?, ?> bMap) {
                    bMap.forEach((k, v) -> merged.put(String.valueOf(k), v));
                }
                if (dataMap.get("after") instanceof Map<?, ?> aMap) {
                    aMap.forEach((k, v) -> merged.put(String.valueOf(k), v));
                }
                dataMap = merged;
            }

            Set<String> filterSet = (filterKeys != null && !filterKeys.isEmpty()) ? new HashSet<>(filterKeys) : null;

            for (FieldDefinition field : fields) {
                if (field.getKey() != null) {
                    String matchedKey = null;
                    for (String k : dataMap.keySet()) {
                        if (k.equalsIgnoreCase(field.getKey()) || (field.getId() != null && k.equalsIgnoreCase(String.valueOf(field.getId())))) {
                            matchedKey = k;
                            break;
                        }
                    }
                    if (matchedKey != null) {
                        boolean matchesFilter = (filterSet == null);
                        if (filterSet != null) {
                            for (String fk : filterSet) {
                                if (fk.equalsIgnoreCase(matchedKey) 
                                        || fk.equalsIgnoreCase(field.getKey()) 
                                        || (field.getId() != null && fk.equalsIgnoreCase(String.valueOf(field.getId())))) {
                                    matchesFilter = true;
                                    break;
                                }
                            }
                        }
                        if (!matchesFilter) {
                            continue;
                        }

                        Object rawValObj = dataMap.get(matchedKey);
                        if (rawValObj instanceof String rawVal && !rawVal.isBlank()) {
                            String decrypted = decryptUntilPlaintext(rawVal);
                            result.put(field.getKey(), decrypted);
                            result.put(field.getKey().toLowerCase(), decrypted);
                            result.put(field.getKey().toUpperCase(), decrypted);
                            String camel = toCamelCase(field.getKey());
                            if (camel != null) result.put(camel, decrypted);
                            String snake = toSnakeCase(field.getKey());
                            if (snake != null) result.put(snake, decrypted);

                            result.put(matchedKey, decrypted);
                            result.put(matchedKey.toLowerCase(), decrypted);
                            result.put(matchedKey.toUpperCase(), decrypted);
                            String matchedCamel = toCamelCase(matchedKey);
                            if (matchedCamel != null) result.put(matchedCamel, decrypted);
                            String matchedSnake = toSnakeCase(matchedKey);
                            if (matchedSnake != null) result.put(matchedSnake, decrypted);

                            if (field.getId() != null) {
                                result.put(String.valueOf(field.getId()), decrypted);
                            }
                        }
                    }
                }
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse JSON data for decryption", e);
        }
        return result;
    }

    private String toCamelCase(String s) {
        if (s == null || !s.contains("_")) return s;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : s.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                sb.append(nextUpper ? Character.toUpperCase(c) : Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return sb.toString();
    }

    private String toSnakeCase(String s) {
        if (s == null) return null;
        return s.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    private String decryptUntilPlaintext(String val) {
        if (val == null || val.isBlank() || encryptionService == null) return val;
        String current = val;
        for (int i = 0; i < 5; i++) {
            String next = encryptionService.decrypt(current);
            if (next == null || next.equals(current)) {
                break;
            }
            current = next;
        }
        return current;
    }

    private void saveAccessLog(String targetType, UUID targetId, List<String> fieldKeys, String accessReason, String ipAddress) {
        try {
            SensitiveDataAccessLog accessLog = new SensitiveDataAccessLog();
            String userId = authContext != null && authContext.getUserId() != null ? authContext.getUserId() : "SYSTEM";
            accessLog.setUserId(userId);
            
            if (userRepository != null && !"SYSTEM".equals(userId)) {
                userRepository.findById(userId).ifPresent(u -> accessLog.setUsername(u.getUsername()));
            }
            
            accessLog.setTargetType(targetType);
            accessLog.setTargetId(targetId);
            accessLog.setFieldKeys(String.join(",", fieldKeys));
            accessLog.setAccessReason(accessReason);
            String normalizedIp = ipAddress;
            if ("0:0:0:0:0:0:0:1".equals(normalizedIp) || "::1".equals(normalizedIp)) {
                normalizedIp = "127.0.0.1";
            }
            accessLog.setIpAddress(normalizedIp);
            accessLogRepository.save(accessLog);
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("Failed to save sensitive data access log — aborting data access for compliance", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Audit log persistence failed. Sensitive data access denied for compliance.");
        }
    }
}
