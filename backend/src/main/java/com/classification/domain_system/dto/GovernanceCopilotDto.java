package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

public class GovernanceCopilotDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CopilotMessage {
        private String role; // USER, COPILOT
        private String content;
        private String suggestedAction; // VIEW_DQ, TRIGGER_HEALING, VIEW_SLA
        private String actionTarget;
        private String timestamp;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CopilotChatRequest {
        private String prompt;
        private List<CopilotMessage> history;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CopilotChatResponse {
        private String reply;
        private List<String> suggestedActions;
        private Map<String, String> metricCards;
        private String timestamp;
    }
}
