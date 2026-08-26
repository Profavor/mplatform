package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecializedDomainTemplateDto {
    private String category;
    private Map<String, String> name;
    private Map<String, String> description;
    private String icon;
    private String numberingPattern;
    private Map<String, String> axisName;
    private String axisCode;
    private Map<String, String> rootNodeName;
    private String identifierFieldKey;
    private String displayNameFieldKey;
    private List<FieldTemplateDto> fields;
    private List<DqRuleTemplateDto> dqRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldTemplateDto {
        private String key;
        private Map<String, String> name;
        private Map<String, String> hint;
        private String type; // TEXT, NUMBER, DATE, BOOLEAN, SELECT, EMAIL
        private String unit;
        private Boolean required;
        private Boolean isSearchable;
        private Boolean isFilterable;
        private Boolean isGridVisible;
        private Integer gridWidth;
        private Integer order;
        private String options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DqRuleTemplateDto {
        private String fieldKey;
        private String ruleType; // NOT_NULL, REGEX, RANGE, BUSINESS_NO_CHECKSUM 등
        private String severity; // ERROR, WARNING
        private String params;
        private Map<String, String> message;
    }
}
