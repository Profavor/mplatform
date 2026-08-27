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
    private List<ClassificationNodeTemplateDto> nodes;
    private List<SectorTemplateDto> sectors;
    private List<FieldTemplateDto> fields;
    private List<DqRuleTemplateDto> dqRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorTemplateDto {
        private String code;
        private Map<String, String> name;
        private Integer order;
        private List<FieldGroupTemplateDto> groups;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldGroupTemplateDto {
        private String code;
        private Map<String, String> name;
        private Integer order;
        private Boolean isDefaultOpen;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassificationNodeTemplateDto {
        private String code;
        private Map<String, String> name;
        private String icon;
        private String parentCode; // null if child of root node
        private Integer order;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldTemplateDto {
        private String key;
        private Map<String, String> name;
        private Map<String, String> hint;
        private String groupCode;
        private String type; // TEXT, NUMBER, DATE, BOOLEAN, SELECT, EMAIL
        private String unit;
        private Boolean required;
        private Boolean isSearchable;
        private Boolean isFilterable;
        private Boolean isGridVisible;
        private Integer gridWidth;
        private Integer tableColumnWidth;
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
