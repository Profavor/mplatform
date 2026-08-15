package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class BusinessRuleDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessRuleItem {
        private String ruleId;
        private String ruleName;
        private String conditionExpr; // IF condition
        private String validationExpr; // THEN validation
        private String errorMessage;
        private boolean enabled;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViolationItem {
        private String recordCode;
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RuleEvaluationResult {
        private String ruleId;
        private String ruleName;
        private boolean passed;
        private int violationCount;
        private List<ViolationItem> sampleViolations;
    }
}
