package com.classification.domain_system.service;

import com.classification.domain_system.dto.MasterOrchestratorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MasterOrchestratorServiceTest {

    private MasterOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        orchestratorService = new MasterOrchestratorService();
    }

    @Test
    @DisplayName("getOrchestratorStatus: 전사 50대 마스터 데이터 거버넌스 핵심 기능 통합 관제")
    void testGetOrchestratorStatus() {
        MasterOrchestratorDto.MasterOrchestratorSummary res = orchestratorService.getOrchestratorStatus();

        assertThat(res).isNotNull();
        assertThat(res.getTotalFeatures()).isEqualTo(50);
        assertThat(res.getHealthyFeatures()).isEqualTo(50);
        assertThat(res.getSystemMaturityLevel()).contains("Level 5");
        assertThat(res.getModules()).hasSize(50);
        assertThat(res.getCategoryDistribution()).containsKeys(
                "DQ_QUALITY", "SECURITY_COMPLIANCE", "WORKFLOW_APPROVAL",
                "INTEGRATION_PIPELINE", "SCHEMA_LIFECYCLE", "AI_INNOVATION"
        );
    }
}
