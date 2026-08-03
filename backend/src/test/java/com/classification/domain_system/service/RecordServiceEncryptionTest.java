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
}
