package com.classification.domain_system.service;

import com.classification.domain_system.dto.AutonomousCleansingDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class AutonomousCleansingServiceTest {

    private AutonomousCleansingService cleansingService;
    private UUID domainId;

    @BeforeEach
    void setUp() {
        cleansingService = new AutonomousCleansingService();
        domainId = UUID.randomUUID();
    }

    @Test
    @DisplayName("getCleansingProposals: 이상치에 대한 통계/사전 기반 자율 정제 추천안 생성")
    void testGetCleansingProposals() {
        AutonomousCleansingDto.CleansingProposalResponse res = cleansingService.getCleansingProposals(domainId);

        assertThat(res).isNotNull();
        assertThat(res.getTotalAnomalies()).isGreaterThan(0);
        assertThat(res.getItems()).isNotEmpty();
        assertThat(res.getItems().get(0).getRecommendedValue()).isNotNull();
        assertThat(res.getItems().get(0).getConfidenceScore()).isGreaterThan(0.8);
    }

    @Test
    @DisplayName("applyCleansing: 선택된 이상치 레코드 자율 정제 일괄 반영")
    void testApplyCleansing() {
        int applied = cleansingService.applyCleansing(domainId, List.of("REC-001", "REC-002"));
        assertThat(applied).isEqualTo(2);
    }
}
