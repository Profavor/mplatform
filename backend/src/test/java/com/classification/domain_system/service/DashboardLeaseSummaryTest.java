package com.classification.domain_system.service;

import com.classification.domain_system.base.BaseServiceTest;
import com.classification.domain_system.dto.LeaseSummaryDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class DashboardLeaseSummaryTest extends BaseServiceTest {

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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("임대차(LEASE_CONTRACT) 도메인이 없으면 hasLeaseDomain=false를 반환한다")
    void returnsNoLeaseDomainWhenDomainNotFound() {
        given(domainRepository.findBySpecializedCategory("LEASE_CONTRACT")).willReturn(Optional.empty());

        LeaseSummaryDto summary = dashboardService.getLeaseSummary(null);

        assertThat(summary).isNotNull();
        assertThat(summary.getHasLeaseDomain()).isFalse();
        assertThat(summary.getTotalContracts()).isEqualTo(0);
        assertThat(summary.getUrgentAlerts()).isEmpty();
    }

    @Test
    @DisplayName("임대차 도메인 및 계약 레코드가 있으면 만기 임박, 연체, 고부채비율, 재무합계를 정확히 집계한다")
    void aggregatesLeaseMetricsCorrectly() throws Exception {
        UUID domainId = UUID.randomUUID();
        Domain leaseDomain = new Domain();
        leaseDomain.setId(domainId);
        leaseDomain.setSpecializedCategory("LEASE_CONTRACT");
        leaseDomain.setName(Map.of("ko", "부동산 임대차 마스터", "en", "Real Estate Lease Master"));

        given(domainRepository.findBySpecializedCategory("LEASE_CONTRACT")).willReturn(Optional.of(leaseDomain));

        ClassificationNode node = new ClassificationNode();
        node.setId(UUID.randomUUID());
        node.setDomain(leaseDomain);

        LocalDate today = LocalDate.now();

        // 레코드 1: 만기 15일 남음 (D-15, EXPIRING_SOON)
        Record r1 = new Record();
        r1.setId(UUID.randomUUID());
        r1.setNode(node);
        r1.setStatus("ACTIVE");
        String data1 = String.format("""
                {
                    "contract_no": "LEASE-2026-000001",
                    "building_name": "강남타워",
                    "unit_number": "101호",
                    "tenant_name": "홍길동",
                    "tenant_contact": "010-1234-5678",
                    "contract_start_date": "2024-09-01",
                    "contract_end_date": "%s",
                    "deposit_amount": 50000000,
                    "monthly_rent": 1500000,
                    "debt_ratio": 45.0,
                    "contract_status": "ACTIVE"
                }
                """, today.plusDays(15));
        r1.setData(data1);

        // 레코드 2: 임대료 연체 (OVERDUE) + 고부채비율(85%)
        Record r2 = new Record();
        r2.setId(UUID.randomUUID());
        r2.setNode(node);
        r2.setStatus("ACTIVE");
        String data2 = String.format("""
                {
                    "contract_no": "LEASE-2026-000002",
                    "building_name": "서초파크",
                    "unit_number": "202호",
                    "tenant_name": "이영희",
                    "tenant_contact": "010-9876-5432",
                    "contract_start_date": "2025-01-01",
                    "contract_end_date": "%s",
                    "deposit_amount": 100000000,
                    "monthly_rent": 2000000,
                    "debt_ratio": 85.0,
                    "contract_status": "OVERDUE"
                }
                """, today.plusDays(100));
        r2.setData(data2);

        // 레코드 3: 이미 만기 종료됨 (EXPIRED, 만기일: 10일 전)
        Record r3 = new Record();
        r3.setId(UUID.randomUUID());
        r3.setNode(node);
        r3.setStatus("ACTIVE");
        String data3 = String.format("""
                {
                    "contract_no": "LEASE-2026-000003",
                    "building_name": "역삼센트럴",
                    "unit_number": "303호",
                    "tenant_name": "김철수",
                    "contract_start_date": "2023-01-01",
                    "contract_end_date": "%s",
                    "deposit_amount": 30000000,
                    "monthly_rent": 1000000,
                    "debt_ratio": 30.0,
                    "contract_status": "EXPIRED"
                }
                """, today.minusDays(10));
        r3.setData(data3);

        List<Record> records = List.of(r1, r2, r3);
        given(recordRepository.findAllByDomainId(domainId)).willReturn(records);

        // ObjectMapper 실제 인스턴스 사용 또는 모킹
        ObjectMapper realMapper = new ObjectMapper();
        dashboardService.setObjectMapper(realMapper);

        // when
        LeaseSummaryDto summary = dashboardService.getLeaseSummary(null);

        // then
        assertThat(summary).isNotNull();
        assertThat(summary.getHasLeaseDomain()).isTrue();
        assertThat(summary.getDomainId()).isEqualTo(domainId);
        assertThat(summary.getTotalContracts()).isEqualTo(3);
        assertThat(summary.getExpiringWithin30Days()).isEqualTo(1);
        assertThat(summary.getExpiredContracts()).isEqualTo(1);
        assertThat(summary.getOverdueCount()).isEqualTo(1);
        assertThat(summary.getHighDebtRatioCount()).isEqualTo(1);
        assertThat(summary.getTotalDepositAmount()).isEqualTo(180_000_000L); // 50M + 100M + 30M
        assertThat(summary.getTotalMonthlyRent()).isEqualTo(4_500_000L);     // 1.5M + 2M + 1M

        // 긴급 조치 대상 계약(urgentAlerts) 검증
        assertThat(summary.getUrgentAlerts()).isNotEmpty();
        List<String> riskContractNos = summary.getUrgentAlerts().stream()
                .map(LeaseSummaryDto.UrgentAlertDto::getContractNo)
                .toList();
        assertThat(riskContractNos).contains("LEASE-2026-000001", "LEASE-2026-000002");
    }
}
