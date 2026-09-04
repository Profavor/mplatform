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

    @Mock
    private com.classification.domain_system.repository.DomainRepository domainRepository;

    @Mock
    private com.classification.domain_system.repository.RecordRepository recordRepository;

    @Mock
    private com.classification.domain_system.repository.RecordHistoryRepository recordHistoryRepository;

    @Mock
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Mock
    private RecordIndexService recordIndexService;

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
        when(fieldEncryptionService.decrypt("user@example.com")).thenReturn("user@example.com");
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

    @Test
    @DisplayName("testPrepareRecordsForRead: Caches effectiveFields per nodeId to prevent N+1 queries")
    void testPrepareRecordsForRead_CachesEffectiveFieldsPerNodeId() {
        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);

        Record r1 = new Record();
        r1.setId(UUID.randomUUID());
        r1.setNode(node);
        r1.setData("{\"email\":\"ENC_1\"}");

        Record r2 = new Record();
        r2.setId(UUID.randomUUID());
        r2.setNode(node);
        r2.setData("{\"email\":\"ENC_2\"}");

        Record r3 = new Record();
        r3.setId(UUID.randomUUID());
        r3.setNode(node);
        r3.setData("{\"email\":\"ENC_3\"}");

        FieldDefinition field = new FieldDefinition();
        field.setKey("email");
        field.setIsEncrypted(true);

        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(field));
        when(authContext.hasPermission("record:unmask")).thenReturn(false);
        when(dataMaskingService.maskJsonData(anyString(), anyList(), eq(false)))
                .thenAnswer(inv -> inv.getArgument(0));

        org.springframework.data.domain.Page<Record> page = new org.springframework.data.domain.PageImpl<>(List.of(r1, r2, r3));
        recordService.prepareRecordsForRead(page);

        // 3개의 레코드이지만 동일한 nodeId이므로 getEffectiveFields는 단 1회만 호출되어야 함 (N+1 방지)
        verify(fieldDefinitionService, times(1)).getEffectiveFields(nodeId);
        verify(dataMaskingService, times(3)).maskJsonData(anyString(), anyList(), eq(false));
    }

    @Test
    @DisplayName("resetDomainRecords: JdbcTemplate이 존재할 때 명시적 개별 삭제 및 OpenSearch 인덱스 정리 수행")
    void testResetDomainRecords_WithJdbcTemplate() {
        UUID domainId = UUID.randomUUID();
        com.classification.domain_system.entity.Domain domain = new com.classification.domain_system.entity.Domain();
        domain.setId(domainId);

        when(domainRepository.findById(domainId)).thenReturn(java.util.Optional.of(domain));
        when(jdbcTemplate.update(contains("DELETE FROM record WHERE"), eq(domainId))).thenReturn(42);

        int deleted = recordService.resetDomainRecords(domainId);

        assertThat(deleted).isEqualTo(42);
        verify(jdbcTemplate).update(contains("DELETE FROM dq_violation"), eq(domainId));
        verify(jdbcTemplate).update(contains("DELETE FROM record_secondary_node"), eq(domainId));
        verify(jdbcTemplate).update(contains("DELETE FROM record_field_source"), eq(domainId));
        verify(jdbcTemplate).update(contains("DELETE FROM match_candidate"), eq(domainId), eq(domainId));
        verify(jdbcTemplate).update(contains("DELETE FROM record_history"), eq(domainId));
        verify(jdbcTemplate).update(contains("UPDATE record SET merged_into_record_id = NULL"), eq(domainId));
        verify(jdbcTemplate).update(contains("DELETE FROM record WHERE"), eq(domainId));
        verify(recordIndexService).deleteByDomainId(domainId.toString());
    }

    @Test
    @DisplayName("resetDomainRecords: 도메인이 존재하지 않으면 ResourceNotFoundException 발생")
    void testResetDomainRecords_DomainNotFound() {
        UUID domainId = UUID.randomUUID();
        when(domainRepository.findById(domainId)).thenReturn(java.util.Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> recordService.resetDomainRecords(domainId))
                .isInstanceOf(com.classification.domain_system.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Domain not found");
    }

    @Test
    @DisplayName("resetDomainRecords: domainId가 null이면 BusinessException 발생")
    void testResetDomainRecords_NullDomainId() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> recordService.resetDomainRecords(null))
                .isInstanceOf(com.classification.domain_system.exception.BusinessException.class)
                .hasMessageContaining("Domain ID is required");
    }
}
