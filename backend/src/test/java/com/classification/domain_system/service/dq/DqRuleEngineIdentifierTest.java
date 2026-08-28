package com.classification.domain_system.service.dq;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.dq.evaluators.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DqRuleEngineIdentifierTest {

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @Mock
    private DqRuleRepository dqRuleRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private DqViolationRepository violationRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private FieldDefinitionRepository fieldDefinitionRepository;

    private DqRuleEngine dqRuleEngine;
    private RegexEvaluator regexEvaluator;
    private LengthEvaluator lengthEvaluator;
    private BusinessNoChecksumEvaluator businessNoChecksumEvaluator;
    private CorporateNoChecksumEvaluator corporateNoChecksumEvaluator;

    @BeforeEach
    void setUp() {
        regexEvaluator = new RegexEvaluator();
        lengthEvaluator = new LengthEvaluator();
        businessNoChecksumEvaluator = new BusinessNoChecksumEvaluator();
        corporateNoChecksumEvaluator = new CorporateNoChecksumEvaluator();

        List<RuleEvaluator> evaluators = List.of(
                regexEvaluator,
                lengthEvaluator,
                businessNoChecksumEvaluator,
                corporateNoChecksumEvaluator
        );

        dqRuleEngine = new DqRuleEngine(
                evaluators,
                fieldDefinitionService,
                dqRuleRepository,
                nodeRepository,
                violationRepository,
                recordRepository,
                fieldDefinitionRepository
        );
    }

    @Nested
    @DisplayName("Record Identifier Extraction")
    class ExtractRecordIdentifierTests {

        @Test
        @DisplayName("HTML 태그가 포함된 name 필드가 있더라도 도메인 식별자 필드(customer_no)가 우선 추출되어야 한다")
        void prioritizesDomainIdentifierFieldOverHtmlName() {
            UUID domainId = UUID.randomUUID();
            UUID idFieldId = UUID.randomUUID();
            UUID recordId = UUID.randomUUID();

            Domain domain = new Domain();
            domain.setId(domainId);
            domain.setIdentifierFieldId(idFieldId);

            ClassificationNode node = new ClassificationNode();
            node.setId(UUID.randomUUID());
            node.setDomain(domain);

            FieldDefinition idFd = new FieldDefinition();
            idFd.setId(idFieldId);
            idFd.setKey("customer_no");

            Record record = new Record();
            record.setId(recordId);
            record.setNode(node);
            record.setData("{\"name\":\"<p>니가 나를 모르는데</p>\",\"customer_no\":\"CUST-2026-0001\"}");

            DqViolation violation = new DqViolation();
            violation.setId(UUID.randomUUID());
            violation.setRecordId(recordId);
            violation.setFieldKey("CONTACT_EMAIL");
            violation.setSeverity("WARNING");
            violation.setMessage(Map.of("ko", "유효한 이메일 형식이 아닙니다."));

            given(violationRepository.findViolationsByDomainId(eq(domainId), any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of(violation)));
            given(recordRepository.findById(recordId)).willReturn(Optional.of(record));
            given(fieldDefinitionRepository.findById(idFieldId)).willReturn(Optional.of(idFd));

            var result = dqRuleEngine.getDomainDqViolations(domainId, null, null, PageRequest.of(0, 10));

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getRecordIdentifier()).isEqualTo("CUST-2026-0001");
            assertThat(result.content().get(0).getRecordIdentifier()).doesNotContain("<p>");
        }

        @Test
        @DisplayName("식별자 필드가 없고 본문 텍스트만 있을 때 REC-xxxxxxxx 포맷의 식별 코드가 반환되어야 한다")
        void fallsBackToRecCodeWhenNoIdentifierFieldExists() {
            UUID domainId = UUID.randomUUID();
            UUID recordId = UUID.fromString("340a0917-af0b-4d13-a1ce-479d4b2e2ca7");

            Record record = new Record();
            record.setId(recordId);
            record.setData("{\"name\":\"<p>니가 나를 모르는데</p>\",\"detail\":\"상세내용\"}");

            DqViolation violation = new DqViolation();
            violation.setId(UUID.randomUUID());
            violation.setRecordId(recordId);
            violation.setFieldKey("CONTACT_EMAIL");
            violation.setSeverity("WARNING");

            given(violationRepository.findViolationsByDomainId(eq(domainId), any(), any(), any()))
                    .willReturn(new PageImpl<>(List.of(violation)));
            given(recordRepository.findById(recordId)).willReturn(Optional.of(record));

            var result = dqRuleEngine.getDomainDqViolations(domainId, null, null, PageRequest.of(0, 10));

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getRecordIdentifier()).isEqualTo("REC-340A0917");
            assertThat(result.content().get(0).getRecordIdentifier()).doesNotContain("<p>");
        }
    }

    @Nested
    @DisplayName("Masked / Encrypted Value DQ Evaluation")
    class MaskedValueEvaluationTests {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        @DisplayName("RegexEvaluator: 마스킹된 이메일(h***@mplatform.com)은 정규식 검증에서 스킵되어 위반이 발생하지 않아야 한다")
        void regexEvaluatorSkipsMaskedValues() {
            FieldDefinition field = new FieldDefinition();
            field.setKey("contact_email");
            field.setType("EMAIL");

            DqRule rule = new DqRule();
            rule.setRuleType(DqRuleType.REGEX);
            rule.setParams("{\"pattern\":\"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$\"}");

            EvaluationContext context = new EvaluationContext(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID());

            Optional<String> result = regexEvaluator.evaluate(
                    field,
                    rule,
                    objectMapper.valueToTree("h***@mplatform.com"),
                    context
            );

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("LengthEvaluator: 마스킹된 값(123-**-****)은 길이 검증에서 스킵되어야 한다")
        void lengthEvaluatorSkipsMaskedValues() {
            FieldDefinition field = new FieldDefinition();
            field.setKey("phone_no");

            DqRule rule = new DqRule();
            rule.setRuleType(DqRuleType.LENGTH);
            rule.setParams("{\"minLength\":13,\"maxLength\":13}");

            EvaluationContext context = new EvaluationContext(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID());

            Optional<String> result = lengthEvaluator.evaluate(
                    field,
                    rule,
                    objectMapper.valueToTree("010-****-5678"),
                    context
            );

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("BusinessNoChecksumEvaluator: 마스킹된 사업자번호(123-**-*****)는 체크섬 검증에서 스킵되어야 한다")
        void businessNoEvaluatorSkipsMaskedValues() {
            FieldDefinition field = new FieldDefinition();
            field.setKey("biz_no");

            DqRule rule = new DqRule();
            rule.setRuleType(DqRuleType.BUSINESS_NO_CHECKSUM);

            EvaluationContext context = new EvaluationContext(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID());

            Optional<String> result = businessNoChecksumEvaluator.evaluate(
                    field,
                    rule,
                    objectMapper.valueToTree("123-**-*****"),
                    context
            );

            assertThat(result).isEmpty();
        }
    }
}
