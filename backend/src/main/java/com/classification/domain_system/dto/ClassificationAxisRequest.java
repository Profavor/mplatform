package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ClassificationAxisRequest {
    private String axisCode;
    private Map<String, String> name;
    private String description;
    private Boolean isDefault;
    private Integer sortOrder;
}
