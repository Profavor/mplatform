package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordBulkReclassifyRequest;
import com.classification.domain_system.dto.RecordBulkReclassifyResult;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecordBulkReclassifyTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private ClassificationNodeRepository nodeRepository;

    @Mock
    private ApprovalService approvalService;

    @Mock
    private FieldEncryptionService fieldEncryptionService;

    @Mock
    private DataMaskingService dataMaskingService;

    @Mock
    private FieldDefinitionService fieldDefinitionService;

    @InjectMocks
    private RecordService recordService;

    private ClassificationNode sourceNode;
    private ClassificationNode targetNode;
    private Record record1;
    private Record record2;
    private UUID targetNodeId;
    private UUID rec1Id;
    private UUID rec2Id;

    @BeforeEach
    void setUp() {
        sourceNode = new ClassificationNode();
        sourceNode.setId(UUID.randomUUID());
        sourceNode.setName(java.util.Map.of("ko", "구 분류 노드"));

        targetNodeId = UUID.randomUUID();
        targetNode = new ClassificationNode();
        targetNode.setId(targetNodeId);
        targetNode.setName(java.util.Map.of("ko", "신규 분류 노드"));

        rec1Id = UUID.randomUUID();
        record1 = new Record();
        record1.setId(rec1Id);
        record1.setNode(sourceNode);
        record1.setData("{\"name\":\"데이터1\"}");
        record1.setVersion(1);

        rec2Id = UUID.randomUUID();
        record2 = new Record();
        record2.setId(rec2Id);
        record2.setNode(sourceNode);
        record2.setData("{\"name\":\"데이터2\"}");
        record2.setVersion(1);
    }

    @Test
    @DisplayName("bulkReclassifyRecords: 정상적으로 다중 레코드 노드 변경 및 이력 생성")
    void testBulkReclassifySuccess() {
        when(nodeRepository.findById(targetNodeId)).thenReturn(Optional.of(targetNode));
        when(recordRepository.findAllById(List.of(rec1Id, rec2Id))).thenReturn(List.of(record1, record2));

        RecordBulkReclassifyRequest req = RecordBulkReclassifyRequest.builder()
                .recordIds(List.of(rec1Id, rec2Id))
                .targetNodeId(targetNodeId)
                .reason("카테고리 개편에 따른 이동")
                .build();

        RecordBulkReclassifyResult result = recordService.bulkReclassifyRecords(req, "admin");

        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(0);

        assertThat(record1.getNode()).isEqualTo(targetNode);
        assertThat(record2.getNode()).isEqualTo(targetNode);

        verify(recordRepository, times(2)).save(any(Record.class));
        verify(recordHistoryRepository, times(2)).save(any(RecordHistory.class));
    }

    @Test
    @DisplayName("bulkReclassifyRecords: 대상 노드가 없으면 ResourceNotFoundException 발생")
    void testTargetNodeNotFound() {
        when(nodeRepository.findById(targetNodeId)).thenReturn(Optional.empty());

        RecordBulkReclassifyRequest req = RecordBulkReclassifyRequest.builder()
                .recordIds(List.of(rec1Id))
                .targetNodeId(targetNodeId)
                .build();

        assertThatThrownBy(() -> recordService.bulkReclassifyRecords(req, "admin"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Target classification node not found");
    }

    @Test
    @DisplayName("bulkReclassifyRecords: recordIds가 비어있으면 BusinessException 발생")
    void testEmptyRecordIds() {
        RecordBulkReclassifyRequest req = RecordBulkReclassifyRequest.builder()
                .recordIds(List.of())
                .targetNodeId(targetNodeId)
                .build();

        assertThatThrownBy(() -> recordService.bulkReclassifyRecords(req, "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Record IDs are required");
    }
}
