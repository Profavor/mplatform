package com.classification.domain_system.dto;

import lombok.Data;
import java.util.UUID;

import java.util.Map;

@Data
public class DomainRequest {
    private Map<String, String> name;
    private Map<String, String> description;
    private UUID identifierFieldId;
    private UUID displayNameFieldId;
    private UUID descriptionFieldId;
    private UUID imageFieldId;
    private String icon;
    private Integer sortOrder;
    private String numberingPattern;
    private Boolean autoDqScanEnabled;
    private String domainType;
    private String specializedCategory;
}
