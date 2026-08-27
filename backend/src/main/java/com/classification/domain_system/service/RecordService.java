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

import java.util.*;

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
    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

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
        return processDataForSave(nodeId, dataJson, null);
    }

    public String processDataForSave(UUID nodeId, String dataJson, String existingDataJson) {
        if (dataJson == null || dataJson.isBlank() || nodeId == null) {
            return dataJson;
        }
        try {
            List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(nodeId);
            if (fields == null || fields.isEmpty()) {
                return dataJson;
            }

            Map<String, Object> dataMap = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> existingMap = null;
            if (existingDataJson != null && !existingDataJson.isBlank()) {
                try {
                    existingMap = objectMapper.readValue(existingDataJson, new TypeReference<Map<String, Object>>() {});
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    log.warn("Could not parse existing data json: {}", e.getMessage());
                }
            }
            boolean modified = false;

            for (FieldDefinition field : fields) {
                if ("EMAIL".equalsIgnoreCase(field.getType()) && field.getKey() != null) {
                    String matchedKey = null;
                    for (String k : dataMap.keySet()) {
                        if (k.equalsIgnoreCase(field.getKey())) {
                            matchedKey = k;
                            break;
                        }
                    }
                    if (matchedKey != null) {
                        Object rawValObj = dataMap.get(matchedKey);
                        if (rawValObj instanceof String rawVal && !rawVal.isBlank() && !rawVal.contains("*")) {
                            if (!rawVal.startsWith("vault:") && !rawVal.startsWith("ENC(") && !EMAIL_PATTERN.matcher(rawVal.trim()).matches()) {
                                throw new com.classification.domain_system.exception.BusinessException(
                                        com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                                        "Invalid email format for field " + field.getKey() + ": " + rawVal
                                );
                            }
                        }
                    }
                }

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
                            // 1. If rawVal contains masking asterisks '*', preserve original ciphertext from existing record
                            if (rawVal.contains("*")) {
                                if (existingMap != null) {
                                    String existingKey = null;
                                    for (String ek : existingMap.keySet()) {
                                        if (ek.equalsIgnoreCase(field.getKey())) {
                                            existingKey = ek;
                                            break;
                                        }
                                    }
                                    if (existingKey != null && existingMap.get(existingKey) != null) {
                                        Object existingVal = existingMap.get(existingKey);
                                        dataMap.put(matchedKey, existingVal);
                                        String existingIdxKey = "_idx_" + field.getKey();
                                        if (existingMap.containsKey(existingIdxKey)) {
                                            dataMap.put(existingIdxKey, existingMap.get(existingIdxKey));
                                        }
                                        String existingMaskKey = "_mask_" + field.getKey();
                                        if (existingMap.containsKey(existingMaskKey)) {
                                            dataMap.put(existingMaskKey, existingMap.get(existingMaskKey));
                                        }
                                        modified = true;
                                    }
                                }
                                continue;
                            }
                            // 2. If rawVal is ALREADY valid ciphertext, do not double encrypt
                            String testDecrypt = fieldEncryptionService.decrypt(rawVal);
                            if (!testDecrypt.equals(rawVal)) {
                                String blindIndex = fieldEncryptionService.generateBlindIndex(testDecrypt);
                                dataMap.put("_idx_" + field.getKey(), blindIndex);
                                String maskKey = "_mask_" + field.getKey();
                                if (!dataMap.containsKey(maskKey) || dataMap.get(maskKey) == null) {
                                    String pattern = field.getMaskingPattern();
                                    String maskVal = (pattern != null && !pattern.isBlank())
                                            ? dataMaskingService.maskByPattern(pattern, testDecrypt)
                                            : (testDecrypt.length() <= 6 ? "******" : testDecrypt.substring(0, 2) + "****" + testDecrypt.substring(testDecrypt.length() - 2));
                                    dataMap.put(maskKey, maskVal);
                                    modified = true;
                                }
                                continue;
                            }
                            // 3. rawVal is new PLAINTEXT typed by user -> encrypt it and generate mask cache!
                            String encrypted = fieldEncryptionService.encrypt(rawVal);
                            String blindIndex = fieldEncryptionService.generateBlindIndex(rawVal);
                            String pattern = field.getMaskingPattern();
                            String maskVal = (pattern != null && !pattern.isBlank())
                                    ? dataMaskingService.maskByPattern(pattern, rawVal)
                                    : (rawVal.length() <= 6 ? "******" : rawVal.substring(0, 2) + "****" + rawVal.substring(rawVal.length() - 2));
                            dataMap.put(matchedKey, encrypted);
                            dataMap.put("_idx_" + field.getKey(), blindIndex);
                            dataMap.put("_mask_" + field.getKey(), maskVal);
                            modified = true;
                        }
                    }
                }
            }

            return modified ? objectMapper.writeValueAsString(dataMap) : dataJson;
        } catch (com.classification.domain_system.exception.EncryptionException e) {
            log.error("Failed to encrypt field data on save for nodeId {}", nodeId, e);
            throw e;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to parse/serialize field data on save for nodeId {}", nodeId, e);
            throw new com.classification.domain_system.exception.BusinessException(
                com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                "Invalid record data JSON: " + e.getMessage()
            );
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

    public List<String> computeChangedFieldKeys(String prevJson, String newJson) {
        if (prevJson == null && newJson == null) return Collections.emptyList();
        if (prevJson == null || prevJson.isBlank()) {
            if (newJson == null || newJson.isBlank()) return Collections.emptyList();
            try {
                Map<String, Object> newMap = objectMapper.readValue(newJson, new TypeReference<Map<String, Object>>() {});
                return newMap.keySet().stream().filter(k -> !k.startsWith("_idx_") && !k.startsWith("_mask_") && !isMetadataKey(k)).toList();
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                return Collections.emptyList();
            }
        }
        if (newJson == null || newJson.isBlank()) {
            try {
                Map<String, Object> prevMap = objectMapper.readValue(prevJson, new TypeReference<Map<String, Object>>() {});
                return prevMap.keySet().stream().filter(k -> !k.startsWith("_idx_") && !k.startsWith("_mask_") && !isMetadataKey(k)).toList();
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                return Collections.emptyList();
            }
        }

        try {
            Map<String, Object> prevMap = objectMapper.readValue(prevJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> newMap = objectMapper.readValue(newJson, new TypeReference<Map<String, Object>>() {});

            Set<String> allKeys = new HashSet<>(prevMap.keySet());
            allKeys.addAll(newMap.keySet());

            List<String> changed = new ArrayList<>();
            for (String key : allKeys) {
                if (key.startsWith("_idx_") || key.startsWith("_mask_") || isMetadataKey(key)) continue;
                Object prevVal = prevMap.get(key);
                Object newVal = newMap.get(key);
                if (!Objects.equals(prevVal, newVal)) {
                    changed.add(key);
                }
            }
            return changed;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Failed to compute changed field keys: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private boolean isMetadataKey(String key) {
        return "id".equals(key) || "createdAt".equals(key) || "updatedAt".equals(key) ||
                "createdBy".equals(key) || "updatedBy".equals(key) || "domainId".equals(key) || "status".equals(key);
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

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Record> getRecords(UUID nodeId, String status, boolean includeChildren, int page, int size, Map<String, String> allParams) {
        List<UUID> targetNodeIds = new ArrayList<>();
        targetNodeIds.add(nodeId);

        if (includeChildren) {
            List<com.classification.domain_system.entity.ClassificationNode> children = nodeRepository.findByParentIdAndIsDeletedFalseOrderByOrderAsc(nodeId);
            targetNodeIds.addAll(children.stream().map(com.classification.domain_system.entity.ClassificationNode::getId).toList());
        }

        Map<String, String> searchParams = new HashMap<>();
        if (allParams != null) {
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("search_")) {
                    searchParams.put(entry.getKey().substring(7), entry.getValue());
                }
            }
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        if (allParams != null) {
            String sortField = allParams.get("sortField");
            String sortOrder = allParams.get("sortOrder");
            if (sortField == null && allParams.containsKey("sort")) {
                String sortParam = allParams.get("sort");
                String[] parts = sortParam.split(",");
                sortField = parts[0];
                if (parts.length > 1) sortOrder = parts[1];
            }
            if (sortField != null && !sortField.isEmpty()) {
                org.springframework.data.domain.Sort.Direction dir = "DESC".equalsIgnoreCase(sortOrder)
                        ? org.springframework.data.domain.Sort.Direction.DESC
                        : org.springframework.data.domain.Sort.Direction.ASC;
                sort = org.springframework.data.domain.Sort.by(dir, sortField);
            }
        }

        Page<Record> records = recordRepository.findDynamicRecords(
                targetNodeIds, status, searchParams, org.springframework.data.domain.PageRequest.of(page, size, sort));
        return prepareRecordsForRead(records);
    }
}

