package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class PermissionAuditLogDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String logCode; // LOG-########
        private String targetUserId;
        private String targetUsername;
        private String actionType;
        private String resourceType;
        private String targetResourceId;
        private String targetResourceName;
        private String beforeValue;
        private String afterValue;
        private String changedBy;
        private LocalDateTime changedAt;
        private String clientIp;
    }
}
