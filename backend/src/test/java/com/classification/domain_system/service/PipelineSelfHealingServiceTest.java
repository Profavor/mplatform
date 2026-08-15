package com.classification.domain_system.service;

import com.classification.domain_system.dto.PipelineSelfHealingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PipelineSelfHealingServiceTest {

    private PipelineSelfHealingService healingService;

    @BeforeEach
    void setUp() {
        healingService = new PipelineSelfHealingService();
    }

    @Test
    @DisplayName("getHealingReport: AI 기반 데이터 파이프라인 장애 진단 및 자율 복구 리포트")
    void testGetHealingReport() {
        PipelineSelfHealingDto.PipelineHealingReport res = healingService.getHealingReport();

        assertThat(res).isNotNull();
        assertThat(res.getTotalIncidents()).isEqualTo(3);
        assertThat(res.getHealingSuccessRate()).isEqualTo(100.0);
        assertThat(res.getActions()).hasSize(3);
        assertThat(res.getActions().get(0).getRecoveredCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("triggerHealing: 특정 파이프라인 채널 자율 복구 실행")
    void testTriggerHealing() {
        boolean ok = healingService.triggerHealing("SAP ERP");
        assertThat(ok).isTrue();
    }
}
