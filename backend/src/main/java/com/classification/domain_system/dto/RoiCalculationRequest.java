package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoiCalculationRequest {
    private Integer partnerCount;
    private Integer monthlyInvoiceCount;
    private Long hourlyWage;
    private Double manualAuditHoursMonthly;
    private Long annualRevenue;
}
