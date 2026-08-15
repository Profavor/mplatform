package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultiTenantRouterDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiTenantRouterServiceTest {

    private MultiTenantRouterService routerService;

    @BeforeEach
    void setUp() {
        routerService = new MultiTenantRouterService();
    }

    @Test
    @DisplayName("getRoutingRules: 전사 가상 멀티 테넌트 파티션 라우팅 규칙 조회")
    void testGetRoutingRules() {
        MultiTenantRouterDto.TenantRoutingResponse res = routerService.getRoutingRules();

        assertThat(res).isNotNull();
        assertThat(res.getTotalTenants()).isEqualTo(3);
        assertThat(res.getActiveTenants()).isEqualTo(3);
        assertThat(res.getRules()).hasSize(3);
        assertThat(res.getRules().get(0).getTenantCode()).isEqualTo("HQ_KR");
    }

    @Test
    @DisplayName("toggleRule: 특정 테넌트 규칙 활성/비활성 상태 변경")
    void testToggleRule() {
        boolean toggled = routerService.toggleRule("SUB_US", false);
        assertThat(toggled).isFalse();
    }
}
