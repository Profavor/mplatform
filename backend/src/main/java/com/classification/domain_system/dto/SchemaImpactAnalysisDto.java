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

    public static class AffectedRecordSample {
        private String recordCode;
        private String nodeName;
        private String fieldValue;
        private String updatedAt;

        public AffectedRecordSample() {}
        public AffectedRecordSample(String recordCode, String nodeName, String fieldValue, String updatedAt) {
            this.recordCode = recordCode;
            this.nodeName = nodeName;
            this.fieldValue = fieldValue;
            this.updatedAt = updatedAt;
        }

        public String getRecordCode() { return recordCode; }
        public void setRecordCode(String recordCode) { this.recordCode = recordCode; }

        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }

        public String getFieldValue() { return fieldValue; }
        public void setFieldValue(String fieldValue) { this.fieldValue = fieldValue; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class ImpactAnalysisResponse {
        private UUID domainId;
        private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
        private long totalAffectedRecords;
        private long expectedDqViolations;
        private String affectedFieldKey;
        private String affectedFieldName;
        private String impactSummary;
        private List<String> affectedIntegrationChannels = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private List<AffectedRecordSample> sampleAffectedRecords = new ArrayList<>();
        private List<String> affectedDqRules = new ArrayList<>();

        public UUID getDomainId() { return domainId; }
        public void setDomainId(UUID domainId) { this.domainId = domainId; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public long getTotalAffectedRecords() { return totalAffectedRecords; }
        public void setTotalAffectedRecords(long totalAffectedRecords) { this.totalAffectedRecords = totalAffectedRecords; }

        public long getExpectedDqViolations() { return expectedDqViolations; }
        public void setExpectedDqViolations(long expectedDqViolations) { this.expectedDqViolations = expectedDqViolations; }

        public String getAffectedFieldKey() { return affectedFieldKey; }
        public void setAffectedFieldKey(String affectedFieldKey) { this.affectedFieldKey = affectedFieldKey; }

        public String getAffectedFieldName() { return affectedFieldName; }
        public void setAffectedFieldName(String affectedFieldName) { this.affectedFieldName = affectedFieldName; }

        public String getImpactSummary() { return impactSummary; }
        public void setImpactSummary(String impactSummary) { this.impactSummary = impactSummary; }

        public List<String> getAffectedIntegrationChannels() { return affectedIntegrationChannels; }
        public void setAffectedIntegrationChannels(List<String> affectedIntegrationChannels) { this.affectedIntegrationChannels = affectedIntegrationChannels; }

        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }

        public List<AffectedRecordSample> getSampleAffectedRecords() { return sampleAffectedRecords; }
        public void setSampleAffectedRecords(List<AffectedRecordSample> sampleAffectedRecords) { this.sampleAffectedRecords = sampleAffectedRecords; }

        public List<String> getAffectedDqRules() { return affectedDqRules; }
        public void setAffectedDqRules(List<String> affectedDqRules) { this.affectedDqRules = affectedDqRules; }
    }
}
