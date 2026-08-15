package com.classification.domain_system.service;

import com.classification.domain_system.dto.SystemDiagnosticsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalSystemDiagnosticsServiceTest {

    private GlobalSystemDiagnosticsService diagnosticsService;

    @BeforeEach
    void setUp() {
        diagnosticsService = new GlobalSystemDiagnosticsService();
    }

    @Test
    @DisplayName("diagnoseSystem: 전사 인프라 컴포넌트 헬스체크 및 레이턴시 진단")
    void testDiagnoseSystem() {
        SystemDiagnosticsDto.GlobalSystemDiagnosticsResponse res = diagnosticsService.diagnoseSystem();

        assertThat(res).isNotNull();
        assertThat(res.getOverallStatus()).isEqualTo("HEALTHY");
        assertThat(res.getComponents()).hasSize(5);
        assertThat(res.getAverageLatencyMs()).isGreaterThanOrEqualTo(0.0);
    }
}
