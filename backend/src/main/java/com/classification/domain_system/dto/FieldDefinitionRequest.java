package com.classification.domain_system.dto;

import lombok.Data;
import java.util.UUID;

import java.util.Map;

@Data
public class FieldDefinitionRequest {
    private Map<String, String> name;
    private Map<String, String> hint;
    private UUID fieldGroupId;
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
    private UUID targetNodeId;
    private Boolean isDomainField;
    private String reason;
    private String comment;
}
