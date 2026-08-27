package com.classification.domain_system.service;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.DataMaskingService;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.service.FieldEncryptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordServiceEncryptionTest {

    @Mock
    private FieldEncryptionService fieldEncryptionService;

    @Mock
    private DataMaskingService dataMaskingService;

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private RecordService recordService;

    @Test
    void testProcessDataForSaveThrowsExceptionOnEncryptionFailure() throws Exception {
        UUID nodeId = UUID.randomUUID();
        String dataJson = "{\"secretField\":\"secretValue\"}";

        FieldDefinition field = new FieldDefinition();
        field.setKey("secretField");
        field.setIsEncrypted(true);

        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
        when(fieldEncryptionService.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0)); // return plaintext
        when(fieldEncryptionService.encrypt(anyString())).thenThrow(new RuntimeException("Encryption error"));

        assertThrows(RuntimeException.class, () -> {
            recordService.processDataForSave(nodeId, dataJson);
        });
    }

    @Test
    void testProcessDataForSaveCreatesMaskCacheForEncryptedField() throws Exception {
        UUID nodeId = UUID.randomUUID();
        String dataJson = "{\"resident_number\":\"900101-1234567\"}";

        FieldDefinition field = new FieldDefinition();
        field.setKey("resident_number");
        field.setIsEncrypted(true);
        field.setMaskingPattern("RRN");

        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
        when(fieldEncryptionService.decrypt("900101-1234567")).thenReturn("900101-1234567"); // not ciphertext yet
        when(fieldEncryptionService.encrypt("900101-1234567")).thenReturn("vault:v1:encryptedRrn");
        when(fieldEncryptionService.generateBlindIndex("900101-1234567")).thenReturn("blind_index_val");
        when(dataMaskingService.maskByPattern("RRN", "900101-1234567")).thenReturn("900101-1******");

        String resultJson = recordService.processDataForSave(nodeId, dataJson);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.Map<String, Object> resultMap = mapper.readValue(resultJson, java.util.Map.class);

        org.assertj.core.api.Assertions.assertThat(resultMap.get("resident_number")).isEqualTo("vault:v1:encryptedRrn");
        org.assertj.core.api.Assertions.assertThat(resultMap.get("_idx_resident_number")).isEqualTo("blind_index_val");
        org.assertj.core.api.Assertions.assertThat(resultMap.get("_mask_resident_number")).isEqualTo("900101-1******");
    }
}
