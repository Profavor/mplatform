package com.classification.domain_system.service;

import com.classification.domain_system.dto.BatchValidationResult;
import com.classification.domain_system.dto.BatchValidationResult.FieldViolation;
import com.classification.domain_system.dto.BatchValidationResult.RowValidationDetail;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqRuleEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Excel 배치 업로드의 각 행에 대해 DQ 검증을 수행하는 서비스.
 * 실제 저장은 수행하지 않으며, 검증 결과만 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class BatchValidationService {

    private final DqRuleEngine dqRuleEngine;

    /**
     * 배치 요청의 각 행에 대해 DQ 검증을 수행합니다.
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

        List<RowValidationDetail> details = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;

        for (int i = 0; i < requests.size(); i++) {
            RecordRequest req = requests.get(i);
            int rowNumber = i + 1; // 1-based row number (Excel 기준)

            DqEvaluationResult evalResult = dqRuleEngine.evaluate(nodeId, req.getData());

            List<FieldViolation> violations = evalResult.getViolations().stream()
                    .map(v -> FieldViolation.builder()
                            .fieldKey(v.getFieldKey())
                            .ruleType(v.getRuleType())
                            .severity(v.getSeverity())
                            .message(v.getMessage())
                            .actualValue(v.getActualValue())
                            .build())
                    .collect(Collectors.toList());

            boolean hasErrors = evalResult.getViolations().stream()
                    .anyMatch(v -> "ERROR".equals(v.getSeverity()));

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
}
