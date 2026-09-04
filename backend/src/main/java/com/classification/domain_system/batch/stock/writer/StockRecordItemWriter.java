package com.classification.domain_system.batch.stock.writer;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class StockRecordItemWriter implements ItemWriter<Record> {

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final String requestedBy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void write(Chunk<? extends Record> chunk) throws Exception {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        for (Record incomingRecord : chunk) {
            try {
                ClassificationNode node = incomingRecord.getNode();
                Domain domain = node != null ? node.getDomain() : null;
                UUID domainId = domain != null ? domain.getId() : null;

                Map<String, Object> incomingMap = new HashMap<>();
                if (incomingRecord.getData() != null && !incomingRecord.getData().isBlank()) {
                    try {
                        incomingMap = objectMapper.readValue(incomingRecord.getData(), new TypeReference<Map<String, Object>>() {});
                    } catch (Exception ex) {
                        log.warn("Failed to parse incoming data: {}", ex.getMessage());
                    }
                }

                String identifierKey = null;
                if (domain != null && domain.getIdentifierFieldId() != null && fieldDefinitionRepository != null) {
                    FieldDefinition idDef = fieldDefinitionRepository.findById(domain.getIdentifierFieldId()).orElse(null);
                    if (idDef != null && idDef.getKey() != null && !idDef.getKey().isBlank()) {
                        identifierKey = idDef.getKey();
                    }
                }
                if (identifierKey == null && domain != null && domain.getIdentifierField() != null && domain.getIdentifierField().getKey() != null) {
                    identifierKey = domain.getIdentifierField().getKey();
                }
                if (identifierKey == null && incomingMap.containsKey("ticker_code")) {
                    identifierKey = "ticker_code";
                }

                String identifierVal = identifierKey != null && incomingMap.get(identifierKey) != null
                        ? String.valueOf(incomingMap.get(identifierKey)).trim()
                        : null;

                List<Record> existing = Collections.emptyList();
                if (domainId != null && identifierKey != null && identifierVal != null && !identifierVal.isBlank()) {
                    existing = recordRepository.findActiveRecordsByDomainAndFieldValue(domainId, identifierKey, identifierVal);
                }

                if (existing != null && !existing.isEmpty()) {
                    // MERGE into existing record
                    Record survivor = existing.get(0);
                    String prevData = survivor.getData();
                    Map<String, Object> survivorMap = new HashMap<>();
                    if (prevData != null && !prevData.isBlank()) {
                        try {
                            survivorMap = objectMapper.readValue(prevData, new TypeReference<Map<String, Object>>() {});
                        } catch (Exception ex) {
                            log.warn("Failed to parse previous data: {}", ex.getMessage());
                        }
                    }

                    for (Map.Entry<String, Object> entry : incomingMap.entrySet()) {
                        if (entry.getValue() != null) {
                            survivorMap.put(entry.getKey(), entry.getValue());
                        }
                    }

                    String mergedDataJson = objectMapper.writeValueAsString(survivorMap);
                    survivor.setData(mergedDataJson);
                    if (incomingRecord.getNode() != null) {
                        survivor.setNode(incomingRecord.getNode());
                    }
                    survivor.setSourceSystem("SPRING_BATCH_STOCK_INGESTION");
                    survivor.setUpdatedAt(LocalDateTime.now());
                    survivor.setVersion((survivor.getVersion() != null ? survivor.getVersion() : 1) + 1);
                    if (incomingRecord.getSearchableData() != null) {
                        survivor.setSearchableData(incomingRecord.getSearchableData());
                    }

                    Record saved = recordRepository.save(survivor);

                    RecordHistory history = new RecordHistory();
                    history.setRecordId(saved.getId());
                    history.setChangeType("BATCH_MERGE");
                    history.setChangedBy(requestedBy != null ? requestedBy : "SPRING_BATCH");
                    history.setSourceSystem("Spring Batch Pipeline [Stock Ingestion Job]");
                    history.setPreviousData(prevData);
                    history.setNewData(mergedDataJson);
                    history.setVersion(saved.getVersion());
                    recordHistoryRepository.save(history);
                } else {
                    // INSERT new record
                    Record saved = recordRepository.save(incomingRecord);

                    RecordHistory history = new RecordHistory();
                    history.setRecordId(saved.getId());
                    history.setChangeType("BATCH_INGEST");
                    history.setChangedBy(requestedBy != null ? requestedBy : "SPRING_BATCH");
                    history.setSourceSystem("Spring Batch Pipeline [Stock Ingestion Job]");
                    history.setNewData(saved.getData());
                    history.setVersion(saved.getVersion() != null ? saved.getVersion() : 1);
                    recordHistoryRepository.save(history);
                }
            } catch (Exception e) {
                log.error("Failed to write stock record in chunk: {}", e.getMessage(), e);
                throw e;
            }
        }
        log.debug("Committed chunk of {} stock records.", chunk.size());
    }
}

