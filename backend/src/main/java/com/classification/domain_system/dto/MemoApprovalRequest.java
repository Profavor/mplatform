package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoApprovalRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String comment;

    @NotEmpty(message = "At least one approval step is required")
    @Builder.Default
    private List<MemoStepItem> steps = new ArrayList<>();

    @Builder.Default
    private List<String> observerIds = new ArrayList<>();

    @Builder.Default
    private List<MemoAttachmentItem> attachments = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoStepItem {
        private Integer stepOrder;
        private String stepType; // APPROVAL, CONSENSUS
        private String assigneeId;
        private String assigneeRole;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoAttachmentItem {
        private String fileName;
        private Long fileSize;
        private String downloadUrl;
    }
}
