package com.classification.domain_system.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DqRuleResponse {
    private UUID id;
    private UUID fieldDefinitionId;
    private String fieldKey;
    private Map<String, String> fieldName;
    private UUID domainId;
    private UUID nodeId;
    private String ruleType;
    private String severity;
    private String params;
    private Map<String, String> message;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
