package com.classification.domain_system.service;

import com.classification.domain_system.dto.AnomalyAccessDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnomalyAccessDetectionServiceTest {

    private AnomalyAccessDetectionService anomalyAccessDetectionService;

    @BeforeEach
    void setUp() {
        anomalyAccessDetectionService = new AnomalyAccessDetectionService();
    }

    @Test
    @DisplayName("detectSecurityEvents: 이상 접근 위협 이벤트 탐지 및 점수 산정")
    void testDetectSecurityEvents() {
        AnomalyAccessDto.AnomalyDetectionSummaryResponse res = anomalyAccessDetectionService.detectSecurityEvents();

        assertThat(res).isNotNull();
        assertThat(res.getActiveThreatCount()).isGreaterThan(0);
        assertThat(res.getEvents()).isNotEmpty();
    }

    @Test
    @DisplayName("blockSuspiciousActor: 의심 행위자 즉시 차단")
    void testBlockSuspiciousActor() {
        boolean blocked = anomalyAccessDetectionService.blockSuspiciousActor("guest_user_99");
        assertThat(blocked).isTrue();

        AnomalyAccessDto.AnomalyDetectionSummaryResponse res = anomalyAccessDetectionService.detectSecurityEvents();
        assertThat(res.getEvents().get(0).isBlocked()).isTrue();
    }
}
