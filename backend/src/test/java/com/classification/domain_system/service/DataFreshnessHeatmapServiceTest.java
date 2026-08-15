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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class DataFreshnessHeatmapServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private DataFreshnessHeatmapService freshnessService;

    @BeforeEach
    void setUp() {
        Domain domain = new Domain();
        domain.setId(UUID.randomUUID());
        domain.setName(Map.of("ko", "임직원"));
        domain.setUpdatedAt(LocalDateTime.now().minusMinutes(5));

        given(domainRepository.findAll()).willReturn(List.of(domain));
        given(recordRepository.findAllByDomainId(any())).willReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("getFreshnessHeatmap: 전사 도메인별 데이터 신선도 및 실시간 지연시간 히트맵 리포트 (DB 동적 연동)")
    void testGetFreshnessHeatmap() {
        DataFreshnessHeatmapDto.FreshnessHeatmapResponse res = freshnessService.getFreshnessHeatmap();

        assertThat(res).isNotNull();
        assertThat(res.getTotalDomains()).isEqualTo(1);
        assertThat(res.getOverallFreshnessScore()).isGreaterThan(80);
        assertThat(res.getStaleCount()).isEqualTo(0);
        assertThat(res.getDomains()).hasSize(1);
        assertThat(res.getDomains().get(0).getStatus()).isEqualTo("FRESH");
    }
}
