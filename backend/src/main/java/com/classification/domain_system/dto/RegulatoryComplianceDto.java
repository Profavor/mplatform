package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class RegulatoryComplianceDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplianceCheckItem {
        private String framework; // ISMS-P, PIPA, GDPR
        private String controlCode;
        private String controlTitle;
        private String status; // PASS, WARNING, FAIL
        private String evidence;
        private String remediation;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ComplianceAuditReport {
        private int overallScore; // 0 ~ 100
        private int passedCount;
        private int warningCount;
        private int failedCount;
        private String certificationReadiness; // READY, NEEDS_REVIEW, CRITICAL
        private List<ComplianceCheckItem> items;
        private String summary;
    }
}
