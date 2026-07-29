package com.classification.domain_system.service;

import com.classification.domain_system.dto.BatchValidationResult;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchValidationServiceTest {

    @Mock
    private DqRuleEngine dqRuleEngine;

    @InjectMocks
    private BatchValidationService batchValidationService;

    private UUID nodeId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("유효한 행과 무효한 행이 혼합된 배치 검증 시 정확한 행별 결과를 반환한다")
    void validateBatch_MixedRows_ReturnsCorrectDetails() {
        String validData = "{\"name\":\"Test\",\"email\":\"test@test.com\"}";
        String invalidData = "{\"name\":\"\",\"email\":\"invalid\"}";

        RecordRequest validReq = new RecordRequest();
        validReq.setData(validData);

        RecordRequest invalidReq = new RecordRequest();
        invalidReq.setData(invalidData);

        // 유효한 행: 위반 없음
        DqEvaluationResult validResult = new DqEvaluationResult();
        when(dqRuleEngine.evaluate(eq(nodeId), eq(validData))).thenReturn(validResult);

        // 무효한 행: ERROR 위반 2건
        DqEvaluationResult invalidResult = new DqEvaluationResult();
        invalidResult.addViolation("name", "NOT_NULL", "ERROR",
                Map.of("ko", "필수 항목입니다", "en", "Required field"), "");
        invalidResult.addViolation("email", "REGEX", "ERROR",
                Map.of("ko", "이메일 형식이 아닙니다", "en", "Invalid email format"), "invalid");
        when(dqRuleEngine.evaluate(eq(nodeId), eq(invalidData))).thenReturn(invalidResult);

        BatchValidationResult result = batchValidationService.validateBatch(nodeId, List.of(validReq, invalidReq));

        assertThat(result.getTotalRows()).isEqualTo(2);
        assertThat(result.getValidRows()).isEqualTo(1);
        assertThat(result.getInvalidRows()).isEqualTo(1);
        assertThat(result.getDetails()).hasSize(2);

        // Row 1: 유효
        assertThat(result.getDetails().get(0).getRowNumber()).isEqualTo(1);
        assertThat(result.getDetails().get(0).isValid()).isTrue();
        assertThat(result.getDetails().get(0).getViolations()).isEmpty();

        // Row 2: 무효 - ERROR 2건
        assertThat(result.getDetails().get(1).getRowNumber()).isEqualTo(2);
        assertThat(result.getDetails().get(1).isValid()).isFalse();
        assertThat(result.getDetails().get(1).getViolations()).hasSize(2);
        assertThat(result.getDetails().get(1).getViolations().get(0).getFieldKey()).isEqualTo("name");
        assertThat(result.getDetails().get(1).getViolations().get(1).getFieldKey()).isEqualTo("email");
    }

    @Test
    @DisplayName("WARNING만 있는 행은 valid로 처리한다")
    void validateBatch_WarningOnly_IsValid() {
        String data = "{\"name\":\"Test\"}";
        RecordRequest req = new RecordRequest();
        req.setData(data);

        DqEvaluationResult warningResult = new DqEvaluationResult();
        warningResult.addViolation("phone", "REGEX", "WARNING",
                Map.of("ko", "형식 확인 필요", "en", "Format check needed"), "123");
        when(dqRuleEngine.evaluate(eq(nodeId), eq(data))).thenReturn(warningResult);

        BatchValidationResult result = batchValidationService.validateBatch(nodeId, List.of(req));

        assertThat(result.getValidRows()).isEqualTo(1);
        assertThat(result.getInvalidRows()).isEqualTo(0);
        assertThat(result.getDetails().get(0).isValid()).isTrue();
        assertThat(result.getDetails().get(0).getViolations()).hasSize(1);
        assertThat(result.getDetails().get(0).getViolations().get(0).getSeverity()).isEqualTo("WARNING");
    }

    @Test
    @DisplayName("빈 요청 목록은 0건 결과를 반환한다")
    void validateBatch_EmptyList_ReturnsZero() {
        BatchValidationResult result = batchValidationService.validateBatch(nodeId, Collections.emptyList());

        assertThat(result.getTotalRows()).isEqualTo(0);
        assertThat(result.getValidRows()).isEqualTo(0);
        assertThat(result.getInvalidRows()).isEqualTo(0);
        assertThat(result.getDetails()).isEmpty();
    }

    @Test
    @DisplayName("null 요청 목록은 0건 결과를 반환한다")
    void validateBatch_NullList_ReturnsZero() {
        BatchValidationResult result = batchValidationService.validateBatch(nodeId, null);

        assertThat(result.getTotalRows()).isEqualTo(0);
        assertThat(result.getDetails()).isEmpty();
    }
}
