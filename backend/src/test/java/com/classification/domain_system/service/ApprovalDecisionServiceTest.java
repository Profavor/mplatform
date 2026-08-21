package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.exception.CustomAccessDeniedException;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalDecisionServiceTest {

    @Mock
    private ApprovalRequestRepository approvalRepository;
    @Mock
    private ApprovalStepRepository stepRepository;
    @Mock
    private RecordRepository recordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApprovalNotificationFacade notificationFacade;
    @Mock
    private ApprovalDelegationService delegationService;
    @Mock
    private com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;
    @Mock
    private BatchJobRepository batchJobRepository;
    @Mock
    private StagingRecordRepository stagingRecordRepository;

    @InjectMocks
    private ApprovalDecisionService decisionService;

    private UUID stepId;
    private UUID approvalId;
    private ApprovalStep mockStep;
    private ApprovalRequest mockApproval;

    @BeforeEach
    void setUp() {
        stepId = UUID.randomUUID();
        approvalId = UUID.randomUUID();

        mockApproval = new ApprovalRequest();
        mockApproval.setId(approvalId);
        mockApproval.setStatus(ApprovalStatus.PENDING.name());
        mockApproval.setTargetType("RECORD");

        mockStep = new ApprovalStep();
        mockStep.setId(stepId);
        mockStep.setAssigneeId("approver1");
        mockStep.setStatus(ApprovalStatus.PENDING.name());
        mockStep.setApprovalRequest(mockApproval);
    }

    @Test
    @DisplayName("approveStep - 담당 결재자 승인 성공")
    void approveStep_Success() {
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));
        when(stepRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        ApprovalRequest result = decisionService.approveStep(stepId, "approver1", "승인합니다");

        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED.name(), mockStep.getStatus());
        assertEquals("승인합니다", mockStep.getComment());
        verify(stepRepository).saveAndFlush(mockStep);
        verify(notificationFacade).publishApprovalStepApproved(mockApproval, mockStep);
    }

    @Test
    @DisplayName("approveStep - 권한 없는 사용자가 승인 시도 시 CustomAccessDeniedException 발생")
    void approveStep_NotAssignee_ThrowsException() {
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));

        assertThrows(CustomAccessDeniedException.class, () ->
            decisionService.approveStep(stepId, "unauthorizedUser", "승인")
        );
    }

    @Test
    @DisplayName("rejectStep - 반려 성공 시 상태 REJECTED로 변경 및 알림 발송")
    void rejectStep_Success() {
        UUID recordId = UUID.randomUUID();
        mockApproval.setTargetId(recordId);

        Record record = new Record();
        record.setId(recordId);
        record.setStatus(ApprovalStatus.PENDING.name());

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(mockStep));
        when(stepRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(approvalRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));

        ApprovalRequest result = decisionService.rejectStep(stepId, "approver1", "사유: 불일치");

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED.name(), mockStep.getStatus());
        assertEquals(ApprovalStatus.REJECTED.name(), mockApproval.getStatus());
        verify(notificationFacade).sendRejectionNotification(eq(mockApproval), eq("approver1"), eq("사유: 불일치"));
    }

    @Test
    @DisplayName("cancelApprovalRequest - 기안자 본인이 PENDING 요청 취소 성공")
    void cancelApprovalRequest_Success() {
        mockApproval.setRequesterId("user1");
        mockApproval.setTargetType("RECORD");
        UUID recordId = UUID.randomUUID();
        mockApproval.setTargetId(recordId);

        Record record = new Record();
        record.setId(recordId);

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(mockApproval));
        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(approvalRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        ApprovalRequest result = decisionService.cancelApprovalRequest(approvalId, "user1", "실수로 상신함");

        assertNotNull(result);
        assertEquals(ApprovalStatus.CANCELLED.name(), result.getStatus());
        assertEquals("실수로 상신함", result.getReason());
        verify(approvalRepository).saveAndFlush(mockApproval);
    }
}
