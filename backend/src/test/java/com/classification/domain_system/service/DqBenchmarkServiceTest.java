package com.classification.domain_system.service;

import com.classification.domain_system.dto.DqBenchmarkDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.DqScoreSnapshot;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.DqScoreSnapshotRepository;
import com.classification.domain_system.service.dq.DqRuleEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DqBenchmarkServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private DqRuleEngine dqRuleEngine;

    @Mock
    private DqRuleRepository dqRuleRepository;

    @Mock
    private DqScoreSnapshotRepository dqScoreSnapshotRepository;

    @InjectMocks
    private DqBenchmarkService dqBenchmarkService;

    private Domain domain1;
    private Domain domain2;
    private Domain domain3;

    @BeforeEach
    void setUp() {
        domain1 = new Domain();
        domain1.setId(UUID.randomUUID());
        domain1.setNumberingPattern("CUSTOMER");
        domain1.setName(Map.of("ko", "고객 마스터", "en", "Customer Master"));

        domain2 = new Domain();
        domain2.setId(UUID.randomUUID());
        domain2.setNumberingPattern("STOCK");
        domain2.setName(Map.of("ko", "주식 마스터", "en", "Stock Master"));

        domain3 = new Domain();
        domain3.setId(UUID.randomUUID());
        domain3.setNumberingPattern("CARTBOM");
        domain3.setName(Map.of("ko", "카트봄 마스터", "en", "Cartbom Master"));
    }

    @Test
    @DisplayName("모든 도메인의 점수, 등급, 통계 및 위험 수준을 올바르게 집계한다")
    void getBenchmarkOverview_CalculatesMetricsAndRiskLevelsCorrectly() {
        when(domainRepository.findAll()).thenReturn(List.of(domain1, domain2, domain3));

        // Domain 1: 95.0 (A, HEALTHY)
        when(dqRuleEngine.getDomainDqScore(domain1.getId())).thenReturn(Map.of(
                "score", 95.0,
                "totalRecords", 1000L,
                "totalViolations", 5L,
                "violationsBySeverity", Map.of("ERROR", 1, "WARNING", 4)
        ));
        when(dqRuleRepository.countByDomainId(domain1.getId())).thenReturn(10L);

        // Domain 2: 85.0 (B, WARNING)
        when(dqRuleEngine.getDomainDqScore(domain2.getId())).thenReturn(Map.of(
                "score", 85.0,
                "totalRecords", 5000L,
                "totalViolations", 50L,
                "violationsBySeverity", Map.of("ERROR", 10, "WARNING", 40)
        ));
        when(dqRuleRepository.countByDomainId(domain2.getId())).thenReturn(15L);

        // Domain 3: 72.0 (C, HIGH_RISK)
        when(dqRuleEngine.getDomainDqScore(domain3.getId())).thenReturn(Map.of(
                "score", 72.0,
                "totalRecords", 3000L,
                "totalViolations", 120L,
                "violationsBySeverity", Map.of("ERROR", 40, "WARNING", 80)
        ));
        when(dqRuleRepository.countByDomainId(domain3.getId())).thenReturn(8L);

        // 스냅샷 비어있음
        when(dqScoreSnapshotRepository.findTop30ByDomainIdOrderByRecordedAtDesc(any())).thenReturn(Collections.emptyList());

        DqBenchmarkDto.OverviewResponse response = dqBenchmarkService.getBenchmarkOverview();

        assertThat(response).isNotNull();
        assertThat(response.getTotalMonitoredDomains()).isEqualTo(3);
        assertThat(response.getAverageScore()).isCloseTo(84.0, within(0.01));
        assertThat(response.getTotalViolations()).isEqualTo(175L);
        assertThat(response.getHighRiskDomainCount()).isEqualTo(1);

        assertThat(response.getHighestDomain().getDomainCode()).isEqualTo("CUSTOMER");
        assertThat(response.getHighestDomain().getGrade()).isEqualTo("A");
        assertThat(response.getHighestDomain().getRiskLevel()).isEqualTo("HEALTHY");

        assertThat(response.getLowestDomain().getDomainCode()).isEqualTo("CARTBOM");
        assertThat(response.getLowestDomain().getGrade()).isEqualTo("C");
        assertThat(response.getLowestDomain().getRiskLevel()).isEqualTo("HIGH_RISK");

        // 개별 아이템 검증
        assertThat(response.getItems()).hasSize(3);
        DqBenchmarkDto.BenchmarkItem stockItem = response.getItems().stream()
                .filter(i -> "STOCK".equals(i.getDomainCode()))
                .findFirst()
                .orElseThrow();
        assertThat(stockItem.getGrade()).isEqualTo("B");
        assertThat(stockItem.getRiskLevel()).isEqualTo("WARNING");
        assertThat(stockItem.getRuleCount()).isEqualTo(15L);
        assertThat(stockItem.getErrorCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("이전 스냅샷과의 점수 차이(trendDelta)를 올바르게 계산한다")
    void getBenchmarkOverview_CalculatesTrendDeltaFromSnapshots() {
        when(domainRepository.findAll()).thenReturn(List.of(domain1));

        when(dqRuleEngine.getDomainDqScore(domain1.getId())).thenReturn(Map.of(
                "score", 95.0,
                "totalRecords", 1000L,
                "totalViolations", 5L,
                "violationsBySeverity", Collections.emptyMap()
        ));
        when(dqRuleRepository.countByDomainId(domain1.getId())).thenReturn(5L);

        DqScoreSnapshot snap1 = new DqScoreSnapshot();
        snap1.setScore(95.0);
        snap1.setRecordedAt(LocalDateTime.now());

        DqScoreSnapshot snap2 = new DqScoreSnapshot();
        snap2.setScore(90.0);
        snap2.setRecordedAt(LocalDateTime.now().minusDays(1));

        when(dqScoreSnapshotRepository.findTop30ByDomainIdOrderByRecordedAtDesc(domain1.getId()))
                .thenReturn(List.of(snap1, snap2));

        DqBenchmarkDto.OverviewResponse response = dqBenchmarkService.getBenchmarkOverview();

        assertThat(response.getItems()).hasSize(1);
        // 최신 스냅샷(95.0) - 직전 스냅샷(90.0) = +5.0
        assertThat(response.getItems().get(0).getTrendDelta()).isCloseTo(5.0, within(0.01));
    }

    @Test
    @DisplayName("모든 도메인의 시계열 트렌드 포인트를 시간순으로 조립한다")
    void getMultiDomainTrend_AssemblesChronologicalTrendPointsForEachDomain() {
        when(domainRepository.findAll()).thenReturn(List.of(domain1, domain2));

        LocalDateTime t1 = LocalDateTime.now().minusDays(2);
        LocalDateTime t2 = LocalDateTime.now().minusDays(1);

        DqScoreSnapshot s1 = new DqScoreSnapshot();
        s1.setDomainId(domain1.getId());
        s1.setScore(80.0);
        s1.setTotalViolations(10L);
        s1.setRecordedAt(t1);

        DqScoreSnapshot s2 = new DqScoreSnapshot();
        s2.setDomainId(domain1.getId());
        s2.setScore(85.0);
        s2.setTotalViolations(8L);
        s2.setRecordedAt(t2);

        when(dqScoreSnapshotRepository.findByDomainIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                eq(domain1.getId()), any(), any())).thenReturn(List.of(s1, s2));

        when(dqScoreSnapshotRepository.findByDomainIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                eq(domain2.getId()), any(), any())).thenReturn(Collections.emptyList());

        List<DqBenchmarkDto.MultiDomainTrend> trends = dqBenchmarkService.getMultiDomainTrend(30);

        assertThat(trends).hasSize(2);
        DqBenchmarkDto.MultiDomainTrend d1Trend = trends.stream()
                .filter(t -> "CUSTOMER".equals(t.getDomainCode()))
                .findFirst()
                .orElseThrow();
        assertThat(d1Trend.getDataPoints()).hasSize(2);
        assertThat(d1Trend.getDataPoints().get(0).getScore()).isEqualTo(80.0);
        assertThat(d1Trend.getDataPoints().get(1).getScore()).isEqualTo(85.0);
    }
}
