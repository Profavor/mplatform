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
    private final com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataMaskingService(FieldEncryptionService fieldEncryptionService) {
        this.fieldEncryptionService = fieldEncryptionService;
        this.recordRepository = null;
        this.fieldDefinitionRepository = null;
    }

    @Autowired
    public DataMaskingService(FieldEncryptionService fieldEncryptionService, 
                              RecordRepository recordRepository,
                              com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository) {
        this.fieldEncryptionService = fieldEncryptionService;
        this.recordRepository = recordRepository;
        this.fieldDefinitionRepository = fieldDefinitionRepository;
    }

    @Transactional(readOnly = true)
    public DataMaskingDto.MaskedDataResponse getMaskedRecord(UUID recordId, boolean hasUnmaskPermission) {
        if (recordRepository == null) {
            throw new IllegalStateException("RecordRepository is not configured");
        }
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found: " + recordId));

        Map<String, Object> originalData = parseData(record.getData());
        List<FieldDefinition> fields = Collections.emptyList();
        if (fieldDefinitionRepository != null && record.getNode() != null) {
            if (record.getNode().getId() != null) {
                fields = fieldDefinitionRepository.findNodeFieldsWithSort(record.getNode().getId());
            }
            if (fields.isEmpty() && record.getNode().getDomain() != null && record.getNode().getDomain().getId() != null) {
                fields = fieldDefinitionRepository.findDomainFieldsWithSort(record.getNode().getDomain().getId());
            }
        }
        
        String maskedJson = maskJsonData(record.getData(), fields, hasUnmaskPermission);
        Map<String, Object> maskedData = parseData(maskedJson);

        int maskedCount = 0;
        for (Map.Entry<String, Object> entry : originalData.entrySet()) {
            Object orig = entry.getValue();
            Object masked = maskedData.get(entry.getKey());
            if (orig != null && !orig.equals(masked)) {
                maskedCount++;
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
        if (dataJson == null || dataJson.isBlank()) {
            return dataJson;
        }
        try {
            Map<String, Object> map = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            Map<String, FieldDefinition> fieldMap = new HashMap<>();
            if (fields != null) {
                for (FieldDefinition f : fields) {
                    if (f.getKey() != null) {
                        fieldMap.put(f.getKey().toLowerCase(), f);
                    }
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();

                FieldDefinition fd = fieldMap.get(key.toLowerCase());
                if (fd == null) {
                    result.put(key, val);
                    continue;
                }

                // 1. Encrypted field handling
                if (Boolean.TRUE.equals(fd.getIsEncrypted()) && val instanceof String valStr && fieldEncryptionService != null) {
                    String decrypted = fieldEncryptionService.decrypt(valStr);
                    if (canUnmask) {
                        result.put(key, decrypted);
                    } else {
                        String pattern = isSpecificMaskingPattern(fd.getMaskingPattern()) ? fd.getMaskingPattern() : "RRN";
                        result.put(key, maskByPattern(pattern, decrypted));
                    }
                    continue;
                }

                // 2. Explicit sensitive masking pattern configured on field definition (e.g. RRN, PHONE, EMAIL, CARD)
                if (isSpecificMaskingPattern(fd.getMaskingPattern())) {
                    if (canUnmask) {
                        result.put(key, val);
                    } else if (val instanceof String valStr) {
                        result.put(key, maskByPattern(fd.getMaskingPattern(), valStr));
                    } else {
                        result.put(key, val);
                    }
                    continue;
                }

                // 3. Normal fields remain untouched (schema-driven, no generic masking)
                result.put(key, val);
            }
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return dataJson;
        }
    }

    public boolean isSpecificMaskingPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) return false;
        String p = pattern.toUpperCase();
        if (p.equals("NONE") || p.equals("OFF") || p.equals("GENERIC") || p.equals("DEFAULT")) return false;
        return p.contains("RRN") || p.contains("RESIDENT") || p.contains("JUMIN") || p.contains("SSN")
                || p.contains("PHONE") || p.contains("MOBILE") || p.contains("TEL")
                || p.contains("EMAIL") || p.contains("MAIL")
                || p.contains("CARD") || p.contains("ACCOUNT") || p.contains("BANK");
    }

    public String maskChangesJson(String changesJson, List<FieldDefinition> fields, boolean canUnmask) {
        if (changesJson == null || changesJson.isBlank()) {
            return changesJson;
        }
        try {
            Map<String, Object> changes = objectMapper.readValue(changesJson, new TypeReference<Map<String, Object>>() {});
            boolean hasSubMaps = false;
            for (String subKey : List.of("data", "newData", "previousData", "changes")) {
                if (changes.containsKey(subKey) && changes.get(subKey) instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) changes.get(subKey);
                    String maskedDataStr = maskJsonData(objectMapper.writeValueAsString(dataMap), fields, canUnmask);
                    changes.put(subKey, objectMapper.readValue(maskedDataStr, Map.class));
                    hasSubMaps = true;
                }
            }
            if (hasSubMaps) {
                return objectMapper.writeValueAsString(changes);
            } else {
                return maskJsonData(changesJson, fields, canUnmask);
            }
        } catch (Exception e) {
            return changesJson;
        }
    }

    public String maskByPattern(String pattern, String val) {
        if (val == null || val.isBlank()) return val;
        if (!isSpecificMaskingPattern(pattern)) return val;

        String upperPattern = pattern.toUpperCase();
        if (upperPattern.contains("RRN") || upperPattern.contains("RESIDENT") || upperPattern.contains("JUMIN") || upperPattern.contains("SSN")) {
            return val.replaceAll("(\\d{6})[-.]?(\\d)[0-9]{6}", "$1-$2******");
        }
        if (upperPattern.contains("PHONE") || upperPattern.contains("MOBILE") || upperPattern.contains("TEL")) {
            return maskPhone(val);
        }
        if (upperPattern.contains("EMAIL") || upperPattern.contains("MAIL")) {
            return maskEmail(val);
        }
        if (upperPattern.contains("CARD") || upperPattern.contains("ACCOUNT") || upperPattern.contains("BANK")) {
            return maskCard(val);
        }

        return val;
    }

    public String maskValue(String key, String val) {
        return val;
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
        return text.substring(0, 2) + "*".repeat(Math.max(1, text.length() - 2));
    }

    public String maskCard(String card) {
        if (card == null) return null;
        return card.replaceAll("(\\d{4})[-.]?(\\d{4})[-.]?(\\d{4})[-.]?(\\d{4})", "$1-****-****-$4");
    }

    private Map<String, Object> parseData(String data) {
        if (data == null || data.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Failed to parse record data: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
