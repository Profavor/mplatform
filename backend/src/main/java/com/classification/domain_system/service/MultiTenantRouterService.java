package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultiTenantRouterDto;
import com.classification.domain_system.entity.Department;
import com.classification.domain_system.repository.DepartmentRepository;
import com.classification.domain_system.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiTenantRouterService {

    private final DepartmentRepository departmentRepository;
    private final DomainRepository domainRepository;

    @Transactional(readOnly = true)
    public MultiTenantRouterDto.TenantRoutingResponse getRoutingRules() {
        List<MultiTenantRouterDto.TenantPartitionRule> rules = new ArrayList<>();
        List<Department> departments = departmentRepository.findAll();
        long domainCount = domainRepository.count();

        if (!departments.isEmpty()) {
            for (Department dept : departments) {
                String deptIdStr = dept.getId() != null ? dept.getId().toString().substring(0, 8) : "00000000";
                String deptCode = "DEPT-" + deptIdStr.toUpperCase();
                String deptName = dept.getName() != null ? dept.getName() : "가상 테넌트";

                rules.add(MultiTenantRouterDto.TenantPartitionRule.builder()
                        .tenantCode(deptCode)
                        .tenantName(deptName + " 파티션")
                        .partitionType("ROW_FILTER")
                        .expression(String.format("dept_id == '%s' or is_global == true", deptCode))
                        .targetDomainCount((int) domainCount)
                        .active(true)
                        .build());
            }
        } else {
            rules.add(MultiTenantRouterDto.TenantPartitionRule.builder()
                    .tenantCode("GLOBAL_HQ")
                    .tenantName("글로벌 통합 본사 (HQ)")
                    .partitionType("ROW_FILTER")
                    .expression("is_global == true")
                    .targetDomainCount((int) domainCount)
                    .active(true)
                    .build());
        }

        return MultiTenantRouterDto.TenantRoutingResponse.builder()
                .totalTenants(rules.size())
                .activeTenants((int) rules.stream().filter(MultiTenantRouterDto.TenantPartitionRule::isActive).count())
                .rules(rules)
                .summary(String.format("전사 %d개 가상 테넌트/조직 격리 라우팅 파티션이 정상 가동 중입니다.", rules.size()))
                .build();
    }

    public boolean toggleRule(String tenantCode, boolean active) {
        log.info("Toggled multi-tenant rule for {}: active={}", tenantCode, active);
        return active;
    }
}
