package com.classification.domain_system.service;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
public class RecordIndexService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final com.classification.domain_system.repository.RecordRepository recordRepository;

    public RecordIndexService(ElasticsearchOperations elasticsearchOperations, com.classification.domain_system.repository.RecordRepository recordRepository) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.recordRepository = recordRepository;
    }

    @Async
    public void indexRecord(Record record) {
        try {
            RecordDocument doc = new RecordDocument();
            doc.setId(record.getId().toString());
            doc.setDomainId(record.getNode().getDomain().getId().toString());
            
            if (record.getData() != null && !record.getData().isBlank()) {
                Map<String, Object> dataMap = objectMapper.readValue(record.getData(), new TypeReference<Map<String, Object>>() {});
                doc.setData(dataMap);
            }
            
            // Store searchable string for full-text search
            doc.setSearchableData(record.getSearchableData() != null ? record.getSearchableData() : record.getData());
            elasticsearchOperations.save(doc);
            log.debug("Successfully indexed record: {}", record.getId());
        } catch (Exception e) {
            log.warn("Failed to index record {} to OpenSearch. JpaFallback will be used. Error: {}", record.getId(), e.getMessage());
        }
    }

    @org.springframework.context.event.EventListener
    @Async
    public void onMasterDataChanged(com.classification.domain_system.event.MasterDataChangedEvent event) {
        if ("DELETE".equals(event.getChangeType())) {
            deleteRecord(event.getRecordId().toString());
        } else {
            try {
                recordRepository.findById(event.getRecordId()).ifPresent(this::indexRecord);
            } catch (Exception e) {
                log.warn("Failed to process MasterDataChangedEvent for OpenSearch index", e);
            }
        }
    }
    
    @Async
    public void deleteRecord(String recordId) {
        try {
            elasticsearchOperations.delete(recordId, RecordDocument.class);
            log.debug("Successfully deleted record from index: {}", recordId);
        } catch (Exception e) {
            log.warn("Failed to delete record {} from OpenSearch index. Error: {}", recordId, e.getMessage());
        }
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    @Async
    public void syncAllRecordsToOpenSearch() {
        try {
            log.info("Starting background synchronization of all records to OpenSearch...");
            java.util.List<Record> records = recordRepository.findAll();
            int count = 0;
            for (Record record : records) {
                if (record.getNode() != null && record.getNode().getDomain() != null) {
                    indexRecord(record);
                    count++;
                }
            }
            log.info("Completed OpenSearch background sync. Total records indexed: {}", count);
        } catch (Exception e) {
            log.warn("OpenSearch background sync skipped or failed: {}", e.getMessage());
        }
    }
}
