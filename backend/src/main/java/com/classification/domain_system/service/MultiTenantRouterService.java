package com.classification.domain_system.service;

import com.classification.domain_system.dto.MultiTenantRouterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiTenantRouterService {

    public MultiTenantRouterDto.TenantRoutingResponse getRoutingRules() {
        List<MultiTenantRouterDto.TenantPartitionRule> rules = new ArrayList<>();

        rules.add(MultiTenantRouterDto.TenantPartitionRule.builder()
                .tenantCode("HQ_KR")
                .tenantName("한국 본사 (HQ)")
                .partitionType("ROW_FILTER")
                .expression("country_code == 'KR' or is_global == true")
                .targetDomainCount(12)
                .active(true)
                .build());

        rules.add(MultiTenantRouterDto.TenantPartitionRule.builder()
                .tenantCode("SUB_US")
                .tenantName("미국 법인 (North America)")
                .partitionType("ROW_FILTER")
                .expression("country_code == 'US' and pii_mask == true")
                .targetDomainCount(8)
                .active(true)
                .build());

        rules.add(MultiTenantRouterDto.TenantPartitionRule.builder()
                .tenantCode("SUB_VN")
                .tenantName("베트남 생산법인 (APAC Production)")
                .partitionType("COLUMN_MASK")
                .expression("exclude_fields('cost_price', 'internal_margin')")
                .targetDomainCount(5)
                .active(true)
                .build());

        return MultiTenantRouterDto.TenantRoutingResponse.builder()
                .totalTenants(rules.size())
                .activeTenants((int) rules.stream().filter(MultiTenantRouterDto.TenantPartitionRule::isActive).count())
                .rules(rules)
                .summary("전사 3대 가상 테넌트(본사/미국/베트남) 격리 라우팅 파티션이 정상 가동 중입니다.")
                .build();
    }

    public boolean toggleRule(String tenantCode, boolean active) {
        log.info("Toggled multi-tenant rule for {}: active={}", tenantCode, active);
        return active;
    }
}
