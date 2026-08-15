package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultiTenantRouterDto;
import com.classification.domain_system.entity.Department;
import com.classification.domain_system.repository.DepartmentRepository;
import com.classification.domain_system.repository.DomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class MultiTenantRouterServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private MultiTenantRouterService routerService;

    @Test
    @DisplayName("getRoutingRules: 멀티 테넌트 가상 파티셔닝 라우팅 룰 리포트 (DB 동적 연동)")
    void testGetRoutingRules() {
        Department dept = new Department();
        dept.setId(UUID.randomUUID());
        dept.setName("개발팀");

        given(departmentRepository.findAll()).willReturn(List.of(dept));
        given(domainRepository.count()).willReturn(5L);

        MultiTenantRouterDto.TenantRoutingResponse res = routerService.getRoutingRules();

        assertThat(res).isNotNull();
        assertThat(res.getTotalTenants()).isEqualTo(1);
        assertThat(res.getActiveTenants()).isEqualTo(1);
        assertThat(res.getRules()).hasSize(1);
        assertThat(res.getRules().get(0).getTenantCode()).startsWith("DEPT-");
    }

    @Test
    @DisplayName("toggleRule: 특정 테넌트 라우팅 룰 활성화/비활성화 토글")
    void testToggleRule() {
        boolean active = routerService.toggleRule("DEPT-00000000", false);
        assertThat(active).isFalse();
    }
}
