package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordBatchUpsertItemResult;
import com.classification.domain_system.dto.RecordBatchUpsertRequest;
import com.classification.domain_system.dto.RecordBatchUpsertResponse;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.entity.enums.RecordStatus;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordBatchUpsertService {

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final RecordService recordService;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private RecordIndexService recordIndexService;

    @Transactional
    public RecordBatchUpsertResponse batchUpsertRecords(UUID nodeId, RecordBatchUpsertRequest request, String requestedBy) {
        long startTime = System.currentTimeMillis();

        if (nodeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Node ID is required.");
        }
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Classification node not found: " + nodeId));

        List<RecordBatchUpsertRequest.Item> incomingRecords = (request != null && request.getRecords() != null)
                ? request.getRecords()
                : Collections.emptyList();

        if (incomingRecords.isEmpty()) {
            return RecordBatchUpsertResponse.builder()
                    .totalCount(0)
                    .createdCount(0)
                    .updatedCount(0)
                    .failedCount(0)
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .items(Collections.emptyList())
                    .build();
        }

        // 1. Determine effective business key (default "PRODUCT_ID")
        String businessKey = resolveBusinessKey(node, request);

        String effectiveSourceSystem = (request != null && request.getSourceSystem() != null && !request.getSourceSystem().isBlank())
                ? request.getSourceSystem()
                : "cartbom";

        boolean autoApprove = request == null || request.getAutoApprove() == null || Boolean.TRUE.equals(request.getAutoApprove());
        String finalStatus = autoApprove ? RecordStatus.ACTIVE.name() : RecordStatus.PENDING_APPROVAL.name();
        String actor = (requestedBy != null && !requestedBy.isBlank()) ? requestedBy : "system-sync";

        // 2. Pre-parse items and extract businessKey values
        List<ParsedItem> parsedItems = new ArrayList<>(incomingRecords.size());
        Set<String> distinctKeyValues = new HashSet<>();

        for (int i = 0; i < incomingRecords.size(); i++) {
            RecordBatchUpsertRequest.Item req = incomingRecords.get(i);
            try {
                if (req == null || req.getEffectiveData() == null) {
                    parsedItems.add(new ParsedItem(i, null, null, "Payload data is empty"));
                    continue;
                }
                Object rawData = req.getEffectiveData();
                Map<String, Object> dataMap;
                if (rawData instanceof Map<?, ?> map) {
                    dataMap = new HashMap<>();
                    map.forEach((k, v) -> dataMap.put(String.valueOf(k), v));
                } else if (rawData instanceof String str) {
                    if (str.isBlank()) {
                        parsedItems.add(new ParsedItem(i, null, null, "Payload data is empty"));
                        continue;
                    }
                    dataMap = objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {});
                } else {
                    dataMap = objectMapper.convertValue(rawData, new TypeReference<Map<String, Object>>() {});
                }

                String keyVal = extractBusinessKeyValue(dataMap, businessKey);
                if (keyVal == null || keyVal.isBlank()) {
                    parsedItems.add(new ParsedItem(i, null, dataMap, "Missing business key: " + businessKey));
                    continue;
                }
                parsedItems.add(new ParsedItem(i, keyVal, dataMap, null));
                distinctKeyValues.add(keyVal);
            } catch (Exception e) {
                log.warn("[BatchUpsert] Error parsing item index {}: {}", i, e.getMessage());
                parsedItems.add(new ParsedItem(i, null, null, "Invalid JSON data: " + e.getMessage()));
            }
        }

        // 3. Batch query existing records by businessKey
        Map<String, Record> existingRecordsByKey = new HashMap<>();
        if (!distinctKeyValues.isEmpty()) {
            List<Record> foundRecords = recordRepository.findActiveRecordsByNodeAndFieldValues(
                    nodeId, businessKey, new ArrayList<>(distinctKeyValues));
            for (Record rec : foundRecords) {
                try {
                    Map<String, Object> dataMap = objectMapper.readValue(rec.getData(), new TypeReference<Map<String, Object>>() {});
                    String val = extractBusinessKeyValue(dataMap, businessKey);
                    if (val != null) {
                        existingRecordsByKey.put(val, rec);
                    }
                } catch (Exception ignored) {}
            }
        }

        // 4. Process each item (Insert or Update with idempotency)
        Set<Record> recordsToSave = new LinkedHashSet<>();
        List<PendingOp> pendingOps = new ArrayList<>();
        List<RecordBatchUpsertItemResult> itemResults = new ArrayList<>();

        int createdCount = 0;
        int updatedCount = 0;
        int failedCount = 0;

        for (ParsedItem item : parsedItems) {
            if (item.errorMessage != null) {
                failedCount++;
                itemResults.add(RecordBatchUpsertItemResult.builder()
                        .index(item.index)
                        .businessKey(businessKey)
                        .businessKeyValue(item.businessKeyValue)
                        .action("FAILED")
                        .errorMessage(item.errorMessage)
                        .build());
                continue;
            }

            try {
                Record existing = existingRecordsByKey.get(item.businessKeyValue);
                if (existing != null) {
                    // --- UPDATE ---
                    String prevData = existing.getData();
                    Map<String, Object> existingDataMap = objectMapper.readValue(
                            prevData != null ? prevData : "{}", new TypeReference<Map<String, Object>>() {});
                    existingDataMap.putAll(item.dataMap);

                    String mergedJson = objectMapper.writeValueAsString(existingDataMap);
                    String processedJson = recordService.processDataForSave(nodeId, mergedJson, prevData);

                    existing.setData(processedJson);
                    existing.setUpdatedAt(LocalDateTime.now());
                    if (existing.getSourceSystem() == null || existing.getSourceSystem().isBlank()) {
                        existing.setSourceSystem(effectiveSourceSystem);
                    }
                    recordsToSave.add(existing);

                    updatedCount++;
                    pendingOps.add(new PendingOp(item.index, item.businessKeyValue, "UPDATED", existing, prevData, processedJson, existing.getStatus()));
                } else {
                    // --- INSERT ---
                    String rawJson = objectMapper.writeValueAsString(item.dataMap);
                    String processedJson = recordService.processDataForSave(nodeId, rawJson, null);

                    Record newRecord = new Record();
                    newRecord.setNode(node);
                    newRecord.setData(processedJson);
                    newRecord.setStatus(finalStatus);
                    newRecord.setSourceSystem(effectiveSourceSystem);

                    recordsToSave.add(newRecord);
                    // Add to existing map to ensure idempotency within the same batch
                    existingRecordsByKey.put(item.businessKeyValue, newRecord);

                    createdCount++;
                    pendingOps.add(new PendingOp(item.index, item.businessKeyValue, "CREATED", newRecord, null, processedJson, finalStatus));
                }
            } catch (Exception ex) {
                failedCount++;
                log.error("[BatchUpsert] Error processing record with key {}: {}", item.businessKeyValue, ex.getMessage(), ex);
                itemResults.add(RecordBatchUpsertItemResult.builder()
                        .index(item.index)
                        .businessKey(businessKey)
                        .businessKeyValue(item.businessKeyValue)
                        .action("FAILED")
                        .errorMessage(ex.getMessage())
                        .build());
            }
        }

        // 5. Bulk commit records first so generated UUIDs are assigned by JPA
        if (!recordsToSave.isEmpty()) {
            recordRepository.saveAllAndFlush(new ArrayList<>(recordsToSave));
        }

        // 6. Record histories and item results with populated IDs
        List<RecordHistory> historiesToSave = new ArrayList<>(pendingOps.size());
        for (PendingOp op : pendingOps) {
            RecordHistory history = new RecordHistory();
            history.setRecordId(op.record.getId());
            history.setChangeType(op.action);
            history.setChangedBy(actor);
            history.setSourceSystem(effectiveSourceSystem);
            history.setPreviousData(op.prevData);
            history.setNewData(op.newData);
            history.setVersion(op.record.getVersion() != null ? op.record.getVersion() : 1);
            historiesToSave.add(history);

            itemResults.add(RecordBatchUpsertItemResult.builder()
                    .index(op.index)
                    .businessKey(businessKey)
                    .businessKeyValue(op.businessKeyValue)
                    .recordId(op.record.getId())
                    .action(op.action)
                    .status(op.status)
                    .build());
        }

        if (!historiesToSave.isEmpty()) {
            recordHistoryRepository.saveAllAndFlush(historiesToSave);
        }

        itemResults.sort(Comparator.comparingInt(RecordBatchUpsertItemResult::getIndex));

        // 6. OpenSearch indexing (optional background sync)
        if (recordIndexService != null && !recordsToSave.isEmpty()) {
            try {
                for (Record r : recordsToSave) {
                    recordIndexService.indexRecord(r);
                }
            } catch (Exception e) {
                log.warn("[BatchUpsert] OpenSearch index update warning: {}", e.getMessage());
            }
        }

        long executionTimeMs = System.currentTimeMillis() - startTime;
        log.info("[BatchUpsert] Completed batch for node {}: total={}, created={}, updated={}, failed={}, duration={}ms",
                nodeId, incomingRecords.size(), createdCount, updatedCount, failedCount, executionTimeMs);

        return RecordBatchUpsertResponse.builder()
                .totalCount(incomingRecords.size())
                .createdCount(createdCount)
                .updatedCount(updatedCount)
                .failedCount(failedCount)
                .executionTimeMs(executionTimeMs)
                .items(itemResults)
                .build();
    }

    private String resolveBusinessKey(ClassificationNode node, RecordBatchUpsertRequest request) {
        if (request != null && request.getBusinessKey() != null && !request.getBusinessKey().isBlank()) {
            return request.getBusinessKey().trim();
        }
        if (node.getDomain() != null && node.getDomain().getIdentifierFieldId() != null) {
            FieldDefinition idDef = fieldDefinitionRepository.findById(node.getDomain().getIdentifierFieldId()).orElse(null);
            if (idDef != null && idDef.getKey() != null && !idDef.getKey().isBlank()) {
                return idDef.getKey();
            }
        }
        return "PRODUCT_ID";
    }

    private String extractBusinessKeyValue(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        Object val = map.get(key);
        if (val == null) {
            val = map.get(key.toLowerCase());
        }
        if (val == null) {
            val = map.get(key.toUpperCase());
        }
        return val != null ? val.toString().trim() : null;
    }

    private static class ParsedItem {
        final int index;
        final String businessKeyValue;
        final Map<String, Object> dataMap;
        final String errorMessage;

        ParsedItem(int index, String businessKeyValue, Map<String, Object> dataMap, String errorMessage) {
            this.index = index;
            this.businessKeyValue = businessKeyValue;
            this.dataMap = dataMap;
            this.errorMessage = errorMessage;
        }
    }

    private static class PendingOp {
        final int index;
        final String businessKeyValue;
        final String action;
        final Record record;
        final String prevData;
        final String newData;
        final String status;

        PendingOp(int index, String businessKeyValue, String action, Record record, String prevData, String newData, String status) {
            this.index = index;
            this.businessKeyValue = businessKeyValue;
            this.action = action;
            this.record = record;
            this.prevData = prevData;
            this.newData = newData;
            this.status = status;
        }
    }
}
