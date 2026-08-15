package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

public class DqRemediationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RemediationProposal {
        private UUID recordId;
        private String recordCode;
        private String fieldKey;
        private String fieldName;
        private String currentValue;
        private String proposedValue;
        private String remediationType; // PHONE_FORMAT, BIZ_NO_FORMAT, TRIM_WHITESPACE, EMAIL_LOWERCASE
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RemediationApplyRequest {
        private List<ProposalItem> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProposalItem {
        private UUID recordId;
        private String fieldKey;
        private String newValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RemediationApplyResult {
        private int successCount;
        private int failedCount;
        private String message;
    }
}
