package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.MatchCandidateRepository;
import com.classification.domain_system.repository.DqViolationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
    @DisplayName("getStats - 도메인, 승인, 레코드, 매칭, 품질위반 집계 검증")
    void getStats_ValidatesCounts() {
        // given
        when(domainRepository.count()).thenReturn(10L);

        ApprovalRequest req1 = new ApprovalRequest();
        when(approvalRepository.findByStatusOrderByCreatedAtDesc(eq("PENDING"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(req1, req1, req1)));
        when(approvalRepository.findByStatusOrderByCreatedAtDesc(eq("APPROVED"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(req1)));
        when(approvalRepository.findByStatusOrderByCreatedAtDesc(eq("REJECTED"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        when(recordRepository.countByStatus("ACTIVE")).thenReturn(2L);
        when(matchCandidateRepository.countByStatus("PENDING")).thenReturn(5L);
        when(dqViolationRepository.countByResolvedFalse()).thenReturn(1L);

        // when
        Map<String, Object> stats = dashboardService.getStats();

        // then
        assertThat(stats.get("totalDomains")).isEqualTo(10L);
        assertThat(stats.get("pendingApprovals")).isEqualTo(3L);
        assertThat(stats.get("approvedApprovals")).isEqualTo(1L);
        assertThat(stats.get("activeRecords")).isEqualTo(2L);
        assertThat(stats.get("pendingMatches")).isEqualTo(5L);
        assertThat(stats.get("openDqViolations")).isEqualTo(1L);
    }
}
