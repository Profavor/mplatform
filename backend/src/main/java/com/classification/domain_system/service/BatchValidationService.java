package com.classification.domain_system.service;

import com.classification.domain_system.dto.BatchValidationResult;
import com.classification.domain_system.dto.BatchValidationResult.FieldViolation;
import com.classification.domain_system.dto.BatchValidationResult.RowValidationDetail;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqRuleEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel 배치 업로드의 각 행에 대해 DQ 검증을 수행하는 서비스.
 * 실제 저장은 수행하지 않으며, 검증 결과만 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class BatchValidationService {

    private final DqRuleEngine dqRuleEngine;
    private final com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    private final com.classification.domain_system.repository.ClassificationNodeRepository classificationNodeRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    private static final java.time.format.DateTimeFormatter ISO_DATE_FORMATTER = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * 배치 요청의 각 행에 대해 스키마 필드 및 DQ 검증을 수행합니다.
     *
     * @param nodeId   분류 노드 ID
     * @param requests 업로드할 레코드 요청 목록
     * @return 행 단위 검증 결과
     */
    public BatchValidationResult validateBatch(UUID nodeId, List<RecordRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return BatchValidationResult.builder()
                    .totalRows(0)
                    .validRows(0)
                    .invalidRows(0)
                    .details(Collections.emptyList())
                    .build();
        }

        // 1. 노드 스키마 필드 및 식별자 키 추출
        List<com.classification.domain_system.entity.FieldDefinition> fields = Collections.emptyList();
        if (fieldDefinitionRepository != null && nodeId != null) {
            try {
                fields = fieldDefinitionRepository.findNodeFieldsWithSort(nodeId);
            } catch (Exception e) {
                fields = Collections.emptyList();
            }
        }
        if (fields == null) {
            fields = Collections.emptyList();
        }

        Set<String> uniqueKeys = resolveUniqueKeys(nodeId, fields);
        Map<String, Set<String>> seenUniqueValues = new HashMap<>();
        for (String k : uniqueKeys) {
            seenUniqueValues.put(k, new HashSet<>());
        }

        List<RowValidationDetail> details = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;

        for (int i = 0; i < requests.size(); i++) {
            RecordRequest req = requests.get(i);
            int rowNumber = i + 1; // 1-based row number (Excel 기준)
            List<FieldViolation> violations = new ArrayList<>();

            // A. 스키마 필드 유효성 검증
            Map<String, Object> dataMap = parseData(req != null ? req.getData() : null);
            for (com.classification.domain_system.entity.FieldDefinition f : fields) {
                String key = f.getKey();
                if (key == null) continue;

                Object rawVal = dataMap.get(key);
                String strVal = extractStringValue(rawVal);
                boolean isEmpty = (strVal == null || strVal.trim().isEmpty());

                // 1) 필수값(Required) 검증
                if (Boolean.TRUE.equals(f.getRequired()) && isEmpty) {
                    violations.add(FieldViolation.builder()
                            .fieldKey(key)
                            .ruleType("NOT_NULL")
                            .severity("ERROR")
                            .message(Map.of("ko", "필수 입력 항목입니다.", "en", "This field is required."))
                            .actualValue("")
                            .build());
                    continue;
                }

                // 비어있으면 타입/유일성 검사는 통과
                if (isEmpty) continue;

                // 2) 데이터 타입 검증
                String type = f.getType() != null ? f.getType().toUpperCase() : "TEXT";
                if ("NUMBER".equals(type)) {
                    if (!isValidNumber(strVal)) {
                        violations.add(FieldViolation.builder()
                                .fieldKey(key)
                                .ruleType("TYPE_MISMATCH")
                                .severity("ERROR")
                                .message(Map.of("ko", "유효한 숫자 형식이 아닙니다.", "en", "Invalid number format."))
                                .actualValue(strVal)
                                .build());
                    }
                } else if ("DATE".equals(type)) {
                    if (!isValidDate(strVal)) {
                        violations.add(FieldViolation.builder()
                                .fieldKey(key)
                                .ruleType("TYPE_MISMATCH")
                                .severity("ERROR")
                                .message(Map.of("ko", "유효한 날짜 형식(YYYY-MM-DD)이 아닙니다.", "en", "Invalid date format (YYYY-MM-DD)."))
                                .actualValue(strVal)
                                .build());
                    }
                }

                // 3) 배치 파일 내 중복(Uniqueness) 검증
                if (uniqueKeys.contains(key)) {
                    Set<String> seen = seenUniqueValues.get(key);
                    if (seen != null) {
                        String normalizedVal = strVal.trim();
                        if (seen.contains(normalizedVal)) {
                            violations.add(FieldViolation.builder()
                                    .fieldKey(key)
                                    .ruleType("DUPLICATE")
                                    .severity("ERROR")
                                    .message(Map.of("ko", "배치 파일 내 중복된 키 값입니다.", "en", "Duplicate key value in batch file."))
                                    .actualValue(normalizedVal)
                                    .build());
                        } else {
                            seen.add(normalizedVal);
                        }
                    }
                }
            }

            // B. DQ 룰 엔진 검증 수행
            if (dqRuleEngine != null && req != null && req.getData() != null) {
                try {
                    DqEvaluationResult evalResult = dqRuleEngine.evaluate(nodeId, req.getData());
                    if (evalResult != null && evalResult.getViolations() != null) {
                        for (com.classification.domain_system.service.dq.DqEvaluationResult.Violation v : evalResult.getViolations()) {
                            // 이미 스키마 레벨에서 동일 필드/동일 룰에 대해 등록된 경우 중복 방지
                            boolean alreadyExists = violations.stream().anyMatch(ex ->
                                    ex.getFieldKey() != null && ex.getFieldKey().equals(v.getFieldKey()) &&
                                    ex.getRuleType() != null && ex.getRuleType().equals(v.getRuleType()));
                            if (!alreadyExists) {
                                violations.add(FieldViolation.builder()
                                        .fieldKey(v.getFieldKey())
                                        .ruleType(v.getRuleType())
                                        .severity(v.getSeverity())
                                        .message(v.getMessage())
                                        .actualValue(v.getActualValue())
                                        .build());
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            boolean hasErrors = violations.stream()
                    .anyMatch(v -> "ERROR".equalsIgnoreCase(v.getSeverity()));

            boolean valid = !hasErrors;
            if (valid) {
                validCount++;
            } else {
                invalidCount++;
            }

            details.add(RowValidationDetail.builder()
                    .rowNumber(rowNumber)
                    .valid(valid)
                    .violations(violations)
                    .build());
        }

        return BatchValidationResult.builder()
                .totalRows(requests.size())
                .validRows(validCount)
                .invalidRows(invalidCount)
                .details(details)
                .build();
    }

    private Set<String> resolveUniqueKeys(UUID nodeId, List<com.classification.domain_system.entity.FieldDefinition> nodeFields) {
        Set<String> uniqueKeys = new HashSet<>();
        if (nodeFields != null) {
            for (com.classification.domain_system.entity.FieldDefinition f : nodeFields) {
                String k = f.getKey();
                if (k != null && (k.equalsIgnoreCase("PRODUCT_ID") || k.equalsIgnoreCase("ITEM_ID")
                        || k.equalsIgnoreCase("CODE") || k.equalsIgnoreCase("ID")
                        || k.equalsIgnoreCase("SKU") || k.equalsIgnoreCase("RECORD_ID"))) {
                    uniqueKeys.add(k);
                }
            }
        }
        if (uniqueKeys.isEmpty() && classificationNodeRepository != null && nodeId != null) {
            try {
                classificationNodeRepository.findById(nodeId).ifPresent(node -> {
                    if (node.getDomain() != null && node.getDomain().getIdentifierFieldId() != null) {
                        UUID idFieldId = node.getDomain().getIdentifierFieldId();
                        if (nodeFields != null) {
                            nodeFields.stream()
                                    .filter(f -> idFieldId.equals(f.getId()))
                                    .findFirst()
                                    .ifPresent(f -> uniqueKeys.add(f.getKey()));
                        }
                    }
                });
            } catch (Exception ignored) {}
        }
        return uniqueKeys;
    }

    private Map<String, Object> parseData(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private String extractStringValue(Object val) {
        if (val == null) return null;
        if (val instanceof Map<?, ?> map) {
            // 다국어 필드인 경우 ko 또는 en 또는 첫 번째 값
            Object ko = map.get("ko");
            if (ko != null && !String.valueOf(ko).trim().isEmpty()) return String.valueOf(ko);
            Object en = map.get("en");
            if (en != null && !String.valueOf(en).trim().isEmpty()) return String.valueOf(en);
            return map.values().stream().filter(Objects::nonNull).map(String::valueOf).findFirst().orElse("");
        }
        return String.valueOf(val);
    }

    private boolean isValidNumber(String val) {
        if (val == null || val.trim().isEmpty()) return true;
        try {
            Double.parseDouble(val.trim().replace(",", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidDate(String val) {
        if (val == null || val.trim().isEmpty()) return true;
        String trimmed = val.trim();
        try {
            java.time.LocalDate.parse(trimmed, ISO_DATE_FORMATTER);
            return true;
        } catch (Exception ignored) {
            // yyyy-MM-dd 포맷 실패 시
            return false;
        }
    }
}
