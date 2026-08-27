package com.classification.domain_system.service;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceEncryptionPreserveTest {

    @Mock private RecordRepository recordRepository;
    @Mock private com.classification.domain_system.repository.RecordHistoryRepository recordHistoryRepository;
    @Mock private com.classification.domain_system.repository.ClassificationNodeRepository nodeRepository;
    @Mock private ApprovalService approvalService;
    @Mock private FieldEncryptionService fieldEncryptionService;
    @Mock private DataMaskingService dataMaskingService;
    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private AuthContext authContext;

    private RecordService recordService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        recordService = new RecordService(
                recordRepository,
                recordHistoryRepository,
                nodeRepository,
                approvalService,
                fieldEncryptionService,
                dataMaskingService,
                fieldDefinitionService,
                authContext
        );
    }

    @Test
    @DisplayName("레코드 수정 시 마스킹된 문자열('*')이 유입되면 기존 레코드의 원본 암호문과 Blind Index를 유지한다")
    void processDataForSave_PreservesExistingCiphertextWhenMaskedStringProvided() throws Exception {
        UUID nodeId = UUID.randomUUID();

        FieldDefinition rrnField = new FieldDefinition();
        rrnField.setKey("rrn");
        rrnField.setIsEncrypted(true);

        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(rrnField));

        String existingDataJson = "{\"rrn\":\"EXISTING_CIPHERTEXT_123\",\"_idx_rrn\":\"existing_blind_index\",\"_mask_rrn\":\"860104-1******\",\"salary\":70000000}";
        String incomingDataJson = "{\"rrn\":\"860104-1******\",\"salary\":80000000}";

        String resultJson = recordService.processDataForSave(nodeId, incomingDataJson, existingDataJson);

        Map<String, Object> resultMap = objectMapper.readValue(resultJson, Map.class);
        assertThat(resultMap.get("rrn")).isEqualTo("EXISTING_CIPHERTEXT_123");
        assertThat(resultMap.get("_idx_rrn")).isEqualTo("existing_blind_index");
        assertThat(resultMap.get("_mask_rrn")).isEqualTo("860104-1******");
        assertThat(resultMap.get("salary")).isEqualTo(80000000);

        // 암호화 메서드가 불필요하게 마스킹 문자열로 호출되지 않아야 함
        verify(fieldEncryptionService, never()).encrypt("860104-1******");
    }

    @Test
    @DisplayName("레코드 수정 시 새로운 평문이 입력되면 정상적으로 암호화하고 새 Blind Index를 생성한다")
    void processDataForSave_EncryptsNewPlaintextOnUpdate() throws Exception {
        UUID nodeId = UUID.randomUUID();

        FieldDefinition rrnField = new FieldDefinition();
        rrnField.setKey("rrn");
        rrnField.setIsEncrypted(true);

        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(rrnField));
        when(fieldEncryptionService.decrypt("900101-1234567")).thenReturn("900101-1234567");
        when(fieldEncryptionService.encrypt("900101-1234567")).thenReturn("NEW_CIPHERTEXT_456");
        when(fieldEncryptionService.generateBlindIndex("900101-1234567")).thenReturn("new_blind_index");

        String existingDataJson = "{\"rrn\":\"EXISTING_CIPHERTEXT_123\",\"_idx_rrn\":\"existing_blind_index\",\"salary\":70000000}";
        String incomingDataJson = "{\"rrn\":\"900101-1234567\",\"salary\":80000000}";

        String resultJson = recordService.processDataForSave(nodeId, incomingDataJson, existingDataJson);

        Map<String, Object> resultMap = objectMapper.readValue(resultJson, Map.class);
        assertThat(resultMap.get("rrn")).isEqualTo("NEW_CIPHERTEXT_456");
        assertThat(resultMap.get("_idx_rrn")).isEqualTo("new_blind_index");
        assertThat(resultMap.get("salary")).isEqualTo(80000000);

        verify(fieldEncryptionService).encrypt("900101-1234567");
    }
}
