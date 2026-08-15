package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculatedFieldEvaluatorTest {

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @InjectMocks
    private CalculatedFieldEvaluator evaluator;

    private UUID nodeId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("evaluateFormula - 사칙연산 및 수학함수 평가")
    void evaluateFormula_MathFunctions() {
        Map<String, Object> data = Map.of("PER", 10.5, "PBR", 2);

        Double result = evaluator.evaluateFormula("${PER} * ${PBR}", data);
        assertThat(result).isEqualTo(21.0);

        Double roundResult = evaluator.evaluateFormula("ROUND(${PER} * ${PBR}, 0)", data);
        assertThat(roundResult).isEqualTo(21.0);

        Double ceilResult = evaluator.evaluateFormula("CEIL(10.1)", data);
        assertThat(ceilResult).isEqualTo(11.0);
    }

    @Test
    @DisplayName("recomputeCalculatedFields - CALCULATED 필드가 포함된 JSON재계산")
    void recomputeCalculatedFields_Success() {
        FieldDefinition field = new FieldDefinition();
        field.setType("CALCULATED");
        field.setKey("TARGET");
        field.setOptions("{\"formula\":\"${VAL} * 2\"}");

        when(fieldDefinitionService.getEffectiveFields(eq(nodeId))).thenReturn(List.of(field));

        String inputJson = "{\"VAL\": 15}";
        String outputJson = evaluator.recomputeCalculatedFields(nodeId, inputJson);

        assertThat(outputJson).contains("\"TARGET\":30");
    }

    @Test
    @DisplayName("recomputeCalculatedFields - DAG 위상 정렬을 통해 정의 순서와 무관하게 다단계 계산 필드를 올바르게 계산한다")
    void recomputeCalculatedFields_TopologicalOrder() {
        // C depends on B, B depends on A (역순 정의: C, B)
        FieldDefinition fieldC = new FieldDefinition();
        fieldC.setType("CALCULATED");
        fieldC.setKey("TOTAL");
        fieldC.setOptions("{\"formula\":\"${SUBTOTAL} + 5000\"}");

        FieldDefinition fieldB = new FieldDefinition();
        fieldB.setType("CALCULATED");
        fieldB.setKey("SUBTOTAL");
        fieldB.setOptions("{\"formula\":\"${UNIT_PRICE} * ${QTY}\"}");

        when(fieldDefinitionService.getEffectiveFields(eq(nodeId))).thenReturn(List.of(fieldC, fieldB));

        String inputJson = "{\"UNIT_PRICE\": 1000, \"QTY\": 3}";
        String outputJson = evaluator.recomputeCalculatedFields(nodeId, inputJson);

        assertThat(outputJson).contains("\"SUBTOTAL\":3000");
        assertThat(outputJson).contains("\"TOTAL\":8000");
    }

    @Test
    @DisplayName("recomputeCalculatedFields - 순환 참조(A -> B -> A) 감지 시 무한 루프 없이 안전하게 처리한다")
    void recomputeCalculatedFields_CircularDependencyProtection() {
        FieldDefinition fieldA = new FieldDefinition();
        fieldA.setType("CALCULATED");
        fieldA.setKey("FIELD_A");
        fieldA.setOptions("{\"formula\":\"${FIELD_B} + 10\"}");

        FieldDefinition fieldB = new FieldDefinition();
        fieldB.setType("CALCULATED");
        fieldB.setKey("FIELD_B");
        fieldB.setOptions("{\"formula\":\"${FIELD_A} * 2\"}");

        when(fieldDefinitionService.getEffectiveFields(eq(nodeId))).thenReturn(List.of(fieldA, fieldB));

        String inputJson = "{\"BASE\": 100}";
        // 순환 참조 시 예외 없이 원래 데이터 또는 안전한 결과 반환
        String outputJson = evaluator.recomputeCalculatedFields(nodeId, inputJson);
        assertThat(outputJson).isNotNull();
    }
}

