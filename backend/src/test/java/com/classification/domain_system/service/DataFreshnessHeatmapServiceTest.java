package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataFreshnessHeatmapDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataFreshnessHeatmapServiceTest {

    private DataFreshnessHeatmapService freshnessService;

    @BeforeEach
    void setUp() {
        freshnessService = new DataFreshnessHeatmapService();
    }

    @Test
    @DisplayName("getFreshnessHeatmap: 전사 도메인별 데이터 신선도 및 실시간 지연시간 히트맵 리포트")
    void testGetFreshnessHeatmap() {
        DataFreshnessHeatmapDto.FreshnessHeatmapResponse res = freshnessService.getFreshnessHeatmap();

        assertThat(res).isNotNull();
        assertThat(res.getTotalDomains()).isEqualTo(5);
        assertThat(res.getOverallFreshnessScore()).isGreaterThan(90);
        assertThat(res.getStaleCount()).isEqualTo(0);
        assertThat(res.getDomains()).hasSize(5);
        assertThat(res.getDomains().get(0).getStatus()).isEqualTo("FRESH");
    }
}
