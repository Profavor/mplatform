package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataAssetDto;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataAssetValuationServiceTest {

    @Mock private DomainRepository domainRepository;
    @Mock private RecordRepository recordRepository;

    @InjectMocks
    private DataAssetValuationService dataAssetValuationService;

    private UUID domainId;
    private Domain domain;

    @BeforeEach
    void setUp() {
        domainId = UUID.randomUUID();
        domain = new Domain();
        domain.setId(domainId);
        domain.setName(Map.of("ko", "고객 도메인", "en", "Customer Domain"));
    }

    @Test
    @DisplayName("evaluateDataAssets: 전사 도메인 데이터 자산 가치 평가 및 등급 산정")
    void testEvaluateDataAssets() {
        when(domainRepository.findAll()).thenReturn(List.of(domain));
        when(recordRepository.findAllByDomainId(domainId)).thenReturn(List.of(new Record(), new Record()));

        DataAssetDto.DataAssetSummaryResponse res = dataAssetValuationService.evaluateDataAssets();

        assertThat(res).isNotNull();
        assertThat(res.getTotalDomainsEvaluated()).isEqualTo(1);
        assertThat(res.getTotalEstimatedAssetValueWon()).isGreaterThan(0);
        assertThat(res.getDomainValuations()).hasSize(1);
        assertThat(res.getDomainValuations().get(0).getAssetRating()).isNotEmpty();
    }
}
