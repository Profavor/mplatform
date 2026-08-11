package com.classification.domain_system.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(indexName = "records")
public class RecordDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String referenceCode;

    @Field(type = FieldType.Keyword)
    private String domainId;

    @Field(type = FieldType.Keyword)
    private String nodeId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    // Use Object to hold dynamic map data. 
    // We can query inside this object using dot notation (e.g., data.productName)
    @Field(type = FieldType.Object)
    private Map<String, Object> data;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchableData;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }

    public String getDomainId() { return domainId; }
    public void setDomainId(String domainId) { this.domainId = domainId; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    
    public String getSearchableData() { return searchableData; }
    public void setSearchableData(String searchableData) { this.searchableData = searchableData; }
}
