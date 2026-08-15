package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class WorkspaceWidgetDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WidgetItem {
        private String widgetId;
        private String title;
        private String description;
        private String category; // GOVERNANCE, QUALITY, WORKFLOW, INFRA
        private boolean enabled;
        private int orderIndex;
        private int gridSpan; // 1 or 2
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveWorkspaceWidgetsRequest {
        private List<WidgetItem> widgets;
    }
}
