package com.classification.domain_system.service.opensearch;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordDocument;
import com.classification.domain_system.repository.RecordSearchRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.HashMap;

@Component
public class RecordSyncListener {

    private static RecordSearchRepository searchRepository;

    @Autowired
    public void setSearchRepository(RecordSearchRepository searchRepository) {
        RecordSyncListener.searchRepository = searchRepository;
    }

    @PostPersist
    @PostUpdate
    public void onPostUpdate(Record record) {
        if (searchRepository == null) return;
        
        RecordDocument doc = new RecordDocument();
        doc.setId(record.getId().toString());
        doc.setDomainId(record.getNode() != null && record.getNode().getDomain() != null ? record.getNode().getDomain().getId().toString() : null);
        doc.setNodeId(record.getNode() != null ? record.getNode().getId().toString() : null);
        doc.setStatus(record.getStatus());
        doc.setCreatedAt(record.getCreatedAt());
        doc.setUpdatedAt(record.getUpdatedAt());
        
        // Use searchableData if available, otherwise fallback to data string
        if (record.getSearchableData() != null && !record.getSearchableData().isBlank()) {
            doc.setSearchableData(record.getSearchableData());
        } else if (record.getData() != null) {
            doc.setSearchableData(record.getData());
        }
        
        try {
            if (record.getData() != null) {
                // Ensure data is properly structured and empty date/numeric strings are sanitized
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> rawDataMap = mapper.readValue(record.getData(), Map.class);
                Map<String, Object> sanitizedMap = new HashMap<>();
                if (rawDataMap != null) {
                    for (Map.Entry<String, Object> entry : rawDataMap.entrySet()) {
                        Object val = entry.getValue();
                        if (val instanceof String str && str.trim().isEmpty()) {
                            // Convert empty strings to null to avoid OpenSearch date/numeric parse errors (e.g. SURVEY_DATE: "")
                            sanitizedMap.put(entry.getKey(), null);
                        } else {
                            sanitizedMap.put(entry.getKey(), val);
                        }
                    }
                }
                doc.setData(sanitizedMap);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }

        try {
            searchRepository.save(doc);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecordSyncListener.class)
                .warn("Failed to sync record {} to OpenSearch index (will not abort DB transaction): {}", record.getId(), e.getMessage());
        }
    }

    @PostRemove
    public void onPostRemove(Record record) {
        if (searchRepository == null) return;
        try {
            searchRepository.deleteById(record.getId().toString());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(RecordSyncListener.class)
                .warn("Failed to delete record {} from OpenSearch index: {}", record.getId(), e.getMessage());
        }
    }
}
