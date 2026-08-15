package com.classification.domain_system.service;

import com.classification.domain_system.dto.VolumeAnomalyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VolumeAnomalyRadarServiceTest {

    private VolumeAnomalyRadarService radarService;

    @BeforeEach
    void setUp() {
        radarService = new VolumeAnomalyRadarService();
    }

    @Test
    @DisplayName("getVolumeRadarData: 실시간 볼륨 스파이크 및 이상치 탐지")
    void testGetVolumeRadarData() {
        VolumeAnomalyDto.VolumeRadarResponse res = radarService.getVolumeRadarData();

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo("SPIKE_DETECTED");
        assertThat(res.getCurrentThroughput()).isGreaterThan(res.getBaselineThroughput());
        assertThat(res.getHistory()).hasSize(6);

        boolean hasSpike = res.getHistory().stream().anyMatch(VolumeAnomalyDto.VolumeDataPoint::isSpike);
        assertThat(hasSpike).isTrue();
    }
}
