package com.classification.domain_system.service;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final com.classification.domain_system.repository.RecordRepository recordRepository;
    private final com.classification.domain_system.repository.RecordHistoryRepository recordHistoryRepository;
    private final com.classification.domain_system.repository.ClassificationNodeRepository nodeRepository;
    @org.springframework.context.annotation.Lazy
    private final ApprovalService approvalService;
    private final FieldEncryptionService fieldEncryptionService;
    private final DataMaskingService dataMaskingService;
    private final FieldDefinitionService fieldDefinitionService;
    private final AuthContext authContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.transaction.annotation.Transactional
    public com.classification.domain_system.dto.RecordBulkReclassifyResult bulkReclassifyRecords(
            com.classification.domain_system.dto.RecordBulkReclassifyRequest req, String requestedBy) {
        if (req == null || req.getRecordIds() == null || req.getRecordIds().isEmpty()) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_INPUT, "Record IDs are required for bulk reclassification.");
        }
        if (req.getTargetNodeId() == null) {
            throw new com.classification.domain_system.exception.BusinessException(
                    com.classification.domain_system.exception.ErrorCode.INVALID_INPUT, "Target node ID is required.");
        }

        com.classification.domain_system.entity.ClassificationNode targetNode = nodeRepository.findById(req.getTargetNodeId())
                .orElseThrow(() -> new com.classification.domain_system.exception.ResourceNotFoundException("Target classification node not found: " + req.getTargetNodeId()));

        List<com.classification.domain_system.entity.Record> records = recordRepository.findAllById(req.getRecordIds());
        int successCount = 0;
        int failureCount = 0;
        List<String> errorMessages = new java.util.ArrayList<>();

        for (com.classification.domain_system.entity.Record record : records) {
            try {
                com.classification.domain_system.entity.ClassificationNode previousNode = record.getNode();
                record.setNode(targetNode);
                recordRepository.save(record);

                com.classification.domain_system.entity.RecordHistory history = new com.classification.domain_system.entity.RecordHistory();
                history.setRecordId(record.getId());
                history.setChangeType("RECLASSIFY");
                history.setChangedBy(requestedBy != null ? requestedBy : "system");
                history.setSourceSystem("Reclassify [" + (previousNode != null ? previousNode.getName() : "None") + 
                        "] -> [" + targetNode.getName() + "]" + (req.getReason() != null && !req.getReason().isBlank() ? " (" + req.getReason() + ")" : ""));
                history.setPreviousData(record.getData());
                history.setNewData(record.getData());
                history.setVersion(record.getVersion() != null ? record.getVersion() : 1);
                recordHistoryRepository.save(history);

                successCount++;
            } catch (Exception e) {
                failureCount++;
                errorMessages.add("Failed to reclassify record " + record.getId() + ": " + e.getMessage());
                log.error("Error reclassifying record {}", record.getId(), e);
            }
        }

        return com.classification.domain_system.dto.RecordBulkReclassifyResult.builder()
                .totalCount(req.getRecordIds().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .errorMessages(errorMessages)
                .build();
    }

    @org.springframework.transaction.annotation.Transactional
    public com.classification.domain_system.entity.ApprovalRequest rollbackRecord(UUID recordId, Integer targetVersion, String reason, String requestedBy) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new com.classification.domain_system.exception.ResourceNotFoundException("Record not found: " + recordId));

        List<com.classification.domain_system.entity.RecordHistory> histories = recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId);
        com.classification.domain_system.entity.RecordHistory targetHistory = histories.stream()
                .filter(h -> h.getVersion() != null && h.getVersion().equals(targetVersion))
                .findFirst()
                .orElseThrow(() -> new com.classification.domain_system.exception.BusinessException(
                        com.classification.domain_system.exception.ErrorCode.RESOURCE_NOT_FOUND,
                        "Target version not found: " + targetVersion));

        String targetData = targetHistory.getNewData();
        if (targetData == null || targetData.isBlank()) {
            targetData = targetHistory.getPreviousData();
        }

        com.classification.domain_system.dto.RecordRequest req = new com.classification.domain_system.dto.RecordRequest();
        req.setData(targetData);
        req.setRequesterId(requestedBy);
        req.setComment("Rollback to version " + targetVersion + (reason != null && !reason.isBlank() ? " (" + reason + ")" : ""));

        return approvalService.requestRecordUpdate(recordId, req);
    }

    public java.util.Optional<Record> getRecordById(UUID id) {
        return recordRepository.findById(id).map(this::prepareRecordForRead);
    }

    public java.util.Optional<Record> findRawById(UUID id) {
        return recordRepository.findById(id);
    }

    public Page<Record> findDynamicRecordsByDomain(UUID domainId, Map<String, String> searchParams, org.springframework.data.domain.Pageable pageable) {
        Page<Record> records = recordRepository.findDynamicRecordsByDomain(domainId, searchParams, pageable);
        return prepareRecordsForRead(records);
    }

    public String processDataForSave(UUID nodeId, String dataJson) {
        if (dataJson == null || dataJson.isBlank() || nodeId == null) {
            return dataJson;
        }
        try {
            List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(nodeId);
            if (fields == null || fields.isEmpty()) {
                return dataJson;
            }

            Map<String, Object> dataMap = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            boolean modified = false;

            for (FieldDefinition field : fields) {
                if (Boolean.TRUE.equals(field.getIsEncrypted()) && field.getKey() != null) {
                    String matchedKey = null;
                    for (String k : dataMap.keySet()) {
                        if (k.equalsIgnoreCase(field.getKey())) {
                            matchedKey = k;
                            break;
                        }
                    }
                    if (matchedKey != null) {
                        Object rawValObj = dataMap.get(matchedKey);
                        if (rawValObj instanceof String rawVal && !rawVal.isBlank()) {
                            // 1. If rawVal contains masking asterisks '*', do not re-encrypt masked string
                            if (rawVal.contains("*")) {
                                continue;
                            }
                            // 2. If rawVal is ALREADY valid ciphertext, do not double encrypt
                            String testDecrypt = fieldEncryptionService.decrypt(rawVal);
                            if (!testDecrypt.equals(rawVal)) {
                                String blindIndex = fieldEncryptionService.generateBlindIndex(testDecrypt);
                                dataMap.put("_idx_" + field.getKey(), blindIndex);
                                continue;
                            }
                            // 3. rawVal is new PLAINTEXT typed by user -> encrypt it!
                            String encrypted = fieldEncryptionService.encrypt(rawVal);
                            String blindIndex = fieldEncryptionService.generateBlindIndex(rawVal);
                            dataMap.put(matchedKey, encrypted);
                            dataMap.put("_idx_" + field.getKey(), blindIndex);
                            modified = true;
                        }
                    }
                }
            }

            return modified ? objectMapper.writeValueAsString(dataMap) : dataJson;
        } catch (Exception e) {
            log.error("Failed to encrypt field data on save for nodeId {}", nodeId, e);
            throw new RuntimeException("Saving aborted due to encryption failure. nodeId=" + nodeId, e);
        }
    }

    public String processDataForRead(UUID nodeId, String dataJson) {
        if (dataJson == null || dataJson.isBlank() || nodeId == null) {
            return dataJson;
        }
        List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(nodeId);
        boolean canUnmask = authContext != null && authContext.hasPermission("record:unmask");
        return dataMaskingService.maskJsonData(dataJson, fields, canUnmask);
    }

    public String generateSearchableData(UUID nodeId, String dataJson) {
        if (dataJson == null || dataJson.isBlank() || nodeId == null) {
            return dataJson;
        }
        List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(nodeId);
        return dataMaskingService.maskJsonData(dataJson, fields, false);
    }

    public Record prepareRecordForRead(Record record) {
        if (record == null || record.getData() == null || record.getNode() == null) {
            return record;
        }
        String maskedData = processDataForRead(record.getNode().getId(), record.getData());
        record.setData(maskedData);
        return record;
    }

    public Page<Record> prepareRecordsForRead(Page<Record> records) {
        if (records == null) return null;
        records.getContent().forEach(this::prepareRecordForRead);
        return records;
    }

    @org.springframework.transaction.annotation.Transactional
    public int migrateSearchableData() {
        int count = 0;
        List<Record> allRecords = com.classification.domain_system.context.ApplicationContextProvider.getApplicationContext().getBean(com.classification.domain_system.repository.RecordRepository.class).findAll();
        for (Record record : allRecords) {
            if (record.getNode() != null && record.getData() != null) {
                record.setSearchableData(generateSearchableData(record.getNode().getId(), record.getData()));
                count++;
            }
        }
        return count;
    }
}
