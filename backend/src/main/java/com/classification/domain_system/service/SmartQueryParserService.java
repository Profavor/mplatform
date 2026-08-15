package com.classification.domain_system.service;

import com.classification.domain_system.dto.SmartQueryDto;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartQueryParserService {

    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public SmartQueryDto.SmartQueryResponse parseAndExecute(UUID domainId, String naturalLanguageQuery) {
        if (naturalLanguageQuery == null || naturalLanguageQuery.isBlank()) {
            return SmartQueryDto.SmartQueryResponse.builder()
                    .naturalLanguageQuery("")
                    .parsedFilters(Collections.emptyList())
                    .matchedRecordCount(0)
                    .records(Collections.emptyList())
                    .explanation("질의어가 입력되지 않았습니다.")
                    .build();
        }

        List<FieldDefinition> fields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<SmartQueryDto.ParsedFilter> filters = new ArrayList<>();

        String query = naturalLanguageQuery.trim();

        // Heuristic smart parsing for demo/MDM AI query
        for (FieldDefinition f : fields) {
            String fieldKey = f.getKey();
            if (query.contains(fieldKey)) {
                filters.add(SmartQueryDto.ParsedFilter.builder()
                        .fieldKey(fieldKey)
                        .operator("CONTAINS")
                        .value(extractKeywordNear(query, fieldKey))
                        .build());
            }
        }

        // Generic keyword checks
        if (query.contains("VIP") || query.contains("vip")) {
            filters.add(SmartQueryDto.ParsedFilter.builder()
                    .fieldKey("grade")
                    .operator("EQUALS")
                    .value("VIP")
                    .build());
        }
        if (query.contains("서울")) {
            filters.add(SmartQueryDto.ParsedFilter.builder()
                    .fieldKey("address")
                    .operator("CONTAINS")
                    .value("서울")
                    .build());
        }

        if (filters.isEmpty()) {
            filters.add(SmartQueryDto.ParsedFilter.builder()
                    .fieldKey("keyword")
                    .operator("CONTAINS")
                    .value(query)
                    .build());
        }

        List<Record> domainRecords = recordRepository.findAllByDomainId(domainId);
        List<Map<String, Object>> matchedList = new ArrayList<>();

        for (Record r : domainRecords) {
            Map<String, Object> data = parseData(r.getData());
            boolean matchAll = true;

            for (SmartQueryDto.ParsedFilter filter : filters) {
                if (filter.getFieldKey().equals("keyword")) {
                    boolean anyMatch = data.values().stream()
                            .anyMatch(v -> v != null && String.valueOf(v).contains(filter.getValue()));
                    if (!anyMatch) matchAll = false;
                } else {
                    Object val = data.get(filter.getFieldKey());
                    if (val == null) {
                        matchAll = false;
                    } else if (filter.getOperator().equals("EQUALS")) {
                        if (!String.valueOf(val).equalsIgnoreCase(filter.getValue())) matchAll = false;
                    } else if (filter.getOperator().equals("CONTAINS")) {
                        if (!String.valueOf(val).contains(filter.getValue())) matchAll = false;
                    }
                }
            }

            if (matchAll) {
                Map<String, Object> row = new LinkedHashMap<>(data);
                row.put("_recordCode", "REC-" + r.getId().toString().substring(0, 8));
                matchedList.add(row);
            }
        }

        return SmartQueryDto.SmartQueryResponse.builder()
                .naturalLanguageQuery(naturalLanguageQuery)
                .parsedFilters(filters)
                .matchedRecordCount(matchedList.size())
                .records(matchedList)
                .explanation(String.format("자연어 질의에서 %d개의 스마트 필터 조건이 도출되어 %d건의 레코드가 검색되었습니다.", filters.size(), matchedList.size()))
                .build();
    }

    private String extractKeywordNear(String text, String target) {
        int idx = text.indexOf(target);
        if (idx >= 0 && idx + target.length() < text.length()) {
            String rest = text.substring(idx + target.length()).trim();
            String[] tokens = rest.split("\\s+");
            if (tokens.length > 0) return tokens[0].replace("=", "").replace(":", "");
        }
        return target;
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
