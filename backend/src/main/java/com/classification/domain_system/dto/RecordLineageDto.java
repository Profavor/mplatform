package com.classification.domain_system.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordLineageDto {

    public static class MappingRuleSummary {
        private String sourceField;
        private String targetField;
        private String expression;

        public MappingRuleSummary() {}
        public MappingRuleSummary(String sourceField, String targetField, String expression) {
            this.sourceField = sourceField;
            this.targetField = targetField;
            this.expression = expression;
        }

        public String getSourceField() { return sourceField; }
        public void setSourceField(String sourceField) { this.sourceField = sourceField; }
        public String getTargetField() { return targetField; }
        public void setTargetField(String targetField) { this.targetField = targetField; }
        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
    }

    public static class PipelineStageSummary {
        private String stage;
        private String label;
        private int order;
        private int nodeCount;
        private String status; // HEALTHY, WARNING, ERROR

        public PipelineStageSummary() {}
        public PipelineStageSummary(String stage, String label, int order, int nodeCount, String status) {
            this.stage = stage;
            this.label = label;
            this.order = order;
            this.nodeCount = nodeCount;
            this.status = status;
        }

        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
        public int getNodeCount() { return nodeCount; }
        public void setNodeCount(int nodeCount) { this.nodeCount = nodeCount; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class LineageNode {
        private String id;
        private String label;
        private String type; // SOURCE, INBOUND, RECORD, RECORD_VERSION, OUTBOUND, CONSUMER
        private String stage; // SOURCE, INBOUND_PIPELINE, MASTER_RECORD, OUTBOUND_PIPELINE, DOWNSTREAM_CONSUMER
        private String healthStatus = "HEALTHY"; // HEALTHY, WARNING, ERROR
        private String anomalyReason;
        private String timestamp;

        private List<MappingRuleSummary> mappingRules = new ArrayList<>();
        private Map<String, Object> metrics = new HashMap<>();
        private Map<String, Object> details = new HashMap<>();

        public LineageNode() {}

        public LineageNode(String id, String label, String type, String timestamp) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.timestamp = timestamp;
        }

        public LineageNode(String id, String label, String type, String stage, String timestamp) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.stage = stage;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }

        public String getHealthStatus() { return healthStatus; }
        public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

        public String getAnomalyReason() { return anomalyReason; }
        public void setAnomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public List<MappingRuleSummary> getMappingRules() { return mappingRules; }
        public void setMappingRules(List<MappingRuleSummary> mappingRules) { this.mappingRules = mappingRules; }

        public Map<String, Object> getMetrics() { return metrics; }
        public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
    }

    public static class LineageEdge {
        private String source;
        private String target;
        private String relationship;

        public LineageEdge() {}

        public LineageEdge(String source, String target, String relationship) {
            this.source = source;
            this.target = target;
            this.relationship = relationship;
        }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
    }

    public static class RecordLineageResponse {
        private UUID recordId;
        private String recordCode;
        private Object recordNameObj;
        private String empNo;
        private List<LineageNode> nodes = new ArrayList<>();
        private List<LineageEdge> edges = new ArrayList<>();
        private List<PipelineStageSummary> stages = new ArrayList<>();
        private List<Map<String, Object>> channelConsumption = new ArrayList<>();
        private Map<String, Map<String, String>> fieldLabels = new HashMap<>();

        public RecordLineageResponse() {}

        public RecordLineageResponse(UUID recordId, String recordCode) {
            this.recordId = recordId;
            this.recordCode = recordCode;
        }

        public UUID getRecordId() { return recordId; }
        public void setRecordId(UUID recordId) { this.recordId = recordId; }

        public String getRecordCode() { return recordCode; }
        public void setRecordCode(String recordCode) { this.recordCode = recordCode; }

        public Object getRecordNameObj() { return recordNameObj; }
        public void setRecordNameObj(Object recordNameObj) { this.recordNameObj = recordNameObj; }

        public String getEmpNo() { return empNo; }
        public void setEmpNo(String empNo) { this.empNo = empNo; }

        public List<LineageNode> getNodes() { return nodes; }
        public void setNodes(List<LineageNode> nodes) { this.nodes = nodes; }

        public List<LineageEdge> getEdges() { return edges; }
        public void setEdges(List<LineageEdge> edges) { this.edges = edges; }

        public List<PipelineStageSummary> getStages() { return stages; }
        public void setStages(List<PipelineStageSummary> stages) { this.stages = stages; }

        public List<Map<String, Object>> getChannelConsumption() { return channelConsumption; }
        public void setChannelConsumption(List<Map<String, Object>> channelConsumption) { this.channelConsumption = channelConsumption; }

        public Map<String, Map<String, String>> getFieldLabels() { return fieldLabels; }
        public void setFieldLabels(Map<String, Map<String, String>> fieldLabels) { this.fieldLabels = fieldLabels; }
    }

    public static class DomainLineageResponse {
        private UUID domainId;
        private String domainName;
        private List<LineageNode> nodes = new ArrayList<>();
        private List<LineageEdge> edges = new ArrayList<>();

        public DomainLineageResponse() {}

        public DomainLineageResponse(UUID domainId, String domainName) {
            this.domainId = domainId;
            this.domainName = domainName;
        }

        public UUID getDomainId() { return domainId; }
        public void setDomainId(UUID domainId) { this.domainId = domainId; }

        public String getDomainName() { return domainName; }
        public void setDomainName(String domainName) { this.domainName = domainName; }

        public List<LineageNode> getNodes() { return nodes; }
        public void setNodes(List<LineageNode> nodes) { this.nodes = nodes; }

        public List<LineageEdge> getEdges() { return edges; }
        public void setEdges(List<LineageEdge> edges) { this.edges = edges; }
    }
}
