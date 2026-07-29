package com.classification.domain_system.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Excel 배치 업로드 행 단위 DQ 검증 결과.
 */
@Getter
@Builder
public class BatchValidationResult {

    /** 전체 행 수 */
    private final int totalRows;
    /** 검증 통과 행 수 */
    private final int validRows;
    /** 검증 실패 행 수 */
    private final int invalidRows;
    /** 행별 상세 검증 결과 */
    private final List<RowValidationDetail> details;

    @Getter
    @Builder
    public static class RowValidationDetail {
        /** 행 번호 (1-based, Excel 기준) */
        private final int rowNumber;
        /** 검증 통과 여부 */
        private final boolean valid;
        /** 위반 사항 목록 */
        private final List<FieldViolation> violations;
    }

    @Getter
    @Builder
    public static class FieldViolation {
        /** 필드 키 */
        private final String fieldKey;
        /** DQ 룰 유형 (NOT_NULL, LENGTH, REGEX 등) */
        private final String ruleType;
        /** 심각도 (ERROR, WARNING) */
        private final String severity;
        /** 위반 메시지 (다국어) */
        private final Map<String, String> message;
        /** 입력된 실제 값 */
        private final String actualValue;
    }
}
