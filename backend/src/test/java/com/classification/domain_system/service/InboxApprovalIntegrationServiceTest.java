package com.classification.domain_system.service;

import com.classification.domain_system.dto.InboxMessageRequest;
import com.classification.domain_system.dto.InboxMessageResponse;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.InboxMessage;
import com.classification.domain_system.repository.InboxMessageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboxApprovalIntegrationServiceTest {

    @Mock InboxService inboxService;
    @Mock ObjectMapper objectMapper;

    @InjectMocks InboxApprovalIntegrationService service;

    @Captor ArgumentCaptor<InboxMessageRequest> requestCaptor;
    @Captor ArgumentCaptor<String> senderIdCaptor;

    private ApprovalRequest mockApproval;
    private InboxMessageResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockApproval = new ApprovalRequest();
        mockApproval.setId(UUID.randomUUID());
        mockApproval.setRequesterId("requester");
        mockApproval.setRequesterName("Requester Name");
        mockApproval.setTargetType("TEST_TYPE");
        
        mockResponse = new InboxMessageResponse();
        mockResponse.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("1. 결재 상신 - 1차 담당자에게만 메시지 생성 (통보자는 상신 시 발송 안 함)")
    void onApprovalSubmitted_createsMessageToAssignee() throws Exception {
        // given
        ApprovalStep step = new ApprovalStep();
        step.setStepOrder(1);
        step.setAssigneeId("assignee1");
        mockApproval.setCurrentStepOrder(1);
        mockApproval.setSteps(List.of(step));
        mockApproval.setObserverIds("[\"obs1\", \"obs2\"]");
                
        when(inboxService.sendMessage(any(InboxMessageRequest.class), anyString()))
                .thenReturn(mockResponse);

        // when
        service.onApprovalSubmitted(mockApproval);

        // then
        verify(inboxService).sendMessage(requestCaptor.capture(), senderIdCaptor.capture());
        
        InboxMessageRequest request = requestCaptor.getValue();
        assertThat(request.getToRecipients()).containsExactly("assignee1");
        assertThat(request.getCcRecipients()).isEmpty();
        assertThat(request.getBccRecipients()).isEmpty();
        assertThat(request.getSubject()).contains("결재 요청");
        assertThat(request.getMessageType()).isEqualTo("APPROVAL_NOTICE");
        assertThat(request.getRelatedApprovalId()).isEqualTo(mockApproval.getId());
        
        assertThat(senderIdCaptor.getValue()).isEqualTo("requester");
    }

    @Test
    @DisplayName("2. 결재 승인 - 다음 결재자에게 알림")
    void onApprovalApproved_notifiesRequesterAndNextAssignee() {
        // given
        ApprovalStep step1 = new ApprovalStep();
        step1.setStepOrder(1);
        step1.setAssigneeId("assignee1");
        step1.setAssigneeName("Assignee One");

        ApprovalStep step2 = new ApprovalStep();
        step2.setStepOrder(2);
        step2.setAssigneeId("assignee2");
        
        mockApproval.setCurrentStepOrder(2);
        mockApproval.setSteps(List.of(step1, step2));

        when(inboxService.sendMessage(any(InboxMessageRequest.class), anyString()))
                .thenReturn(mockResponse);

        // when
        service.onApprovalApproved(mockApproval, step1);

        // then
        verify(inboxService).sendMessage(requestCaptor.capture(), senderIdCaptor.capture());
        
        InboxMessageRequest request = requestCaptor.getValue();
        assertThat(request.getToRecipients()).containsExactly("assignee2");
        assertThat(request.getSubject()).contains("결재 진행");
        assertThat(request.getBody()).contains("Assignee One");
        assertThat(request.getMessageType()).isEqualTo("APPROVAL_NOTICE");
        assertThat(request.getRelatedApprovalId()).isEqualTo(mockApproval.getId());
        
        assertThat(senderIdCaptor.getValue()).isEqualTo("assignee1");
    }

    @Test
    @DisplayName("3. 결재 반려 - 오직 기안자에게만 사유와 함께 알림 (통보자 제외)")
    void onApprovalRejected_notifiesRequesterWithReason() {
        // given
        ApprovalStep step = new ApprovalStep();
        step.setAssigneeId("assignee1");
        step.setAssigneeName("Assignee One");
        step.setComment("Reject Reason");
        
        mockApproval.setSteps(List.of(step));
        mockApproval.setObserverIds("[\"obs1\", \"obs2\"]");

        when(inboxService.sendMessage(any(InboxMessageRequest.class), anyString()))
                .thenReturn(mockResponse);

        // when
        service.onApprovalRejected(mockApproval, step);

        // then
        verify(inboxService).sendMessage(requestCaptor.capture(), senderIdCaptor.capture());
        
        InboxMessageRequest request = requestCaptor.getValue();
        assertThat(request.getToRecipients()).containsExactly("requester");
        assertThat(request.getCcRecipients()).isEmpty();
        assertThat(request.getBccRecipients()).isEmpty();
        assertThat(request.getSubject()).contains("결재 반려");
        assertThat(request.getBody()).contains("Reject Reason").contains("Assignee One");
        assertThat(request.getMessageType()).isEqualTo("APPROVAL_NOTICE");
        assertThat(request.getRelatedApprovalId()).isEqualTo(mockApproval.getId());
        
        assertThat(senderIdCaptor.getValue()).isEqualTo("assignee1");
    }

    @Test
    @DisplayName("4. 결재 최종 완료 - 상신자(To) 및 통보자(CC)에게 완료 알림")
    void onApprovalCompleted_notifiesAllParties() throws Exception {
        // given
        mockApproval.setObserverIds("[\"obs1\"]");

        when(objectMapper.readValue(eq("[\"obs1\"]"), any(TypeReference.class)))
                .thenReturn(List.of("obs1"));

        when(inboxService.sendMessage(any(InboxMessageRequest.class), anyString()))
                .thenReturn(mockResponse);

        // when
        service.onApprovalCompleted(mockApproval);

        // then
        verify(inboxService).sendMessage(requestCaptor.capture(), senderIdCaptor.capture());
        
        InboxMessageRequest request = requestCaptor.getValue();
        assertThat(request.getToRecipients()).containsExactly("requester");
        assertThat(request.getCcRecipients()).containsExactly("obs1");
        assertThat(request.getSubject()).contains("결재 완료");
        assertThat(request.getMessageType()).isEqualTo("APPROVAL_NOTICE");
        assertThat(request.getRelatedApprovalId()).isEqualTo(mockApproval.getId());
        
        assertThat(senderIdCaptor.getValue()).isEqualTo("SYSTEM");
    }
}
