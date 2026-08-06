package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveDataAccessLogDto {
    private UUID id;
    private String userId;
    private String userDisplayName;
    private String targetType;
    private UUID targetId;
    private String formattedTargetId;
    private String domainName;
    private String classificationName;
    private String idAttribute;
    private String nameAttribute;
    private String fieldKeys;
    private String formattedFieldLabels;
    private String accessReason;
    private String ipAddress;
    private LocalDateTime accessedAt;
}
