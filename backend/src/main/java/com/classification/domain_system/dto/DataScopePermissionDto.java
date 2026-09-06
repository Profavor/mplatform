package com.classification.domain_system.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class DataScopePermissionDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private UUID domainId;
        private UUID nodeId; // null이면 도메인 전체
        private String permissionLevel; // READ, WRITE, ADMIN
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private UUID id;
        private String scopeCode; // SCP-########
        private String userId;
        private String username;
        private UUID domainId;
        private Map<String, String> domainName;
        private UUID nodeId;
        private Map<String, String> nodeName;
        private String permissionLevel;
        private LocalDateTime createdAt;
        private String createdBy;
    }
}
