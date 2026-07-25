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

    private final FieldEncryptionService fieldEncryptionService;
    private final DataMaskingService dataMaskingService;
    private final FieldDefinitionService fieldDefinitionService;
    private final AuthContext authContext;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                if (Boolean.TRUE.equals(field.getIsEncrypted()) && dataMap.containsKey(field.getKey())) {
                    Object rawValObj = dataMap.get(field.getKey());
                    if (rawValObj instanceof String rawVal && !rawVal.isBlank()) {
                        String encrypted = fieldEncryptionService.encrypt(rawVal);
                        String blindIndex = fieldEncryptionService.generateBlindIndex(rawVal);
                        dataMap.put(field.getKey(), encrypted);
                        dataMap.put("_idx_" + field.getKey(), blindIndex);
                        modified = true;
                    }
                }
            }

            return modified ? objectMapper.writeValueAsString(dataMap) : dataJson;
        } catch (Exception e) {
            log.error("Failed to encrypt field data on save for nodeId {}", nodeId, e);
            return dataJson;
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
}
