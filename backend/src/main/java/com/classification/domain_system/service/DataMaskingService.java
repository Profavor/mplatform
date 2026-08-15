package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataMaskingDto;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class DataMaskingService {

    private final FieldEncryptionService fieldEncryptionService;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataMaskingService(FieldEncryptionService fieldEncryptionService) {
        this.fieldEncryptionService = fieldEncryptionService;
        this.recordRepository = null;
    }

    @Autowired
    public DataMaskingService(FieldEncryptionService fieldEncryptionService, RecordRepository recordRepository) {
        this.fieldEncryptionService = fieldEncryptionService;
        this.recordRepository = recordRepository;
    }

    @Transactional(readOnly = true)
    public DataMaskingDto.MaskedDataResponse getMaskedRecord(UUID recordId, boolean hasUnmaskPermission) {
        if (recordRepository == null) {
            throw new IllegalStateException("RecordRepository is not configured");
        }
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found: " + recordId));

        Map<String, Object> originalData = parseData(record.getData());
        Map<String, Object> maskedData = new LinkedHashMap<>();
        int maskedCount = 0;

        for (Map.Entry<String, Object> entry : originalData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                maskedData.put(key, null);
                continue;
            }

            if (hasUnmaskPermission) {
                maskedData.put(key, value);
            } else {
                String valStr = String.valueOf(value);
                String maskedVal = maskValue(key, valStr);
                if (!valStr.equals(maskedVal)) {
                    maskedCount++;
                }
                maskedData.put(key, maskedVal);
            }
        }

        String recordCode = "REC-" + record.getId().toString().substring(0, 8);

        return DataMaskingDto.MaskedDataResponse.builder()
                .recordId(record.getId())
                .recordCode(recordCode)
                .originalData(hasUnmaskPermission ? originalData : Collections.emptyMap())
                .maskedData(maskedData)
                .isMasked(!hasUnmaskPermission && maskedCount > 0)
                .maskedFieldCount(maskedCount)
                .build();
    }

    public String maskJsonData(String dataJson, List<FieldDefinition> fields, boolean canUnmask) {
        if (dataJson == null || dataJson.isBlank() || canUnmask) {
            return dataJson;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> masked = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String valStr = entry.getValue() != null ? String.valueOf(entry.getValue()) : null;
                masked.put(entry.getKey(), valStr != null ? maskValue(entry.getKey(), valStr) : null);
            }
            return objectMapper.writeValueAsString(masked);
        } catch (Exception e) {
            return dataJson;
        }
    }

    public String maskChangesJson(String changesJson, List<FieldDefinition> fields, boolean canUnmask) {
        if (changesJson == null || changesJson.isBlank() || canUnmask) {
            return changesJson;
        }
        try {
            Map<String, Object> changes = objectMapper.readValue(changesJson, new TypeReference<Map<String, Object>>() {});
            if (changes.containsKey("data") && changes.get("data") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) changes.get("data");
                Map<String, Object> maskedData = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    String valStr = entry.getValue() != null ? String.valueOf(entry.getValue()) : null;
                    maskedData.put(entry.getKey(), valStr != null ? maskValue(entry.getKey(), valStr) : null);
                }
                changes.put("data", maskedData);
                return objectMapper.writeValueAsString(changes);
            } else {
                return maskJsonData(changesJson, fields, canUnmask);
            }
        } catch (Exception e) {
            return changesJson;
        }
    }

    public String maskValue(String key, String val) {
        if (val == null || val.isBlank()) return val;
        String lowerKey = key.toLowerCase();

        // 1. Phone mask
        if (lowerKey.contains("phone") || lowerKey.contains("mobile") || lowerKey.contains("tel")) {
            return maskPhone(val);
        }

        // 2. Email mask
        if (lowerKey.contains("email") || lowerKey.contains("mail")) {
            return maskEmail(val);
        }

        // 3. Resident/Identity No mask (6 digits - 7 digits)
        if (lowerKey.contains("resident") || lowerKey.contains("rrn") || lowerKey.contains("ssn")) {
            return val.replaceAll("(\\d{6})[-.]?(\\d)[0-9]{6}", "$1-$2******");
        }

        // 4. Account / Card mask
        if (lowerKey.contains("account") || lowerKey.contains("card") || lowerKey.contains("bank")) {
            return maskCard(val);
        }

        return maskGeneric(val);
    }

    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (name.length() <= 1) {
            return name + "***" + domain;
        }
        return name.charAt(0) + "***" + domain;
    }

    public String maskPhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("(\\d{2,3})[-.]?(\\d{3,4})[-.]?(\\d{4})", "$1-****-$3");
    }

    public String maskGeneric(String text) {
        if (text == null || text.length() <= 2) return text;
        return text.substring(0, 2) + "*".repeat(Math.max(0, text.length() - 2));
    }

    public String maskCard(String cardNo) {
        if (cardNo == null) return null;
        if (cardNo.length() >= 16) {
            return cardNo.replaceAll("(\\d{4})[-.]?(\\d{4})[-.]?(\\d{4})[-.]?(\\d{4})", "$1-****-****-$4");
        }
        if (cardNo.length() > 6) {
            return cardNo.substring(0, 4) + "-****-" + cardNo.substring(cardNo.length() - 2);
        }
        return "***";
    }

    private Map<String, Object> parseData(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
