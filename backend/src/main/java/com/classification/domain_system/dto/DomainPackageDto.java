package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainPackageDto {

    @Builder.Default
    private String version = "1.0";
    private LocalDateTime exportedAt;
    private String exportedBy;

    private DomainInfo domain;
    @Builder.Default
    private List<AxisInfo> axes = new ArrayList<>();
    @Builder.Default
    private List<NodeInfo> nodes = new ArrayList<>();
    @Builder.Default
    private List<FieldInfo> fields = new ArrayList<>();
    @Builder.Default
    private List<DqRuleInfo> dqRules = new ArrayList<>();
    @Builder.Default
    private List<MatchingRuleInfo> matchingRules = new ArrayList<>();
    @Builder.Default
    private List<WorkflowInfo> workflows = new ArrayList<>();

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DomainInfo {
        private Map<String, String> name;
        private Map<String, String> description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AxisInfo {
        private Map<String, String> name;
        private String axisCode;
        private Boolean isDefault;
        private Integer sortOrder;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class NodeInfo {
        private String nodeKey; // ID 매핑용 임시 키
        private String parentNodeKey;
        private Map<String, String> name;
        private Integer sortOrder;
        private String axisCode;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FieldInfo {
        private String nodeKey;
        private String key;
        private Map<String, String> name;
        private String type;
        private Boolean required;
        private Boolean isEncrypted;
        private String maskingPattern;
        private Integer order;
        private String options;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DqRuleInfo {
        private String fieldKey;
        private String ruleType;
        private String params;
        private String severity;
        private Map<String, String> message;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MatchingRuleInfo {
        private String ruleName;
        private String matchType;
        private String targetFieldKeys;
        private Double similarityThreshold;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WorkflowInfo {
        private String nodeKey;
        private String actionType;
        private String name;
        private String description;
        private String stepsConfig;
        private Boolean isDefault;
        private Boolean isActive;
    }
}
