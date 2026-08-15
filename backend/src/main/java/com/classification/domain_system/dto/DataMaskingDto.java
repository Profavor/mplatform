package com.classification.domain_system.dto;

import lombok.*;

import java.util.Map;
import java.util.UUID;

public class DataMaskingDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaskedDataResponse {
        private UUID recordId;
        private String recordCode;
        private Map<String, Object> originalData;
        private Map<String, Object> maskedData;
        private boolean isMasked;
        private int maskedFieldCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaskingRuleConfig {
        private String fieldKey;
        private String maskingType; // PHONE, EMAIL, RESIDENT_NO, GENERAL
        private boolean enabled;
    }
}
