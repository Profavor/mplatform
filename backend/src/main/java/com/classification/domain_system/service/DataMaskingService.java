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
        String trimmed = rrn.trim();
        Pattern pattern = Pattern.compile("^(\\d{6})[-.]?([\\d*]{1})([\\d*]*)$");
        Matcher matcher = pattern.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1) + "-" + matcher.group(2) + "******";
        }
        
        String clean = trimmed.replaceAll("[^\\d*]", "");
        if (clean.length() >= 7) {
            return clean.substring(0, 6) + "-" + clean.substring(6, 7) + "******";
        }
        return maskGeneric(rrn);
    }

    public String maskCard(String cardNo) {
        if (cardNo == null || cardNo.isBlank()) return cardNo;
        String trimmed = cardNo.trim();
        Pattern hyphenPattern = Pattern.compile("^(\\d{4})[-.]?(\\d{4})[-.]?(\\d{4})[-.]?(\\d{3,4})$");
        Matcher matcher = hyphenPattern.matcher(trimmed);
        if (matcher.matches()) {
            return matcher.group(1) + "-****-****-" + matcher.group(4);
        }
        String digitsOnly = trimmed.replaceAll("\\D", "");
        if (digitsOnly.length() >= 13 && digitsOnly.length() <= 19) {
            int len = digitsOnly.length();
            String prefix = digitsOnly.substring(0, 4);
            String suffix = digitsOnly.substring(len - 4);
            return prefix + "-****-****-" + suffix;
        }
        return maskGeneric(cardNo);
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
            dataMap.keySet().removeIf(key -> key.toLowerCase().startsWith("_idx_"));

            if (fields != null) {
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
                            Object valObj = dataMap.get(matchedKey);
                            if (valObj instanceof String rawVal && !rawVal.isBlank()) {
                                String decrypted = decryptUntilPlaintext(rawVal);
                                if (canUnmask) {
                                    dataMap.put(matchedKey, decrypted);
                                } else {
                                    if (isStillEncrypted(decrypted)) {
                                        dataMap.put(matchedKey, decrypted);
                                    } else {
                                        String maskedVal = applyMasking(field, decrypted);
                                        dataMap.put(matchedKey, maskedVal);
                                    }
                                }
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

    public String maskChangesJson(String changesJson, List<FieldDefinition> fields, boolean canUnmask) {
        if (changesJson == null || changesJson.isBlank()) return changesJson;
        try {
            Map<String, Object> map = objectMapper.readValue(changesJson, new TypeReference<Map<String, Object>>() {});
            if (map.containsKey("before") || map.containsKey("after")) {
                if (map.get("before") != null) {
                    String beforeStr = objectMapper.writeValueAsString(map.get("before"));
                    String maskedBefore = maskJsonData(beforeStr, fields, canUnmask);
                    map.put("before", objectMapper.readValue(maskedBefore, Object.class));
                }
                if (map.get("after") != null) {
                    String afterStr = objectMapper.writeValueAsString(map.get("after"));
                    String maskedAfter = maskJsonData(afterStr, fields, canUnmask);
                    map.put("after", objectMapper.readValue(maskedAfter, Object.class));
                }
                return objectMapper.writeValueAsString(map);
            } else {
                return maskJsonData(changesJson, fields, canUnmask);
            }
        } catch (Exception e) {
            log.error("Failed to mask changes JSON data", e);
            return changesJson;
        }
    }

    private String applyMasking(FieldDefinition field, String value) {
        if (value == null) return null;
        String pattern = field.getMaskingPattern() != null ? field.getMaskingPattern().toUpperCase() : "";

        if ("CARD".equals(pattern)) return maskCard(value);
        if ("RRN".equals(pattern) || "SSN".equals(pattern)) return maskRrn(value);
        if ("PHONE".equals(pattern) || "MOBILE".equals(pattern)) return maskPhone(value);
        if ("EMAIL".equals(pattern)) return maskEmail(value);

        String fieldKey = field.getKey() != null ? field.getKey().toLowerCase() : "";
        String fieldType = field.getType() != null ? field.getType().toUpperCase() : "";

        if (fieldType.contains("CARD") || fieldKey.contains("card")) {
            return maskCard(value);
        } else if (fieldType.contains("EMAIL") || fieldKey.contains("email")) {
            return maskEmail(value);
        } else if (fieldType.contains("PHONE") || fieldKey.contains("phone") || fieldKey.contains("mobile")) {
            return maskPhone(value);
        } else if (fieldType.contains("RRN") || fieldKey.contains("rrn") || fieldKey.contains("ssn") || fieldKey.contains("jumin") || fieldKey.contains("resident") || fieldKey.contains("주민")) {
            return maskRrn(value);
        }

        // Auto-detect value format if pattern is GENERIC or unspecified
        String trimmedVal = value.trim();
        if (Pattern.matches("^(\\d{6})[-.]?([\\d*]{1})([\\d*]{6})$", trimmedVal)) {
            return maskRrn(trimmedVal);
        }
        if (Pattern.matches("^(\\d{2,3})[-.]?(\\d{3,4})[-.]?(\\d{4})$", trimmedVal)) {
            return maskPhone(trimmedVal);
        }

        if ("GENERIC".equals(pattern)) return maskGeneric(value);
        return maskGeneric(value);
    }

    private String decryptUntilPlaintext(String val) {
        if (val == null || val.isBlank() || fieldEncryptionService == null) return val;
        String current = val;
        for (int i = 0; i < 5; i++) {
            String next = fieldEncryptionService.decrypt(current);
            if (next == null || next.equals(current)) {
                break;
            }
            current = next;
        }
        return current;
    }

    private boolean isStillEncrypted(String str) {
        if (str == null || str.isBlank()) return false;
        return fieldEncryptionService != null && fieldEncryptionService.isEncrypted(str);
    }
}
