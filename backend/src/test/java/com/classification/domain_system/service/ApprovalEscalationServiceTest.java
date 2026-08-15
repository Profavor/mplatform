package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.repository.ApprovalStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApprovalEscalationServiceTest {

    @Mock
    private ApprovalStepRepository approvalStepRepository;

    @InjectMocks
    private ApprovalEscalationService escalationService;

    @Test
    @DisplayName("scanAndEscalate: SLA 기한 초과 미결 결재 단계 발견 시 ROLE_ADMIN으로 이관 및 코멘트 추가")
    void testScanAndEscalate() {
        ApprovalStep step = new ApprovalStep();
        step.setId(UUID.randomUUID());
        step.setStatus("PENDING");
        step.setAssigneeId("user-1");
        step.setSlaDueAt(LocalDateTime.now().minusHours(2));
        step.setIsEscalated(false);

        when(approvalStepRepository.findByStatusAndSlaDueAtBeforeAndIsEscalatedFalse(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of(step));

        int count = escalationService.scanAndEscalate();

        assertThat(count).isEqualTo(1);
        assertThat(step.getIsEscalated()).isTrue();
        assertThat(step.getEscalatedFromUserId()).isEqualTo("user-1");
        assertThat(step.getAssigneeRole()).isEqualTo("ROLE_ADMIN");
        assertThat(step.getComment()).contains("[SLA 만료 자동 에스컬레이션]");
        verify(approvalStepRepository, times(1)).save(step);
    }

    @Test
    @DisplayName("scanAndEscalate: 기한 초과 건이 없으면 0 반환")
    void testScanNoExpired() {
        when(approvalStepRepository.findByStatusAndSlaDueAtBeforeAndIsEscalatedFalse(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        int count = escalationService.scanAndEscalate();

        assertThat(count).isEqualTo(0);
        verify(approvalStepRepository, never()).save(any());
    }
}
