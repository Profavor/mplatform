package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultiRegionConflictDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiRegionConflictServiceTest {

    private MultiRegionConflictService conflictService;

    @BeforeEach
    void setUp() {
        conflictService = new MultiRegionConflictService();
    }

    @Test
    @DisplayName("getConflictReport: 멀티 리전 분산 동기화 충돌 탐지 및 자율 해소 리포트")
    void testGetConflictReport() {
        MultiRegionConflictDto.RegionSyncReport res = conflictService.getConflictReport();

        assertThat(res).isNotNull();
        assertThat(res.getTotalRegions()).isEqualTo(3);
        assertThat(res.getActiveConflicts()).isEqualTo(0);
        assertThat(res.getAutoResolvedCount()).isEqualTo(2);
        assertThat(res.getConflicts()).hasSize(2);
        assertThat(res.getConflicts().get(0).getResolvedValue()).isNotNull();
    }

    @Test
    @DisplayName("resolveConflict: 특정 충돌 건 수동 지정 해소")
    void testResolveConflict() {
        boolean ok = conflictService.resolveConflict("CONF-KR-US-01", "CUSTOM_VALUE");
        assertThat(ok).isTrue();
    }
}
