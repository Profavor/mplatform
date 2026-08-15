package com.classification.domain_system.service;

import com.classification.domain_system.dto.ApprovalSandboxDto;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApprovalSandboxServiceTest {

    @Mock private ApprovalRequestRepository approvalRequestRepository;
    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private ApprovalSandboxService approvalSandboxService;

    private UUID requestId;
    private UUID recordId;
    private ApprovalRequest approvalRequest;
    private Record record;

    @BeforeEach
    void setUp() {
        requestId = UUID.randomUUID();
        recordId = UUID.randomUUID();

        approvalRequest = new ApprovalRequest();
        approvalRequest.setId(requestId);
        approvalRequest.setTargetType("RECORD");
        approvalRequest.setTargetId(recordId);
        approvalRequest.setChanges("{\"data\": {\"phone\": \"010-9999-0000\", \"grade\": \"VIP\"}}");

        record = new Record();
        record.setId(recordId);
        record.setData("{\"name\": \"홍길동\", \"phone\": \"010-1111-2222\"}");
    }

    @Test
    @DisplayName("generateSandboxPreview: 결재 변경사항 가상 시뮬레이션 및 Diff 생성 검증")
    void testGenerateSandboxPreview() {
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.of(approvalRequest));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        ApprovalSandboxDto.SandboxPreviewResponse response = approvalSandboxService.generateSandboxPreview(requestId);

        assertThat(response).isNotNull();
        assertThat(response.getActionType()).isEqualTo("UPDATE");
        assertThat(response.getTargetRecords()).hasSize(1);

        ApprovalSandboxDto.TargetRecordPreview preview = response.getTargetRecords().get(0);
        assertThat(preview.getSimulatedData()).containsEntry("phone", "010-9999-0000");
        assertThat(preview.getSimulatedData()).containsEntry("grade", "VIP");

        // phone -> MODIFIED, grade -> ADDED, name -> UNCHANGED
        assertThat(preview.getFieldDiffs().stream().anyMatch(d -> d.getFieldKey().equals("phone") && d.getDiffStatus().equals("MODIFIED"))).isTrue();
        assertThat(preview.getFieldDiffs().stream().anyMatch(d -> d.getFieldKey().equals("grade") && d.getDiffStatus().equals("ADDED"))).isTrue();
        assertThat(preview.getFieldDiffs().stream().anyMatch(d -> d.getFieldKey().equals("name") && d.getDiffStatus().equals("UNCHANGED"))).isTrue();
    }

    @Test
    @DisplayName("generateSandboxPreview: 결재 요청 미존재 시 ResourceNotFoundException 발생")
    void testApprovalNotFound() {
        when(approvalRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvalSandboxService.generateSandboxPreview(requestId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Approval request not found");
    }
}
