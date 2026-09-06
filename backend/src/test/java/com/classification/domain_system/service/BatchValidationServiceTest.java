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

    @Mock
    private com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;

    @Mock
    private com.classification.domain_system.repository.ClassificationNodeRepository classificationNodeRepository;

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

    @Test
    @DisplayName("스키마 필수 필드가 누락된 경우 NOT_NULL ERROR 위반을 발생시킨다")
    void validateBatch_SchemaRequiredFieldMissing_FailsWithNotNullViolation() {
        com.classification.domain_system.entity.FieldDefinition reqField = createField("itemCode", "TEXT", true);
        when(fieldDefinitionRepository.findNodeFieldsWithSort(eq(nodeId))).thenReturn(List.of(reqField));

        String dataMissingKey = "{\"description\":\"only description\"}";
        RecordRequest req = new RecordRequest();
        req.setData(dataMissingKey);

        when(dqRuleEngine.evaluate(eq(nodeId), eq(dataMissingKey))).thenReturn(new DqEvaluationResult());

        BatchValidationResult result = batchValidationService.validateBatch(nodeId, List.of(req));

        assertThat(result.getInvalidRows()).isEqualTo(1);
        assertThat(result.getValidRows()).isEqualTo(0);
        assertThat(result.getDetails().get(0).isValid()).isFalse();
        assertThat(result.getDetails().get(0).getViolations()).anyMatch(v -> 
            "itemCode".equals(v.getFieldKey()) && "NOT_NULL".equals(v.getRuleType()) && "ERROR".equals(v.getSeverity())
        );
    }

    @Test
    @DisplayName("숫자 타입 필드에 비숫자 문자열이 들어온 경우 TYPE_MISMATCH ERROR 위반을 발생시킨다")
    void validateBatch_SchemaNumberTypeInvalid_FailsWithTypeMismatch() {
        com.classification.domain_system.entity.FieldDefinition numField = createField("price", "NUMBER", false);
        when(fieldDefinitionRepository.findNodeFieldsWithSort(eq(nodeId))).thenReturn(List.of(numField));

        String dataInvalidNumber = "{\"price\":\"not_a_number\"}";
        RecordRequest req = new RecordRequest();
        req.setData(dataInvalidNumber);

        when(dqRuleEngine.evaluate(eq(nodeId), eq(dataInvalidNumber))).thenReturn(new DqEvaluationResult());

        BatchValidationResult result = batchValidationService.validateBatch(nodeId, List.of(req));

        assertThat(result.getInvalidRows()).isEqualTo(1);
        assertThat(result.getDetails().get(0).isValid()).isFalse();
        assertThat(result.getDetails().get(0).getViolations()).anyMatch(v -> 
            "price".equals(v.getFieldKey()) && "TYPE_MISMATCH".equals(v.getRuleType())
        );
    }

    @Test
    @DisplayName("날짜 타입 필드에 잘못된 형식의 값이 들어온 경우 TYPE_MISMATCH ERROR 위반을 발생시킨다")
    void validateBatch_SchemaDateFormatInvalid_FailsWithTypeMismatch() {
        com.classification.domain_system.entity.FieldDefinition dateField = createField("startDate", "DATE", false);
        when(fieldDefinitionRepository.findNodeFieldsWithSort(eq(nodeId))).thenReturn(List.of(dateField));

        String dataInvalidDate = "{\"startDate\":\"2026/99/99\"}";
        RecordRequest req = new RecordRequest();
        req.setData(dataInvalidDate);

        when(dqRuleEngine.evaluate(eq(nodeId), eq(dataInvalidDate))).thenReturn(new DqEvaluationResult());

        BatchValidationResult result = batchValidationService.validateBatch(nodeId, List.of(req));

        assertThat(result.getInvalidRows()).isEqualTo(1);
        assertThat(result.getDetails().get(0).isValid()).isFalse();
        assertThat(result.getDetails().get(0).getViolations()).anyMatch(v -> 
            "startDate".equals(v.getFieldKey()) && "TYPE_MISMATCH".equals(v.getRuleType())
        );
    }

    @Test
    @DisplayName("배치 파일 내 식별자(Business Key)가 중복 등장할 경우 DUPLICATE ERROR 위반을 발생시킨다")
    void validateBatch_DuplicateBusinessKeysInBatch_FailsWithDuplicateKeyViolation() {
        com.classification.domain_system.entity.FieldDefinition keyField = createField("PRODUCT_ID", "TEXT", true);
        when(fieldDefinitionRepository.findNodeFieldsWithSort(eq(nodeId))).thenReturn(List.of(keyField));

        String row1Data = "{\"PRODUCT_ID\":\"P1001\",\"name\":\"Product 1\"}";
        String row2Data = "{\"PRODUCT_ID\":\"P1001\",\"name\":\"Product 2 (Dup)\"}";

        RecordRequest req1 = new RecordRequest();
        req1.setData(row1Data);
        RecordRequest req2 = new RecordRequest();
        req2.setData(row2Data);

        when(dqRuleEngine.evaluate(eq(nodeId), eq(row1Data))).thenReturn(new DqEvaluationResult());
        when(dqRuleEngine.evaluate(eq(nodeId), eq(row2Data))).thenReturn(new DqEvaluationResult());

        BatchValidationResult result = batchValidationService.validateBatch(nodeId, List.of(req1, req2));

        assertThat(result.getTotalRows()).isEqualTo(2);
        // 첫 번째 행은 정상 혹은 두 번째 행에서 중복 검출되어 invalid
        assertThat(result.getInvalidRows()).isGreaterThanOrEqualTo(1);
        assertThat(result.getDetails().get(1).getViolations()).anyMatch(v -> 
            "PRODUCT_ID".equals(v.getFieldKey()) && "DUPLICATE".equals(v.getRuleType())
        );
    }

    private com.classification.domain_system.entity.FieldDefinition createField(String key, String type, boolean required) {
        com.classification.domain_system.entity.FieldDefinition f = new com.classification.domain_system.entity.FieldDefinition();
        f.setId(UUID.randomUUID());
        f.setKey(key);
        f.setType(type);
        f.setRequired(required);
        f.setName(Map.of("ko", key, "en", key));
        return f;
    }
}
