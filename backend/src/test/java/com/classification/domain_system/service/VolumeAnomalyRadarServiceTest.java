package com.classification.domain_system.service;

import com.classification.domain_system.dto.VolumeAnomalyDto;
import com.classification.domain_system.repository.IntegrationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class VolumeAnomalyRadarServiceTest {

    @Mock
    private IntegrationLogRepository logRepository;

    @InjectMocks
    private VolumeAnomalyRadarService radarService;

    @BeforeEach
    void setUp() {
        given(logRepository.findAll()).willReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("getVolumeRadarData: 실시간 트래픽 급증(Spike) 탐지 및 Z-Score 레이더 리포트 (DB 동적 연동)")
    void testGetVolumeRadarData() {
        VolumeAnomalyDto.VolumeRadarResponse res = radarService.getVolumeRadarData();

        assertThat(res).isNotNull();
        assertThat(res.getHistory()).hasSize(6);
        assertThat(res.getStatus()).isEqualTo("NORMAL");
    }
}
