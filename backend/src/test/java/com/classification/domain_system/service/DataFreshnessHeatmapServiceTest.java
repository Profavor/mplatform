package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataFreshnessHeatmapDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataFreshnessHeatmapServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private DataFreshnessHeatmapService heatmapService;

    private Domain domain1;
    private Domain domain2;

    @BeforeEach
    void setUp() {
        domain1 = new Domain();
        domain1.setId(UUID.randomUUID());
        domain1.setName(Map.of("ko", "고객 마스터", "en", "Customer Master"));
        domain1.setCreatedAt(LocalDateTime.now().minusDays(10));
        domain1.setUpdatedAt(LocalDateTime.now().minusDays(5));

        domain2 = new Domain();
        domain2.setId(UUID.randomUUID());
        domain2.setName(Map.of("ko", "주식 마스터", "en", "Stock Master"));
        domain2.setCreatedAt(LocalDateTime.now().minusDays(20));
        domain2.setUpdatedAt(LocalDateTime.now().minusDays(10));
    }

    @Test
    @DisplayName("getFreshnessHeatmap - findAllByDomainId 풀스캔 대신 findLatestCreatedAtByDomainId 단건 쿼리를 사용하여 신선도를 산출한다")
    void getFreshnessHeatmap_UsesOptimizedLatestCreatedAtQuery() {
        LocalDateTime domain1Latest = LocalDateTime.now().minusMinutes(10);
        LocalDateTime domain2Latest = LocalDateTime.now().minusHours(2);

        when(domainRepository.findAll()).thenReturn(List.of(domain1, domain2));
        when(recordRepository.findLatestCreatedAtByDomainId(domain1.getId())).thenReturn(domain1Latest);
        when(recordRepository.findLatestCreatedAtByDomainId(domain2.getId())).thenReturn(domain2Latest);

        DataFreshnessHeatmapDto.FreshnessHeatmapResponse result = heatmapService.getFreshnessHeatmap();

        assertThat(result).isNotNull();
        assertThat(result.getTotalDomains()).isEqualTo(2);
        assertThat(result.getDomains()).hasSize(2);

        // 첫 번째 도메인 (10분 전이므로 FRESH)
        DataFreshnessHeatmapDto.DomainFreshnessItem item1 = result.getDomains().get(0);
        assertThat(item1.getStatus()).isEqualTo("FRESH");
        assertThat(item1.getDelayMinutes()).isLessThanOrEqualTo(15);

        // 두 번째 도메인 (2시간 전이므로 SLA 60분 초과 -> STALE)
        DataFreshnessHeatmapDto.DomainFreshnessItem item2 = result.getDomains().get(1);
        assertThat(item2.getStatus()).isEqualTo("STALE");
        assertThat(item2.getDelayMinutes()).isGreaterThanOrEqualTo(110);

        // 핵심 검증: findAllByDomainId 풀스캔은 호출되지 않고 findLatestCreatedAtByDomainId만 호출되어야 함
        verify(recordRepository, never()).findAllByDomainId(any());
        verify(recordRepository, times(1)).findLatestCreatedAtByDomainId(domain1.getId());
        verify(recordRepository, times(1)).findLatestCreatedAtByDomainId(domain2.getId());
    }
}
