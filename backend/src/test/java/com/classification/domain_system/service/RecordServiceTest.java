package com.classification.domain_system.service;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private FieldEncryptionService fieldEncryptionService;

    @Mock
    private DataMaskingService dataMaskingService;

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @Mock
    private AuthContext authContext;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private RecordService recordService;

    private UUID nodeId;

    @BeforeEach
    void setUp() {
        nodeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("testProcessDataForSave: Encrypts field and adds blind index when isEncrypted is true")
    void testProcessDataForSave() throws Exception {
        FieldDefinition field = new FieldDefinition();
        field.setKey("email");
        field.setIsEncrypted(true);

        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
        when(fieldEncryptionService.encrypt("user@example.com")).thenReturn("ENCRYPTED_STRING");
        when(fieldEncryptionService.generateBlindIndex("user@example.com")).thenReturn("BLIND_INDEX_HASH");

        String inputJson = "{\"email\":\"user@example.com\",\"name\":\"John Doe\"}";
        String resultJson = recordService.processDataForSave(nodeId, inputJson);

        JsonNode resultNode = objectMapper.readTree(resultJson);
        assertThat(resultNode.get("email").asText()).isEqualTo("ENCRYPTED_STRING");
        assertThat(resultNode.get("_idx_email").asText()).isEqualTo("BLIND_INDEX_HASH");
        assertThat(resultNode.get("name").asText()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("testPrepareRecordForRead: Calls DataMaskingService according to AuthContext unmask permission")
    void testPrepareRecordForRead() {
        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);

        Record record = new Record();
        record.setId(UUID.randomUUID());
        record.setNode(node);
        record.setData("{\"email\":\"ENCRYPTED_STRING\"}");

        FieldDefinition field = new FieldDefinition();
        field.setKey("email");
        field.setIsEncrypted(true);

        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
        when(authContext.hasPermission("record:unmask")).thenReturn(false);
        when(dataMaskingService.maskJsonData(anyString(), anyList(), eq(false)))
                .thenReturn("{\"email\":\"u***@example.com\"}");

        Record processed = recordService.prepareRecordForRead(record);

        assertThat(processed.getData()).isEqualTo("{\"email\":\"u***@example.com\"}");
        verify(dataMaskingService).maskJsonData(anyString(), anyList(), eq(false));
    }
}
