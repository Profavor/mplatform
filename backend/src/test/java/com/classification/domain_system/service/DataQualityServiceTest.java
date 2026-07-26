package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.DqEvaluationResult;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.classification.domain_system.repository.ClassificationNodeRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataQualityServiceTest {

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private DqRuleEngine dqRuleEngine;

    @InjectMocks
    private DataQualityService dataQualityService;

    private UUID nodeId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        lenient().when(nodeRepository.findById(nodeId)).thenReturn(Optional.empty());
        // Default: engine returns empty result (no violations) — both old and new signature
        lenient().when(dqRuleEngine.evaluate(eq(nodeId), any(), any()))
                .thenReturn(new DqEvaluationResult());
        lenient().when(dqRuleEngine.evaluate(eq(nodeId), any(), any(), any(), any()))
                .thenReturn(new DqEvaluationResult());
    }

    // ─────────────────────────────────────────────────────────────────
    // validateData - Required 체크 (legacy)
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validateData - Required 필드 검증")
    class RequiredCheck {

        @Test
        @DisplayName("required 필드가 null이면 isValid=false, 에러 메시지 추가")
        void requiredFieldNull_Invalid() {
            FieldDefinition field = makeField("name", "TEXT", true);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"name\":null}");

            assertThat(result.isValid).isFalse();
            assertThat(result.errors).anyMatch(e -> e.contains("required") || e.contains("is required"));
        }

        @Test
        @DisplayName("required 필드가 빈 문자열이면 isValid=false")
        void requiredFieldEmpty_Invalid() {
            FieldDefinition field = makeField("name", "TEXT", true);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"name\":\"   \"}");

            assertThat(result.isValid).isFalse();
        }

        @Test
        @DisplayName("required 필드가 정상 값이면 isValid=true")
        void requiredFieldPresent_Valid() {
            FieldDefinition field = makeField("name", "TEXT", true);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"name\":\"홍길동\"}");

            assertThat(result.isValid).isTrue();
            assertThat(result.errors).isEmpty();
        }

        @Test
        @DisplayName("required=false 필드가 누락돼도 isValid=true")
        void nonRequiredFieldMissing_Valid() {
            FieldDefinition field = makeField("remark", "TEXT", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{}");

            assertThat(result.isValid).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // validateData - 타입 체크 (legacy)
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validateData - 타입 검증")
    class TypeCheck {

        @Test
        @DisplayName("NUMBER 필드에 문자열 'abc' 입력 시 타입 에러")
        void numberField_StringValue_TypeError() {
            FieldDefinition field = makeField("price", "NUMBER", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"price\":\"abc\"}");

            assertThat(result.isValid).isFalse();
            assertThat(result.errors).anyMatch(e -> e.contains("number") || e.contains("NUMBER"));
        }

        @Test
        @DisplayName("NUMBER 필드에 '3.14' 문자열 입력 시 통과")
        void numberField_NumericString_Valid() {
            FieldDefinition field = makeField("price", "NUMBER", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"price\":\"3.14\"}");

            assertThat(result.isValid).isTrue();
        }

        @Test
        @DisplayName("NUMBER 필드에 JSON 숫자 타입 입력 시 통과")
        void numberField_JsonNumber_Valid() {
            FieldDefinition field = makeField("price", "NUMBER", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"price\":100}");

            assertThat(result.isValid).isTrue();
        }

        @Test
        @DisplayName("BOOLEAN 필드에 'true' 문자열 허용")
        void booleanField_TrueString_Valid() {
            FieldDefinition field = makeField("active", "BOOLEAN", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"active\":\"true\"}");

            assertThat(result.isValid).isTrue();
        }

        @Test
        @DisplayName("BOOLEAN 필드에 'yes' 문자열 거부")
        void booleanField_YesString_Invalid() {
            FieldDefinition field = makeField("active", "BOOLEAN", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"active\":\"yes\"}");

            assertThat(result.isValid).isFalse();
            assertThat(result.errors).anyMatch(e -> e.contains("boolean") || e.contains("BOOLEAN"));
        }

        @Test
        @DisplayName("CHECKBOX 필드에 JSON boolean true 허용")
        void checkboxField_JsonBoolean_Valid() {
            FieldDefinition field = makeField("checked", "CHECKBOX", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"checked\":true}");

            assertThat(result.isValid).isTrue();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // validateData - 기타
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validateData - 기타")
    class Misc {

        @Test
        @DisplayName("잘못된 JSON 형식이면 isValid=false, 'Invalid JSON format' 에러")
        void invalidJson_ReturnsInvalidResult() {
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of());
            // Engine also catches this
            DqEvaluationResult engineResult = new DqEvaluationResult();
            engineResult.addViolation("_json", "PARSE", "ERROR",
                    Map.of("en", "Invalid JSON format"), "NOT_JSON");
            when(dqRuleEngine.evaluate(eq(nodeId), eq("NOT_JSON"), any(), any(), any())).thenReturn(engineResult);

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "NOT_JSON");

            assertThat(result.isValid).isFalse();
            assertThat(result.errors).anyMatch(e -> e.contains("Invalid JSON"));
        }

        @Test
        @DisplayName("getFieldName - en 키 있으면 en 이름 반환")
        void getFieldName_EnPresent_ReturnsEn() {
            FieldDefinition field = makeField("ticker", "TEXT", true);
            field.setName(Map.of("en", "Ticker", "ko", "종목코드"));
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{}");

            assertThat(result.errors).anyMatch(e -> e.contains("Ticker"));
        }

        @Test
        @DisplayName("getFieldName - en 없고 ko만 있으면 ko 이름 반환")
        void getFieldName_OnlyKo_ReturnsKo() {
            FieldDefinition field = makeField("ticker", "TEXT", true);
            field.setName(Map.of("ko", "종목코드"));
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{}");

            assertThat(result.errors).anyMatch(e -> e.contains("종목코드"));
        }

        @Test
        @DisplayName("getFieldName - name이 null이면 key 반환")
        void getFieldName_NullName_ReturnsKey() {
            FieldDefinition field = makeField("ticker", "TEXT", true);
            field.setName(null);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{}");

            assertThat(result.errors).anyMatch(e -> e.contains("ticker"));
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // DQ 엔진 통합
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DQ 엔진 통합")
    class EngineIntegration {

        @Test
        @DisplayName("엔진에서 ERROR 발생 시 isValid=false")
        void engineError_ShouldBeInvalid() {
            DqEvaluationResult engineResult = new DqEvaluationResult();
            engineResult.addViolation("email", "REGEX", "ERROR",
                    Map.of("en", "Invalid email format"), "bad-email");
            when(dqRuleEngine.evaluate(eq(nodeId), any(), any(), any(), any())).thenReturn(engineResult);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of());

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"email\":\"bad-email\"}");

            assertThat(result.isValid).isFalse();
            assertThat(result.errors).contains("Invalid email format");
        }

        @Test
        @DisplayName("엔진에서 WARNING만 발생 시 isValid=true + warnings 포함")
        void engineWarning_ShouldBeValidWithWarnings() {
            DqEvaluationResult engineResult = new DqEvaluationResult();
            engineResult.addViolation("nickname", "LENGTH", "WARNING",
                    Map.of("en", "Nickname is too short"), "ab");
            when(dqRuleEngine.evaluate(eq(nodeId), any(), any(), any(), any())).thenReturn(engineResult);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of());

            DataQualityService.DQResult result = dataQualityService.validateData(nodeId, "{\"nickname\":\"ab\"}");

            assertThat(result.isValid).isTrue();
            assertThat(result.warnings).contains("Nickname is too short");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 지정 필드 스코프 DQ 검증 테스트
    // ─────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("지정 필드 스코프 DQ 검증")
    class FieldScopedValidation {

        @Test
        @DisplayName("지정된 필드(targetFieldKeys)에 포함되지 않은 미지정 필수 필드는 DQ 검증에서 제외됨")
        void unassignedRequiredField_IsIgnoredInScopedValidation() {
            FieldDefinition nameField = makeField("name", "TEXT", true);
            FieldDefinition salaryField = makeField("salary", "NUMBER", true); // required in schema
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(nameField, salaryField));

            // Only validate "name" field
            DataQualityService.DQResult result = dataQualityService.validateData(
                    nodeId, "{\"name\":\"Hong Gil Dong\"}", null, List.of("name")
            );

            assertThat(result.isValid).isTrue();
            assertThat(result.errors).isEmpty();
        }

        @Test
        @DisplayName("지정된 필드(targetFieldKeys)의 유효성 검사 실패 시 에러 감지")
        void assignedField_Invalid_DetectedInScopedValidation() {
            FieldDefinition nameField = makeField("name", "TEXT", true);
            FieldDefinition salaryField = makeField("salary", "NUMBER", true);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(nameField, salaryField));

            // Validate "name" field when "name" is missing
            DataQualityService.DQResult result = dataQualityService.validateData(
                    nodeId, "{\"salary\": 5000}", null, List.of("name")
            );

            assertThat(result.isValid).isFalse();
            assertThat(result.errors).anyMatch(e -> e.contains("name") || e.contains("required"));
        }

        @Test
        @DisplayName("DqRuleEngine에 targetFieldKeys가 올바르게 전달되어 비대상 필드의 엔진 에러가 발생하지 않음")
        void engineReceivesTargetFieldKeys_NonTargetFieldViolationsExcluded() {
            FieldDefinition nameField = makeField("name", "TEXT", false);
            FieldDefinition emailField = makeField("email", "TEXT", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(nameField, emailField));

            // Engine mock: when called with targetFieldKeys=["name"], return no violations
            // (since engine filters at field level, "email" violations should not appear)
            when(dqRuleEngine.evaluate(eq(nodeId), any(), any(), any(), eq(List.of("name"))))
                    .thenReturn(new DqEvaluationResult());

            DataQualityService.DQResult result = dataQualityService.validateData(
                    nodeId, "{\"name\":\"test\",\"email\":\"invalid\"}", null, List.of("name")
            );

            assertThat(result.isValid).isTrue();
            assertThat(result.errors).isEmpty();
            // Verify engine was called WITH the targetFieldKeys
            verify(dqRuleEngine).evaluate(eq(nodeId), any(), any(), any(), eq(List.of("name")));
        }

        @Test
        @DisplayName("targetFieldKeys가 null이면 DqRuleEngine에 null이 전달되어 전체 필드를 검사함")
        void nullTargetFieldKeys_EngineChecksAllFields() {
            FieldDefinition nameField = makeField("name", "TEXT", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(nameField));

            // Engine returns violation for all fields
            DqEvaluationResult engineResult = new DqEvaluationResult();
            engineResult.addViolation("name", "NOT_NULL", "ERROR",
                    Map.of("en", "Name is required"), "null");
            when(dqRuleEngine.evaluate(eq(nodeId), any(), any(), any(), isNull()))
                    .thenReturn(engineResult);

            DataQualityService.DQResult result = dataQualityService.validateData(
                    nodeId, "{}", null, null
            );

            assertThat(result.isValid).isFalse();
            assertThat(result.errors).anyMatch(e -> e.contains("Name is required"));
            // Verify engine was called with null targetFieldKeys
            verify(dqRuleEngine).evaluate(eq(nodeId), any(), any(), any(), isNull());
        }

        @Test
        @DisplayName("엔진이 비대상 필드 violation을 반환해도 사후 필터링으로 제외됨 (이중 안전장치)")
        void engineViolationForNonTargetField_FilteredByPostProcessing() {
            FieldDefinition nameField = makeField("name", "TEXT", false);
            FieldDefinition emailField = makeField("email", "TEXT", false);
            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(nameField, emailField));

            // Simulate engine returning violation for BOTH fields (hypothetical bypass)
            DqEvaluationResult engineResult = new DqEvaluationResult();
            engineResult.addViolation("email", "REGEX", "ERROR",
                    Map.of("en", "Invalid email format"), "bad");
            when(dqRuleEngine.evaluate(eq(nodeId), any(), any(), any(), eq(List.of("name"))))
                    .thenReturn(engineResult);

            DataQualityService.DQResult result = dataQualityService.validateData(
                    nodeId, "{\"name\":\"test\",\"email\":\"bad\"}", null, List.of("name")
            );

            // "email" violation should be filtered out by post-processing
            assertThat(result.isValid).isTrue();
            assertThat(result.errors).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // 헬퍼 메서드
    // ─────────────────────────────────────────────────────────────────
    private FieldDefinition makeField(String key, String type, boolean required) {
        FieldDefinition f = new FieldDefinition();
        f.setId(UUID.randomUUID());
        f.setKey(key);
        f.setType(type);
        f.setRequired(required);
        f.setName(Map.of("en", key));
        return f;
    }
}

