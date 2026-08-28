package com.classification.domain_system.service.dq;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.service.DataMaskingService;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.FieldEncryptionService;
import com.classification.domain_system.service.dq.evaluators.RegexEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DqRuleEngineEncryptionDqTest {

    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private DqRuleRepository dqRuleRepository;
    @Mock private ClassificationNodeRepository nodeRepository;
    @Mock private DqViolationRepository violationRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private FieldDefinitionRepository fieldDefinitionRepository;
    @Mock private FieldEncryptionService fieldEncryptionService;
    @Mock private DataMaskingService dataMaskingService;

    private DqRuleEngine dqRuleEngine;
    private UUID nodeId;
    private UUID fieldId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
        fieldId = UUID.randomUUID();

        List<RuleEvaluator> evaluators = List.of(new RegexEvaluator());

        dqRuleEngine = new DqRuleEngine(
                evaluators,
                fieldDefinitionService,
                dqRuleRepository,
                nodeRepository,
                violationRepository,
                recordRepository,
                fieldDefinitionRepository,
                fieldEncryptionService,
                dataMaskingService
        );
    }

    @Test
    @DisplayName("암호화된 이메일 필드는 평문으로 복호화된 후 DQ 검증을 통과해야 한다")
    void evaluatesDecryptedPlaintextForValidEmail() {
        FieldDefinition emailField = new FieldDefinition();
        emailField.setId(fieldId);
        emailField.setKey("contact_email");
        emailField.setType("EMAIL");
        emailField.setIsEncrypted(true);

        given(fieldDefinitionService.getEffectiveFields(nodeId)).willReturn(List.of(emailField));
        given(dqRuleRepository.findByFieldDefinition_IdInAndIsActiveTrueOrderBySortOrderAsc(List.of(fieldId)))
                .willReturn(Collections.emptyList());

        String ciphertext = "vault:v1:sampleEncryptedEmailCiphertext";
        String decryptedPlaintext = "hong@mplatform.com";

        given(fieldEncryptionService.decrypt(ciphertext)).willReturn(decryptedPlaintext);

        String json = "{\"contact_email\":\"" + ciphertext + "\",\"_mask_contact_email\":\"h***@mplatform.com\"}";

        DqEvaluationResult result = dqRuleEngine.evaluate(nodeId, json);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    @DisplayName("암호화된 필드의 복호화 결과가 유효하지 않은 형식일 때 정확하게 DQ 위반으로 탐지되고 actualValue는 마스킹된다")
    void detectsDqViolationOnDecryptedInvalidValueAndMasksActualValue() {
        FieldDefinition emailField = new FieldDefinition();
        emailField.setId(fieldId);
        emailField.setKey("contact_email");
        emailField.setType("EMAIL");
        emailField.setIsEncrypted(true);

        given(fieldDefinitionService.getEffectiveFields(nodeId)).willReturn(List.of(emailField));
        given(dqRuleRepository.findByFieldDefinition_IdInAndIsActiveTrueOrderBySortOrderAsc(List.of(fieldId)))
                .willReturn(Collections.emptyList());

        String ciphertext = "vault:v1:sampleInvalidEmailCiphertext";
        String decryptedPlaintext = "invalid-email-not-valid";

        given(fieldEncryptionService.decrypt(ciphertext)).willReturn(decryptedPlaintext);

        String json = "{\"contact_email\":\"" + ciphertext + "\",\"_mask_contact_email\":\"inva***@mplatform.com\"}";

        DqEvaluationResult result = dqRuleEngine.evaluate(nodeId, json);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getViolations()).hasSize(1);
        assertThat(result.getViolations().get(0).getFieldKey()).isEqualTo("contact_email");
        assertThat(result.getViolations().get(0).getActualValue()).isEqualTo("inva***@mplatform.com");
    }

    @Test
    @DisplayName("복호화된 평문 이메일(tax@mplatform.com)은 유효한 이메일 형식으로 DQ 검증을 통과한다")
    void allowsValidPlaintextEmailFormatAfterDecryption() {
        FieldDefinition emailField = new FieldDefinition();
        emailField.setId(fieldId);
        emailField.setKey("tax_invoice_email");
        emailField.setType("EMAIL");
        emailField.setIsEncrypted(true);

        given(fieldDefinitionService.getEffectiveFields(nodeId)).willReturn(List.of(emailField));
        given(dqRuleRepository.findByFieldDefinition_IdInAndIsActiveTrueOrderBySortOrderAsc(List.of(fieldId)))
                .willReturn(Collections.emptyList());

        String ciphertext = "vault:v1:sampleTaxInvoiceEmail";
        String plaintext = "tax@mplatform.com";
        given(fieldEncryptionService.isEncrypted(ciphertext)).willReturn(true);
        given(fieldEncryptionService.decrypt(ciphertext)).willReturn(plaintext);

        String json = "{\"tax_invoice_email\":\"" + ciphertext + "\",\"_mask_tax_invoice_email\":\"t***@mplatform.com\"}";

        DqEvaluationResult result = dqRuleEngine.evaluate(nodeId, json);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getViolations()).isEmpty();
    }
}
