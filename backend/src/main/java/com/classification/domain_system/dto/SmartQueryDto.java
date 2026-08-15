package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SmartQueryDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SmartQueryRequest {
        private String naturalLanguageQuery;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParsedFilter {
        private String fieldKey;
        private String operator; // EQUALS, CONTAINS, GTE, LTE
        private String value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SmartQueryResponse {
        private String naturalLanguageQuery;
        private List<ParsedFilter> parsedFilters;
        private int matchedRecordCount;
        private List<Map<String, Object>> records;
        private String explanation;
    }
}
