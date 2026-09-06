package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class ColumnMaskingPolicyDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID domainId;
        private String fieldKey;
        private String targetType; // ROLE, DEPARTMENT
        private String targetId;
        private String maskingAction; // UNMASK, MASK
        private Boolean isActive;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String policyCode; // POL-########
        private UUID domainId;
        private String fieldKey;
        private String targetType;
        private String targetId;
        private String targetName; // 역할명 또는 부서명
        private String maskingAction;
        private Boolean isActive;
        private String description;
        private LocalDateTime createdAt;
        private String createdBy;
    }
}
