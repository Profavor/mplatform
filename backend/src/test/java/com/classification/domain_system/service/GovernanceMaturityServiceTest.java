package com.classification.domain_system.service;

import com.classification.domain_system.dto.GovernanceMaturityDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GovernanceMaturityServiceTest {

    private GovernanceMaturityService maturityService;

    @BeforeEach
    void setUp() {
        maturityService = new GovernanceMaturityService();
    }

    @Test
    @DisplayName("evaluateMaturity: 마스터 데이터 거버넌스 성숙도 5대 차원 평가 및 KPI 산출")
    void testEvaluateMaturity() {
        GovernanceMaturityDto.GovernanceMaturityReport res = maturityService.evaluateMaturity();

        assertThat(res).isNotNull();
        assertThat(res.getOverallLevel()).contains("Level 4");
        assertThat(res.getOverallScore()).isGreaterThan(90);
        assertThat(res.getDimensions()).hasSize(5);
        assertThat(res.getKpiSummary()).containsKeys("completeness", "timeliness", "consistency", "validity");
    }
}
