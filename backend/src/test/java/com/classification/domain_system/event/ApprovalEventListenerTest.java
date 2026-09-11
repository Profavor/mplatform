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
    @Mock private com.classification.domain_system.service.NumberingService numberingService;
    @Mock private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;
    @Mock private DomainRepository domainRepository;

    @InjectMocks
    private ApprovalEventListener eventListener;

    @Test
    @DisplayName("성공 - 결재선이 없을 때 자동 승인 처리되며 번호가 채번되어 record.data와 approval.changes에 저장된다")
    void successAutoApproveAndNumberingWhenNoSteps() {
        // given
        UUID domainId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();
        UUID identifierFieldId = UUID.randomUUID();

        com.classification.domain_system.entity.Domain domain = new com.classification.domain_system.entity.Domain();
        domain.setId(domainId);
        domain.setNumberingPattern("BIL-{SEQ:6}");
        domain.setIdentifierFieldId(identifierFieldId);

        com.classification.domain_system.entity.ClassificationNode node = new com.classification.domain_system.entity.ClassificationNode();
        node.setId(nodeId);
        node.setDomain(domain);

        com.classification.domain_system.entity.Record record = new com.classification.domain_system.entity.Record();
        record.setId(recordId);
        record.setNode(node);
        record.setStatus("PENDING_APPROVAL");
        record.setData("{\"BILL_NO\":\"2208232\"}");

        ApprovalRequest approval = new ApprovalRequest();
        approval.setId(UUID.randomUUID());
        approval.setTargetType("RECORD");
        approval.setTargetId(recordId);
        approval.setRequesterId("tester");
        approval.setStatus("PENDING");
        approval.setChanges("{\"BILL_NO\":\"2208232\"}");
        approval.setSteps(new ArrayList<>()); // 결재선 0개

        com.classification.domain_system.entity.FieldDefinition idFieldDef = new com.classification.domain_system.entity.FieldDefinition();
        idFieldDef.setId(identifierFieldId);
        idFieldDef.setKey("BILL_CODE");

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));
        when(numberingService.issueNextCode(domainId)).thenReturn("BIL-000001");
        when(fieldDefinitionService.getEffectiveFields(nodeId)).thenReturn(List.of(idFieldDef));
        when(calculatedFieldEvaluator.recomputeCalculatedFields(eq(nodeId), any())).thenAnswer(i -> i.getArgument(1));
        when(recordRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(approvalRepository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        // when
        eventListener.onApprovalRequestCreated(new ApprovalRequestCreatedEvent(approval));

        // then
        assertThat(approval.getStatus()).isEqualTo("APPROVED");
        assertThat(record.getStatus()).isEqualTo("ACTIVE");
        assertThat(record.getData()).contains("\"BILL_CODE\":\"BIL-000001\"");
        assertThat(approval.getChanges()).contains("\"BILL_CODE\":\"BIL-000001\"");
        org.mockito.Mockito.verify(recordRepository).saveAndFlush(record);
        org.mockito.Mockito.verify(approvalRepository).saveAndFlush(approval);
    }

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
