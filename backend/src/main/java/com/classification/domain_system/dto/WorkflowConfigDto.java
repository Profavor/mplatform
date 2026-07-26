package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowConfigDto {

    @Builder.Default
    private List<WorkflowPermissionDto> permissions = new ArrayList<>();

    @Builder.Default
    private List<ApprovalStepConfigDto> approvalLine = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkflowPermissionDto {
        private String targetType; // USER, ROLE
        private String targetId;
        private String targetRole;
        @Builder.Default
        private List<String> allowedActions = new ArrayList<>(); // CREATE, READ, UPDATE, DELETE
        @Builder.Default
        private List<String> editableFields = new ArrayList<>();
        @Builder.Default
        private List<String> readOnlyFields = new ArrayList<>();
        @Builder.Default
        private List<String> hiddenFields = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalStepConfigDto {
        private int stepOrder;
        private String stepName;
        private String assigneeType; // USER, ROLE
        private String assigneeId;
        private String assigneeRole;
        private String stepType;
        @Builder.Default
        private String approvalMode = "ANY"; // ANY, ALL
    }
}
