package com.classification.domain_system.service.dq;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.dq.evaluators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DqRuleEngineTest {

    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private DqRuleRepository dqRuleRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private DqViolationRepository violationRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private Environment environment;

    private DqRuleEngine engine;
    private UUID nodeId;
    private UUID domainId;
    private UUID fieldId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        domainId = UUID.randomUUID();
        fieldId = UUID.randomUUID();

        // Build evaluators list
        List<RuleEvaluator> evaluators = List.of(
                new NotNullEvaluator(),
                new RegexEvaluator(),
                new RangeEvaluator(),
                new LengthEvaluator(),
                new EnumEvaluator(),
                new DateRangeEvaluator(),
                new UniqueEvaluator(jdbcTemplate, environment),
                new CrossFieldEvaluator(),
                new SpelEvaluator()
        );

        engine = new DqRuleEngine(evaluators, fieldDefinitionService, dqRuleRepository,
                nodeRepository, violationRepository, recordRepository);

        // Default mock: node lookup returns a node with domain
        ClassificationNode node = new ClassificationNode();
        Domain domain = new Domain();
        domain.setId(domainId);
        node.setDomain(domain);
        lenient().when(nodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
    }

    private FieldDefinition makeField(String key, String type) {
        FieldDefinition f = new FieldDefinition();
        f.setId(fieldId);
        f.setKey(key);
        f.setType(type);
        f.setRequired(false);
        f.setName(Map.of("en", key));
        return f;
    }

    private DqRule makeRule(DqRuleType type, DqSeverity severity, String params) {
        DqRule rule = new DqRule();
        rule.setId(UUID.randomUUID());
        rule.setFieldDefinition(makeField("test", "TEXT")); // will be overridden
        rule.setRuleType(type);
        rule.setSeverity(severity);
        rule.setParams(params);
        rule.setIsActive(true);
        rule.setSortOrder(0);
        return rule;
    }

    private void setupFieldAndRule(FieldDefinition field, DqRule rule) {
        rule.setFieldDefinition(field);
        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
        when(dqRuleRepository.findByFieldDefinition_IdInAndIsActiveTrueOrderBySortOrderAsc(List.of(field.getId())))
                .thenReturn(List.of(rule));
    }

    // ─── NOT_NULL Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("NOT_NULL 규칙 평가")
    class NotNullTests {
        @Test
        @DisplayName("null 값이면 ERROR 위반 발생")
        void nullValue_ShouldViolate() {
            FieldDefinition field = makeField("name", "TEXT");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"name\":null}");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).hasSize(1);
            assertThat(result.getViolations().get(0).getRuleType()).isEqualTo("NOT_NULL");
        }

        @Test
        @DisplayName("빈 문자열이면 ERROR 위반 발생")
        void emptyString_ShouldViolate() {
            FieldDefinition field = makeField("name", "TEXT");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"name\":\"   \"}");

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("정상 값이면 통과")
        void validValue_ShouldPass() {
            FieldDefinition field = makeField("name", "TEXT");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"name\":\"홍길동\"}");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getViolations()).isEmpty();
        }

        @Test
        @DisplayName("WARNING 심각도 규칙은 isValid=true 유지")
        void warningNotNull_ShouldBeValidButHaveWarnings() {
            FieldDefinition field = makeField("nickname", "TEXT");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.WARNING, null);
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"nickname\":null}");

            assertThat(result.isValid()).isTrue(); // WARNING은 차단하지 않음
            assertThat(result.hasWarnings()).isTrue();
            assertThat(result.getViolations()).hasSize(1);
            assertThat(result.getViolations().get(0).getSeverity()).isEqualTo("WARNING");
        }
    }

    // ─── REGEX Tests ─────────────────────────────────────────────────

    @Nested
    @DisplayName("REGEX 규칙 평가")
    class RegexTests {
        @Test
        @DisplayName("패턴 매칭 실패 시 위반")
        void patternMismatch_ShouldViolate() {
            FieldDefinition field = makeField("code", "TEXT");
            DqRule rule = makeRule(DqRuleType.REGEX, DqSeverity.ERROR,
                    "{\"pattern\": \"^[A-Z]{2}\\\\d{4}$\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"code\":\"abc\"}");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations().get(0).getRuleType()).isEqualTo("REGEX");
        }

        @Test
        @DisplayName("패턴 매칭 성공 시 통과")
        void patternMatch_ShouldPass() {
            FieldDefinition field = makeField("code", "TEXT");
            DqRule rule = makeRule(DqRuleType.REGEX, DqSeverity.ERROR,
                    "{\"pattern\": \"^[A-Z]{2}\\\\d{4}$\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"code\":\"AB1234\"}");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("null 값은 REGEX 검증 스킵")
        void nullValue_ShouldSkip() {
            FieldDefinition field = makeField("code", "TEXT");
            DqRule rule = makeRule(DqRuleType.REGEX, DqSeverity.ERROR,
                    "{\"pattern\": \"^[A-Z]+$\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"code\":null}");

            assertThat(result.isValid()).isTrue();
        }
    }

    // ─── RANGE Tests ─────────────────────────────────────────────────

    @Nested
    @DisplayName("RANGE 규칙 평가")
    class RangeTests {
        @Test
        @DisplayName("범위 이내 값은 통과")
        void inRange_ShouldPass() {
            FieldDefinition field = makeField("price", "NUMBER");
            DqRule rule = makeRule(DqRuleType.RANGE, DqSeverity.ERROR,
                    "{\"min\": 0, \"max\": 1000}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"price\":500}");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("최소값 미만 시 위반")
        void belowMin_ShouldViolate() {
            FieldDefinition field = makeField("price", "NUMBER");
            DqRule rule = makeRule(DqRuleType.RANGE, DqSeverity.ERROR,
                    "{\"min\": 0, \"max\": 1000}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"price\":-5}");

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("최대값 초과 시 위반")
        void aboveMax_ShouldViolate() {
            FieldDefinition field = makeField("price", "NUMBER");
            DqRule rule = makeRule(DqRuleType.RANGE, DqSeverity.ERROR,
                    "{\"min\": 0, \"max\": 1000}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"price\":1500}");

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("비숫자 값이면 위반")
        void nonNumeric_ShouldViolate() {
            FieldDefinition field = makeField("price", "NUMBER");
            DqRule rule = makeRule(DqRuleType.RANGE, DqSeverity.ERROR,
                    "{\"min\": 0, \"max\": 1000}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"price\":\"abc\"}");

            assertThat(result.isValid()).isFalse();
        }
    }

    // ─── LENGTH Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("LENGTH 규칙 평가")
    class LengthTests {
        @Test
        @DisplayName("길이 범위 내 통과")
        void withinLength_ShouldPass() {
            FieldDefinition field = makeField("username", "TEXT");
            DqRule rule = makeRule(DqRuleType.LENGTH, DqSeverity.ERROR,
                    "{\"minLength\": 3, \"maxLength\": 20}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"username\":\"john\"}");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("최소 길이 미달 시 위반")
        void tooShort_ShouldViolate() {
            FieldDefinition field = makeField("username", "TEXT");
            DqRule rule = makeRule(DqRuleType.LENGTH, DqSeverity.ERROR,
                    "{\"minLength\": 3, \"maxLength\": 20}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"username\":\"ab\"}");

            assertThat(result.isValid()).isFalse();
        }
    }

    // ─── ENUM Tests ──────────────────────────────────────────────────

    @Nested
    @DisplayName("ENUM 규칙 평가")
    class EnumTests {
        @Test
        @DisplayName("허용 값 목록에 있으면 통과")
        void allowedValue_ShouldPass() {
            FieldDefinition field = makeField("country", "TEXT");
            DqRule rule = makeRule(DqRuleType.ENUM, DqSeverity.ERROR,
                    "{\"values\": [\"KR\",\"US\",\"JP\"]}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"country\":\"KR\"}");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("허용 값 목록에 없으면 위반")
        void disallowedValue_ShouldViolate() {
            FieldDefinition field = makeField("country", "TEXT");
            DqRule rule = makeRule(DqRuleType.ENUM, DqSeverity.ERROR,
                    "{\"values\": [\"KR\",\"US\",\"JP\"]}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"country\":\"CN\"}");

            assertThat(result.isValid()).isFalse();
        }
    }

    // ─── DATE_RANGE Tests ────────────────────────────────────────────

    @Nested
    @DisplayName("DATE_RANGE 규칙 평가")
    class DateRangeTests {
        @Test
        @DisplayName("범위 내 날짜 통과")
        void inRange_ShouldPass() {
            FieldDefinition field = makeField("birthDate", "DATE");
            DqRule rule = makeRule(DqRuleType.DATE_RANGE, DqSeverity.ERROR,
                    "{\"after\": \"1900-01-01\", \"before\": \"2025-12-31\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"birthDate\":\"1990-05-15\"}");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("범위 이전 날짜 위반")
        void beforeRange_ShouldViolate() {
            FieldDefinition field = makeField("birthDate", "DATE");
            DqRule rule = makeRule(DqRuleType.DATE_RANGE, DqSeverity.ERROR,
                    "{\"after\": \"2000-01-01\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"birthDate\":\"1999-12-31\"}");

            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("잘못된 날짜 형식 위반")
        void invalidFormat_ShouldViolate() {
            FieldDefinition field = makeField("birthDate", "DATE");
            DqRule rule = makeRule(DqRuleType.DATE_RANGE, DqSeverity.ERROR,
                    "{\"after\": \"2000-01-01\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"birthDate\":\"not-a-date\"}");

            assertThat(result.isValid()).isFalse();
        }
    }

    // ─── CROSS_FIELD Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("CROSS_FIELD 규칙 평가")
    class CrossFieldTests {
        @Test
        @DisplayName("startDate <= endDate 통과")
        void startBeforeEnd_ShouldPass() {
            FieldDefinition field = makeField("endDate", "DATE");
            DqRule rule = makeRule(DqRuleType.CROSS_FIELD, DqSeverity.ERROR,
                    "{\"targetFieldKey\": \"startDate\", \"operator\": \">=\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId,
                    "{\"startDate\":\"2024-01-01\", \"endDate\":\"2024-12-31\"}");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("endDate < startDate 위반")
        void endBeforeStart_ShouldViolate() {
            FieldDefinition field = makeField("endDate", "DATE");
            DqRule rule = makeRule(DqRuleType.CROSS_FIELD, DqSeverity.ERROR,
                    "{\"targetFieldKey\": \"startDate\", \"operator\": \">=\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId,
                    "{\"startDate\":\"2024-12-31\", \"endDate\":\"2024-01-01\"}");

            assertThat(result.isValid()).isFalse();
        }
    }

    // ─── CUSTOM_SPEL Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("CUSTOM_SPEL 규칙 평가")
    class SpelTests {
        @Test
        @DisplayName("SpEL 표현식 통과")
        void spelPass_ShouldPass() {
            FieldDefinition field = makeField("email", "TEXT");
            DqRule rule = makeRule(DqRuleType.CUSTOM_SPEL, DqSeverity.ERROR,
                    "{\"expression\": \"#value.contains('@')\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"email\":\"test@test.com\"}");

            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("SpEL 표현식 실패 시 위반")
        void spelFail_ShouldViolate() {
            FieldDefinition field = makeField("email", "TEXT");
            DqRule rule = makeRule(DqRuleType.CUSTOM_SPEL, DqSeverity.ERROR,
                    "{\"expression\": \"#value.contains('@')\"}");
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"email\":\"invalid-email\"}");

            assertThat(result.isValid()).isFalse();
        }
    }

    // ─── JSON Parse Error ────────────────────────────────────────────

    @Nested
    @DisplayName("JSON 파싱 에러")
    class JsonParseTests {
        @Test
        @DisplayName("잘못된 JSON 형식이면 위반")
        void invalidJson_ShouldViolate() {
            // No mock needed — engine returns before calling getEffectiveFields

            DqEvaluationResult result = engine.evaluate(nodeId, "NOT_JSON");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).hasSize(1);
            assertThat(result.getViolations().get(0).getFieldKey()).isEqualTo("_json");
        }
    }

    // ─── Multiple Rules per Field ────────────────────────────────────

    @Nested
    @DisplayName("필드당 복수 규칙")
    class MultipleRulesTests {
        @Test
        @DisplayName("여러 규칙 중 일부만 위반 시 해당 위반만 기록")
        void multipleRules_PartialViolation() {
            FieldDefinition field = makeField("code", "TEXT");
            DqRule notNullRule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            notNullRule.setFieldDefinition(field);
            DqRule regexRule = makeRule(DqRuleType.REGEX, DqSeverity.ERROR,
                    "{\"pattern\": \"^[A-Z]+$\"}");
            regexRule.setFieldDefinition(field);

            when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
            when(dqRuleRepository.findByFieldDefinition_IdInAndIsActiveTrueOrderBySortOrderAsc(List.of(field.getId())))
                    .thenReturn(List.of(notNullRule, regexRule));

            // "abc" passes NOT_NULL but fails REGEX (requires uppercase)
            DqEvaluationResult result = engine.evaluate(nodeId, "{\"code\":\"abc\"}");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).hasSize(1);
            assertThat(result.getViolations().get(0).getRuleType()).isEqualTo("REGEX");
        }
    }

    // ─── Custom Message Override ─────────────────────────────────────

    @Nested
    @DisplayName("커스텀 메시지 오버라이드")
    class CustomMessageTests {
        @Test
        @DisplayName("규칙에 커스텀 메시지가 있으면 해당 메시지 사용")
        void customMessage_ShouldOverride() {
            FieldDefinition field = makeField("name", "TEXT");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            rule.setMessage(Map.of("en", "Name is mandatory", "ko", "이름은 필수입니다"));
            setupFieldAndRule(field, rule);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"name\":null}");

            assertThat(result.getViolations().get(0).getMessage().get("en")).isEqualTo("Name is mandatory");
            assertThat(result.getViolations().get(0).getMessage().get("ko")).isEqualTo("이름은 필수입니다");
        }
    }

    // ─── Conditional Field Control & DQ Exclusion Tests ────────────────────────

    @Nested
    @DisplayName("조건부 필드 연동 및 DQ 검증 제외 테스트")
    class ConditionalRuleTests {
        @Test
        @DisplayName("SHOW 조건 미충족으로 숨겨진 필드는 DQ 검증 스킵")
        void hiddenField_ShouldSkipDq() {
            FieldDefinition field = makeField("kospiNote", "TEXT");
            field.setOptions("{\"conditionRule\":{\"enabled\":true,\"action\":\"SHOW\",\"dependsOnFieldKey\":\"market\",\"operator\":\"EQUALS\",\"value\":\"KOSPI\"}}");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            // When market is KOSDAQ, kospiNote is hidden -> DQ NOT_NULL should be SKIPPED!
            DqEvaluationResult result = engine.evaluate(nodeId, "{\"market\":\"KOSDAQ\",\"kospiNote\":null}");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getViolations()).isEmpty();
        }

        @Test
        @DisplayName("SHOW 조건 충족으로 노출된 필드는 DQ 검증 수행")
        void visibleField_ShouldExecuteDq() {
            FieldDefinition field = makeField("kospiNote", "TEXT");
            field.setOptions("{\"conditionRule\":{\"enabled\":true,\"action\":\"SHOW\",\"dependsOnFieldKey\":\"market\",\"operator\":\"EQUALS\",\"value\":\"KOSPI\"}}");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            // When market is KOSPI, kospiNote is visible -> DQ NOT_NULL should FAIL!
            DqEvaluationResult result = engine.evaluate(nodeId, "{\"market\":\"KOSPI\",\"kospiNote\":null}");

            assertThat(result.isValid()).isFalse();
            assertThat(result.getViolations()).hasSize(1);
        }

        @Test
        @DisplayName("READ_ONLY 또는 DISABLE 조건 충족 필드는 DQ 검증 스킵")
        void readOnlyOrDisableField_ShouldSkipDq() {
            FieldDefinition field = makeField("readOnlyCode", "TEXT");
            field.setOptions("{\"conditionRule\":{\"enabled\":true,\"action\":\"READ_ONLY\",\"expression\":\"#{market} == 'KOSPI'\"}}");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            // When market is KOSPI, readOnlyCode is READ_ONLY -> DQ NOT_NULL should be SKIPPED!
            DqEvaluationResult result = engine.evaluate(nodeId, "{\"market\":\"KOSPI\",\"readOnlyCode\":null}");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getViolations()).isEmpty();
        }

        @Test
        @DisplayName("다중 제어 동작(SHOW + HIGHLIGHT + READ_ONLY) 배열 파싱 및 DQ 검증 제외")
        void multiActionField_ShouldHandleArrayActions() {
            FieldDefinition field = makeField("multiNote", "TEXT");
            field.setOptions("{\"conditionRule\":{\"enabled\":true,\"action\":[\"SHOW\",\"HIGHLIGHT\",\"READ_ONLY\"],\"expression\":\"#{market} == 'KOSPI'\"}}");
            DqRule rule = makeRule(DqRuleType.NOT_NULL, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            // When market is KOSDAQ, SHOW is false -> hidden -> SKIP DQ
            DqEvaluationResult result1 = engine.evaluate(nodeId, "{\"market\":\"KOSDAQ\",\"multiNote\":null}");
            assertThat(result1.isValid()).isTrue();

            // When market is KOSPI, READ_ONLY is true -> read-only -> SKIP DQ
            DqEvaluationResult result2 = engine.evaluate(nodeId, "{\"market\":\"KOSPI\",\"multiNote\":null}");
            assertThat(result2.isValid()).isTrue();
        }
    }

    // ─── UNIQUE with recordId Tests ───────────────────────────────────

    @Nested
    @DisplayName("UNIQUE 규칙 평가 (recordId 연동 테스트)")
    class UniqueWithRecordIdTests {
        @Test
        @DisplayName("레코드 수정 시 recordId가 전달되면 자기 자신 레코드는 중복 검사 대상에서 제외한다")
        void evaluateWithRecordId_ExcludesSelfFromUniquenessCheck() {
            UUID currentRecordId = UUID.randomUUID();
            FieldDefinition field = makeField("emp_id", "TEXT");
            DqRule rule = makeRule(DqRuleType.UNIQUE, DqSeverity.ERROR, null);
            setupFieldAndRule(field, rule);

            when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), any(), any(), eq(currentRecordId)))
                    .thenReturn(0);

            DqEvaluationResult result = engine.evaluate(nodeId, "{\"emp_id\":\"TEST\"}", currentRecordId);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getViolations()).isEmpty();
        }
    }
}
