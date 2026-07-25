package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataMaskingService {

    private final FieldEncryptionService fieldEncryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataMaskingService() {
        this.fieldEncryptionService = null;
    }

    public String maskEmail(String email) {
        if (email == null || email.isBlank()) return email;
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return maskGeneric(email);
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        char firstChar = local.charAt(0);
        return firstChar + "***" + domain;
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return phone;
        Pattern pattern = Pattern.compile("^(\\d{2,3})[-.]?(\\d{3,4})[-.]?(\\d{4})$");
        Matcher matcher = pattern.matcher(phone.trim());
        if (matcher.matches()) {
            return matcher.group(1) + "-****-" + matcher.group(3);
        }
        return maskGeneric(phone);
    }

    public String maskRrn(String rrn) {
        if (rrn == null || rrn.isBlank()) return rrn;
        Pattern pattern = Pattern.compile("^(\\d{6})[-.]?(\\d{1})(\\d{6})$");
        Matcher matcher = pattern.matcher(rrn.trim());
        if (matcher.matches()) {
            return matcher.group(1) + "-" + matcher.group(2) + "******";
        }
        return maskGeneric(rrn);
    }

    public String maskGeneric(String str) {
        if (str == null || str.isEmpty()) return str;
        int len = str.length();
        if (len <= 2) {
            return "*".repeat(len);
        }
        return str.substring(0, 2) + "*".repeat(len - 2);
    }

    public String maskJsonData(String dataJson, List<FieldDefinition> fields, boolean canUnmask) {
        if (dataJson == null || dataJson.isBlank()) return dataJson;
        try {
            Map<String, Object> dataMap = objectMapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            dataMap.keySet().removeIf(key -> key.startsWith("_idx_"));

            if (fields != null) {
                for (FieldDefinition field : fields) {
                    if (Boolean.TRUE.equals(field.getIsEncrypted()) && dataMap.containsKey(field.getKey())) {
                        Object valObj = dataMap.get(field.getKey());
                        if (valObj instanceof String rawVal && !rawVal.isBlank()) {
                            String decrypted = (fieldEncryptionService != null) ? fieldEncryptionService.decrypt(rawVal) : rawVal;
                            if (canUnmask) {
                                dataMap.put(field.getKey(), decrypted);
                            } else {
                                String maskedVal = applyMasking(field, decrypted);
                                dataMap.put(field.getKey(), maskedVal);
                            }
                        }
                    }
                }
            }
            return objectMapper.writeValueAsString(dataMap);
        } catch (Exception e) {
            log.error("Failed to mask JSON data", e);
            return dataJson;
        }
    }

    private String applyMasking(FieldDefinition field, String value) {
        if (value == null) return null;
        String fieldKey = field.getKey() != null ? field.getKey().toLowerCase() : "";
        String fieldType = field.getType() != null ? field.getType().toUpperCase() : "";

        if (fieldType.contains("EMAIL") || fieldKey.contains("email")) {
            return maskEmail(value);
        } else if (fieldType.contains("PHONE") || fieldKey.contains("phone") || fieldKey.contains("mobile")) {
            return maskPhone(value);
        } else if (fieldType.contains("RRN") || fieldKey.contains("rrn") || fieldKey.contains("ssn")) {
            return maskRrn(value);
        } else {
            return maskGeneric(value);
        }
    }
}
