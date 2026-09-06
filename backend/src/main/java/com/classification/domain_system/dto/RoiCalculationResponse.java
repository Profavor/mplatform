package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoiCalculationResponse {
    private Long monthlyTotalSavings;
    private Long annualTotalSavings;
    private Double monthlyHoursSaved;
    private Double annualHoursSaved;

    private Long starterPlanMonthlyFee;
    private Double starterRoiMultiplier;

    private Long proPlanMonthlyFee;
    private Double proRoiMultiplier;

    private Integer paybackDays;

    private List<SavingsBreakdownItem> breakdownItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavingsBreakdownItem {
        private String categoryKey;
        private Long monthlySavings;
        private Double monthlyHoursSaved;
        private String featureKey;
    }
}
