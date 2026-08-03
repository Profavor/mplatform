package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataMaskingServiceTest {

    @Mock
    private FieldEncryptionService fieldEncryptionService;

    private DataMaskingService dataMaskingService;

    @BeforeEach
    void setUp() {
        dataMaskingService = new DataMaskingService(fieldEncryptionService);
    }

    @Test
    @DisplayName("maskChangesJson - before/after 구조의 JSON 암호화 필드를 마스킹 처리한다")
    void maskChangesJson_BeforeAfterStructure() {
        FieldDefinition field = new FieldDefinition();
        field.setKey("rrn");
        field.setIsEncrypted(true);
        field.setMaskingPattern("RRN");

        when(fieldEncryptionService.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        String changesJson = "{\"before\":{\"rrn\":\"900101-1234567\"},\"after\":{\"rrn\":\"900101-7654321\"}}";

        String masked = dataMaskingService.maskChangesJson(changesJson, List.of(field), false);

        assertThat(masked).contains("900101-1******");
        assertThat(masked).contains("900101-7******");
    }

    @Test
    @DisplayName("maskChangesJson - 암호문을 평문으로 먼저 복호화 진행 후 지정된 마스킹 평문으로 변형한다")
    void maskChangesJson_EncryptedDataDecryptedAndMaskedPlaintext() {
        FieldDefinition field = new FieldDefinition();
        field.setKey("rrn");
        field.setIsEncrypted(true);
        field.setMaskingPattern("RRN");

        String cipherText = "Ot3aB9c123456789012345678901234567890=";
        when(fieldEncryptionService.decrypt(cipherText)).thenReturn("900101-1234567");

        String changesJson = "{\"after\":{\"rrn\":\"" + cipherText + "\"}}";

        String masked = dataMaskingService.maskChangesJson(changesJson, List.of(field), false);

        assertThat(masked).contains("900101-1******");
        assertThat(masked).doesNotContain(cipherText);
    }

    @Test
    @DisplayName("maskChangesJson - 복호화 실패로 여전히 암호문인 경우 마스킹하지 않고 암호문 원본을 유지한다")
    void maskChangesJson_FailedDecryptionKeepsOriginalCiphertext() {
        FieldDefinition field = new FieldDefinition();
        field.setKey("rrn");
        field.setIsEncrypted(true);
        field.setMaskingPattern("RRN");

        String cipherText = "Ot3aB9c123456789012345678901234567890=";
        when(fieldEncryptionService.decrypt(cipherText)).thenReturn(cipherText);
        when(fieldEncryptionService.isEncrypted(cipherText)).thenReturn(true);

        String changesJson = "{\"after\":{\"rrn\":\"" + cipherText + "\"}}";

        String masked = dataMaskingService.maskChangesJson(changesJson, List.of(field), false);

        assertThat(masked).contains(cipherText);
        assertThat(masked).doesNotContain("Ot****************************************");
    }

    @Test
    @DisplayName("maskChangesJson - canUnmask=true 시 복호화된 원본 데이터를 반환한다")
    void maskChangesJson_CanUnmaskTrue() {
        FieldDefinition field = new FieldDefinition();
        field.setKey("rrn");
        field.setIsEncrypted(true);

        when(fieldEncryptionService.decrypt("ENC_DATA")).thenReturn("900101-1234567");

        String changesJson = "{\"before\":{\"rrn\":\"ENC_DATA\"}}";

        String unmasked = dataMaskingService.maskChangesJson(changesJson, List.of(field), true);

        assertThat(unmasked).contains("900101-1234567");
    }

    @Test
    @DisplayName("maskChangesJson - maskingPattern이 GENERIC이거나 미지정이어도 데이터가 주민등록번호 형식일 경우 maskRrn 패턴으로 가린다")
    void maskChangesJson_GenericPatternWithRrnFormat_MasksAsRrn() {
        FieldDefinition field = new FieldDefinition();
        field.setKey("jumin");
        field.setIsEncrypted(true);
        field.setMaskingPattern("GENERIC");

        when(fieldEncryptionService.decrypt("ENC_JUMIN")).thenReturn("860104-1234567");

        String changesJson = "{\"after\":{\"jumin\":\"ENC_JUMIN\"}}";

        String masked = dataMaskingService.maskChangesJson(changesJson, List.of(field), false);

        assertThat(masked).contains("860104-1******");
        assertThat(masked).doesNotContain("86*************");
    }
}
