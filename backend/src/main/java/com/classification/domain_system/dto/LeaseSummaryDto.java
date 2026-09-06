package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaseSummaryDto {
    private Boolean hasLeaseDomain;
    private UUID domainId;
    private String domainName;
    private long totalContracts;
    private long expiringWithin30Days;
    private long expiredContracts;
    private long overdueCount;
    private long highDebtRatioCount;
    private long totalDepositAmount;
    private long totalMonthlyRent;
    private List<UrgentAlertDto> urgentAlerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrgentAlertDto {
        private UUID recordId;
        private String contractNo;
        private String buildingName;
        private String unitNumber;
        private String tenantName;
        private String tenantContact;
        private String endDate;
        private Long daysRemaining;
        private String riskType; // EXPIRING_SOON, OVERDUE, HIGH_DEBT
        private Double debtRatio;
        private Long monthlyRent;
        private Long depositAmount;
    }
}
