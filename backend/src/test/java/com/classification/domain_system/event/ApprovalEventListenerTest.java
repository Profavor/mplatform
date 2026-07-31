package com.classification.domain_system.event;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.service.FieldDefinitionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.classification.domain_system.service.CalculatedFieldEvaluator;
import com.classification.domain_system.service.RecordHistoryWriter;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalEventListenerTest {

    @Mock private ApprovalRequestRepository approvalRepository;
    @Mock private ApprovalStepRepository stepRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private RecordHistoryRepository recordHistoryRepository;
    @Mock private FieldDefinitionService fieldDefinitionService;
    @Mock private CalculatedFieldEvaluator calculatedFieldEvaluator;
    @Mock private RecordHistoryWriter recordHistoryWriter;
    @Mock private com.classification.domain_system.service.NotificationService notificationService;
    @Mock private UserRepository userRepository;
    @Mock private com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;

    @InjectMocks
    private ApprovalEventListener eventListener;

    @Test
    @DisplayName("성공 - ApprovalRequestCreatedEvent 발생 시, 1단계와 2단계 결재자가 기안자 본인이면 2단계까지 자동 전결된다")
    void successAutoApproveOnRequestCreated() {
        // given
        String requesterId = UUID.randomUUID().toString();
        String otherUserId = UUID.randomUUID().toString();
        UUID recordId = UUID.randomUUID();

        ApprovalRequest approval = new ApprovalRequest();
        approval.setId(UUID.randomUUID());
        approval.setRequesterId(requesterId);
        approval.setStatus("PENDING");
        approval.setCurrentStepOrder(1);
        approval.setTargetType("RECORD");
        approval.setTargetId(recordId);

        // step1: 기안자 본인
        ApprovalStep step1 = new ApprovalStep();
        step1.setApprovalRequest(approval);
        step1.setStepOrder(1);
        step1.setStepType("APPROVAL");
        step1.setAssigneeId(requesterId);
        step1.setStatus("PENDING");

        // step2: 기안자 본인
        ApprovalStep step2 = new ApprovalStep();
        step2.setApprovalRequest(approval);
        step2.setStepOrder(2);
        step2.setStepType("APPROVAL");
        step2.setAssigneeId(requesterId);
        step2.setStatus("WAITING");

        // step3: 타인
        ApprovalStep step3 = new ApprovalStep();
        step3.setApprovalRequest(approval);
        step3.setStepOrder(3);
        step3.setStepType("APPROVAL");
        step3.setAssigneeId(otherUserId);
        step3.setStatus("WAITING");

        approval.setSteps(new ArrayList<>(List.of(step1, step2, step3)));

        // when
        eventListener.onApprovalRequestCreated(new ApprovalRequestCreatedEvent(approval));

        // then
        assertThat(step1.getStatus()).isEqualTo("APPROVED");
        assertThat(step2.getStatus()).isEqualTo("APPROVED");
        assertThat(step3.getStatus()).isEqualTo("PENDING");
        assertThat(approval.getStatus()).isEqualTo("PENDING");
        assertThat(approval.getCurrentStepOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("성공 - ApprovalStepApprovedEvent 발생 시, 다음 차수의 모든 결재자가 기안자와 다르면 대기 상태로 유지된다")
    void successStopAtOtherOnStepApproved() {
        // given
        String requesterId = UUID.randomUUID().toString();
        String otherUserId = UUID.randomUUID().toString();

        ApprovalRequest approval = new ApprovalRequest();
        approval.setId(UUID.randomUUID());
        approval.setRequesterId(requesterId);
        approval.setStatus("PENDING");
        approval.setCurrentStepOrder(1);

        // step1: 수동 승인 대상 (방금 승인됨)
        ApprovalStep step1 = new ApprovalStep();
        step1.setApprovalRequest(approval);
        step1.setStepOrder(1);
        step1.setStepType("APPROVAL");
        step1.setAssigneeId(UUID.randomUUID().toString());
        step1.setStatus("APPROVED");

        // step2: 다음 차수, 결재자는 타인(너) -> 자동승인 안됨
        ApprovalStep step2 = new ApprovalStep();
        step2.setApprovalRequest(approval);
        step2.setStepOrder(2);
        step2.setStepType("APPROVAL");
        step2.setAssigneeId(otherUserId);
        step2.setStatus("WAITING");

        approval.setSteps(new ArrayList<>(List.of(step1, step2)));
        when(approvalRepository.findByIdWithLock(eq(approval.getId()))).thenReturn(Optional.of(approval));

        // when
        eventListener.onApprovalStepApproved(new ApprovalStepApprovedEvent(approval, step1));

        // then
        assertThat(step2.getStatus()).isEqualTo("PENDING");
        assertThat(approval.getStatus()).isEqualTo("PENDING");
        assertThat(approval.getCurrentStepOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("동시성 - OptimisticLockException 발생 시 또 다른 스레드가 이미 처리한 것으로 간주하여 예외 없이 종료")
    void handleOptimisticLockExceptionGracefully() {
        // given
        ApprovalRequest approval = new ApprovalRequest();
        approval.setId(UUID.randomUUID());
        approval.setStatus("PENDING");
        approval.setCurrentStepOrder(1);

        ApprovalStep step1 = new ApprovalStep();
        step1.setApprovalRequest(approval);
        step1.setStepOrder(1);
        step1.setStatus("APPROVED");

        when(approvalRepository.findByIdWithLock(eq(approval.getId())))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException("Concurrent update", new Throwable()));

        // when & then (no exception thrown)
        eventListener.onApprovalStepApproved(new ApprovalStepApprovedEvent(approval, step1));
    }
}
