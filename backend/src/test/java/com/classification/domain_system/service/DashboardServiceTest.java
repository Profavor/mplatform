package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.DqViolation;
import com.classification.domain_system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private ApprovalRequestRepository approvalRepository;

    @Mock
    private MatchCandidateRepository matchCandidateRepository;

    @Mock
    private DqViolationRepository dqViolationRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getApprovalTrends - 7일간의 결재 요청 트렌드를 일자별로 반환한다")
    void getApprovalTrends_CalculatesDailyCounts() {
        ApprovalRequest req1 = new ApprovalRequest();
        req1.setCreatedAt(LocalDateTime.now().minusDays(1));

        ApprovalRequest req2 = new ApprovalRequest();
        req2.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(approvalRepository.findByCreatedAtAfter(any(LocalDateTime.class)))
                .thenReturn(List.of(req1, req2));

        List<Map<String, Object>> trends = dashboardService.getApprovalTrends();

        assertThat(trends).hasSize(7);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Map<String, Object> yesterdayTrend = trends.stream()
                .filter(t -> yesterday.toString().equals(t.get("date")))
                .findFirst()
                .orElse(null);

        assertThat(yesterdayTrend).isNotNull();
        assertThat(yesterdayTrend.get("count")).isEqualTo(2L);
    }

    @Test
    @DisplayName("getDqTrends - findAll 풀스캔 대신 findByCheckedAtAfter를 사용하여 기간 내 위반만 조회한다")
    void getDqTrends_UsesOptimizedDateFilteredQuery() {
        DqViolation v1 = new DqViolation();
        v1.setCheckedAt(LocalDateTime.now().minusDays(2));

        when(dqViolationRepository.findByCheckedAtAfter(any(LocalDateTime.class)))
                .thenReturn(List.of(v1));

        List<Map<String, Object>> trends = dashboardService.getDqTrends();

        assertThat(trends).hasSize(7);
        verify(dqViolationRepository, never()).findAll();
        verify(dqViolationRepository, times(1)).findByCheckedAtAfter(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("getDqSeverityDistribution - findAll 풀스캔 대신 findByResolvedFalse를 사용하여 미해결 건만 조회한다")
    void getDqSeverityDistribution_UsesOptimizedUnresolvedQuery() {
        DqViolation v1 = new DqViolation();
        v1.setSeverity("HIGH");
        v1.setResolved(false);

        DqViolation v2 = new DqViolation();
        v2.setSeverity("HIGH");
        v2.setResolved(false);

        DqViolation v3 = new DqViolation();
        v3.setSeverity("LOW");
        v3.setResolved(false);

        when(dqViolationRepository.findByResolvedFalse())
                .thenReturn(List.of(v1, v2, v3));

        List<Map<String, Object>> distribution = dashboardService.getDqSeverityDistribution();

        assertThat(distribution).hasSize(2);
        verify(dqViolationRepository, never()).findAll();
        verify(dqViolationRepository, times(1)).findByResolvedFalse();
    }
}
