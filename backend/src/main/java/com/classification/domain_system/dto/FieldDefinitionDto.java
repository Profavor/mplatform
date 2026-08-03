package com.classification.domain_system.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class FieldDefinitionDto {
    private UUID id;
    private UUID domainId;
    private UUID definedAtNodeId;
    private Map<String, String> name;
    private Map<String, String> hint;
    private FieldGroupDto fieldGroup;
    private String key;
    private String type;
    private String unit;
    private String options;
    private Boolean required;
    private String defaultValue;
    private Integer order;
    private Integer gridWidth;
    private Integer tableColumnWidth;
    private Boolean isHighlighted;
    private Boolean isMultiValue;
    private Boolean isTable;
    private Boolean isEncrypted;
    private Boolean isSearchable;
    private Boolean isReadOnly;
    private Boolean isImmutable;
    private Boolean isHidden;
    private String maskingPattern;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
