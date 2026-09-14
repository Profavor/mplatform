package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataAssetDto;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataAssetValuationServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private DataAssetValuationService valuationService;

    private Domain domain1;
    private Domain domain2;

    @BeforeEach
    void setUp() {
        domain1 = new Domain();
        domain1.setId(UUID.randomUUID());
        domain1.setName(Map.of("ko", "고객 마스터", "en", "Customer Master"));

        domain2 = new Domain();
        domain2.setId(UUID.randomUUID());
        domain2.setName(Map.of("ko", "주식 마스터", "en", "Stock Master"));
    }

    @Test
    @DisplayName("evaluateDataAssets - findAllByDomainId 풀스캔 대신 countByDomainId 단건 쿼리를 사용하여 자산 가치를 계산한다")
    void evaluateDataAssets_UsesOptimizedCountQuery() {
        when(domainRepository.findAll()).thenReturn(List.of(domain1, domain2));
        when(recordRepository.countByDomainId(domain1.getId())).thenReturn(100L);
        when(recordRepository.countByDomainId(domain2.getId())).thenReturn(500L);

        DataAssetDto.DataAssetSummaryResponse result = valuationService.evaluateDataAssets();

        assertThat(result).isNotNull();
        assertThat(result.getTotalDomainsEvaluated()).isEqualTo(2);
        assertThat(result.getDomainValuations()).hasSize(2);

        // 첫 번째 도메인 레코드 수 및 가치 확인
        DataAssetDto.DomainValuationItem item1 = result.getDomainValuations().get(0);
        assertThat(item1.getDomainId()).isEqualTo(domain1.getId());
        assertThat(item1.getRecordCount()).isEqualTo(100);

        // 두 번째 도메인 레코드 수 확인
        DataAssetDto.DomainValuationItem item2 = result.getDomainValuations().get(1);
        assertThat(item2.getDomainId()).isEqualTo(domain2.getId());
        assertThat(item2.getRecordCount()).isEqualTo(500);

        // 핵심 검증: findAllByDomainId 풀스캔은 절대 호출되지 않고 countByDomainId만 호출되어야 함
        verify(recordRepository, never()).findAllByDomainId(any());
        verify(recordRepository, times(1)).countByDomainId(domain1.getId());
        verify(recordRepository, times(1)).countByDomainId(domain2.getId());
    }
}
