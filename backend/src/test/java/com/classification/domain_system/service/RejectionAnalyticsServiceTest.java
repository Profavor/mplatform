package com.classification.domain_system.service;

import com.classification.domain_system.dto.RejectionAnalyticsDto;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RejectionAnalyticsServiceTest {

    @Mock private ApprovalRequestRepository approvalRequestRepository;

    @InjectMocks
    private RejectionAnalyticsService rejectionAnalyticsService;

    private ApprovalRequest req1;
    private ApprovalRequest req2;

    @BeforeEach
    void setUp() {
        req1 = new ApprovalRequest();
        req1.setId(UUID.randomUUID());
        req1.setStatus("REJECTED");
        req1.setReason("필수 사업자번호 누락으로 인한 반려");

        ApprovalStep step = new ApprovalStep();
        step.setComment("전화번호 포맷 오류입니다.");
        req2 = new ApprovalRequest();
        req2.setId(UUID.randomUUID());
        req2.setStatus("REJECTED");
        req2.setSteps(List.of(step));
    }

    @Test
    @DisplayName("analyzeRejections: 결재 반려 이력 분석 및 원인별 통계 산출")
    void testAnalyzeRejections() {
        when(approvalRequestRepository.findByStatus("REJECTED")).thenReturn(List.of(req1, req2));

        RejectionAnalyticsDto.RejectionAnalysisResponse res = rejectionAnalyticsService.analyzeRejections();

        assertThat(res).isNotNull();
        assertThat(res.getTotalRejections()).isEqualTo(2);
        assertThat(res.getTopCategories()).isNotEmpty();
        assertThat(res.getRecommendedChecklist()).isNotEmpty();
    }
}
