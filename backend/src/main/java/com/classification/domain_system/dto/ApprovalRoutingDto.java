package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ApprovalRoutingDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateCreateRequest {
        private String templateName;
        private UUID domainId;
        private String conditionField;
        private String conditionOperator; // EQUALS, CONTAINS, GTE, DEFAULT
        private String conditionValue;
        private List<ApproverStepDto> steps;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateResponse {
        private UUID id;
        private String templateName;
        private UUID domainId;
        private String conditionField;
        private String conditionOperator;
        private String conditionValue;
        private List<ApproverStepDto> steps;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproverStepDto {
        private int stepOrder;
        private String requiredRole; // e.g. ROLE_DEPT_HEAD, ROLE_SECURITY_ADMIN, ROLE_EXECUTIVE
        private String stepName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvaluateRouteRequest {
        private UUID domainId;
        private Map<String, Object> recordData;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EvaluateRouteResponse {
        private String matchedTemplateName;
        private List<ApproverStepDto> dynamicSteps;
        private String matchReason;
    }
}
