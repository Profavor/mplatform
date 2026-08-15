package com.classification.domain_system.service;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecordRollbackTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private RecordHistoryRepository recordHistoryRepository;

    @Mock
    private ApprovalService approvalService;

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

    private UUID recordId;
    private UUID nodeId;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        nodeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("rollbackRecord: 성공적으로 과거 버전 데이터를 찾아 ApprovalService.createUpdateApproval을 호출한다")
    void testRollbackRecordSuccess() {
        Record record = new Record();
        record.setId(recordId);
        ClassificationNode node = new ClassificationNode();
        node.setId(nodeId);
        record.setNode(node);
        record.setData("{\"name\":\"Updated Name\",\"email\":\"updated@test.com\"}");

        RecordHistory v1 = new RecordHistory();
        v1.setRecordId(recordId);
        v1.setVersion(1);
        v1.setNewData("{\"name\":\"Original Name\",\"email\":\"original@test.com\"}");

        RecordHistory v2 = new RecordHistory();
        v2.setRecordId(recordId);
        v2.setVersion(2);
        v2.setNewData("{\"name\":\"Updated Name\",\"email\":\"updated@test.com\"}");

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId)).thenReturn(List.of(v1, v2));

        ApprovalRequest mockApproval = new ApprovalRequest();
        mockApproval.setId(UUID.randomUUID());
        mockApproval.setStatus("PENDING");
        when(approvalService.requestRecordUpdate(eq(recordId), any(RecordRequest.class))).thenReturn(mockApproval);

        ApprovalRequest result = recordService.rollbackRecord(recordId, 1, "Rollback test", "admin");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(approvalService, times(1)).requestRecordUpdate(eq(recordId), any(RecordRequest.class));
    }

    @Test
    @DisplayName("rollbackRecord: 존재하지 않는 버전 번호 지정 시 BusinessException 발생")
    void testRollbackRecordVersionNotFound() {
        Record record = new Record();
        record.setId(recordId);

        RecordHistory v1 = new RecordHistory();
        v1.setRecordId(recordId);
        v1.setVersion(1);
        v1.setNewData("{\"name\":\"Original Name\"}");

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId)).thenReturn(List.of(v1));

        assertThatThrownBy(() -> recordService.rollbackRecord(recordId, 99, "Invalid version", "admin"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Target version not found: 99");
    }
}
