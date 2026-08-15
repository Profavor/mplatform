package com.classification.domain_system.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordLineageDto {

    public static class LineageNode {
        private String id;
        private String label;
        private String type; // SOURCE, RECORD, RECORD_VERSION, OUTBOUND
        private String timestamp;

        private Map<String, Object> details = new HashMap<>();

        public LineageNode() {}

        public LineageNode(String id, String label, String type, String timestamp) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.timestamp = timestamp;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

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
