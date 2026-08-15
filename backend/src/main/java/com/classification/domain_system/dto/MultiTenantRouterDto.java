package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class MultiTenantRouterDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TenantPartitionRule {
        private String tenantCode; // HQ_KR, SUB_US, SUB_VN
        private String tenantName;
        private String partitionType; // ROW_FILTER, COLUMN_MASK, READ_ONLY
        private String expression;
        private int targetDomainCount;
        private boolean active;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TenantRoutingResponse {
        private int totalTenants;
        private int activeTenants;
        private List<TenantPartitionRule> rules;
        private String summary;
    }
}
