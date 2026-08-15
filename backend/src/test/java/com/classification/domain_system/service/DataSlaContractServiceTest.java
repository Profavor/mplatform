package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataSlaContractDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataSlaContractServiceTest {

    private DataSlaContractService slaService;

    @BeforeEach
    void setUp() {
        slaService = new DataSlaContractService();
    }

    @Test
    @DisplayName("getSlaContracts: 엔터프라이즈 데이터 SLA 서비스 수준 협약 및 실시간 준수율 산출")
    void testGetSlaContracts() {
        DataSlaContractDto.DataSlaReport res = slaService.getSlaContracts();

        assertThat(res).isNotNull();
        assertThat(res.getTotalContracts()).isEqualTo(3);
        assertThat(res.getOverallComplianceRate()).isEqualTo(100.0);
        assertThat(res.getContracts()).hasSize(3);
        assertThat(res.getContracts().get(0).getCurrentLatencyMs()).isLessThan(res.getContracts().get(0).getLatencyThresholdMs());
    }
}
