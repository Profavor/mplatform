package com.classification.domain_system.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SchemaImpactAnalysisDto {

    public static class ImpactAnalysisRequest {
        private String changeType; // DELETE_FIELD, MODIFY_FIELD_TYPE, CHANGE_DQ_RULE
        private UUID fieldDefinitionId;
        private UUID dqRuleId;
        private String newFieldType;

        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }

        public UUID getFieldDefinitionId() { return fieldDefinitionId; }
        public void setFieldDefinitionId(UUID fieldDefinitionId) { this.fieldDefinitionId = fieldDefinitionId; }

        public UUID getDqRuleId() { return dqRuleId; }
        public void setDqRuleId(UUID dqRuleId) { this.dqRuleId = dqRuleId; }

        public String getNewFieldType() { return newFieldType; }
        public void setNewFieldType(String newFieldType) { this.newFieldType = newFieldType; }
    }

    public static class ImpactAnalysisResponse {
        private UUID domainId;
        private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
        private long totalAffectedRecords;
        private long expectedDqViolations;
        private List<String> affectedIntegrationChannels = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public UUID getDomainId() { return domainId; }
        public void setDomainId(UUID domainId) { this.domainId = domainId; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public long getTotalAffectedRecords() { return totalAffectedRecords; }
        public void setTotalAffectedRecords(long totalAffectedRecords) { this.totalAffectedRecords = totalAffectedRecords; }

        public long getExpectedDqViolations() { return expectedDqViolations; }
        public void setExpectedDqViolations(long expectedDqViolations) { this.expectedDqViolations = expectedDqViolations; }

        public List<String> getAffectedIntegrationChannels() { return affectedIntegrationChannels; }
        public void setAffectedIntegrationChannels(List<String> affectedIntegrationChannels) { this.affectedIntegrationChannels = affectedIntegrationChannels; }

        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
}
