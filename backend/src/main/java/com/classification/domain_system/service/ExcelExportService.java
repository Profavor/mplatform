package com.classification.domain_system.service;

import com.classification.domain_system.dto.ExcelExportDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExcelExportService {

    private final DomainRepository domainRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] generateTemplate(UUID domainId, UUID nodeId, String lang) {
        String effectiveLang = (lang != null && !lang.isBlank()) ? lang : "ko";
        List<FieldDefinition> fields = getEffectiveFields(domainId, nodeId);

        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF"); // UTF-8 BOM for Microsoft Excel compatibility

        // Row 1: Display Names (with * for required)
        List<String> headerNames = fields.stream().map(f -> {
            String name = (f.getName() != null)
                    ? f.getName().getOrDefault(effectiveLang, f.getName().getOrDefault("en", f.getKey()))
                    : f.getKey();
            return escapeCsv(f.getRequired() != null && f.getRequired() ? name + "*" : name);
        }).collect(Collectors.toList());
        sb.append(String.join(",", headerNames)).append("\r\n");

        // Row 2: Field Keys
        List<String> headerKeys = fields.stream().map(f -> escapeCsv(f.getKey())).collect(Collectors.toList());
        sb.append(String.join(",", headerKeys)).append("\r\n");

        // Row 3: Guide Sample Values
        List<String> sampleValues = fields.stream().map(f -> escapeCsv(getSampleValue(f))).collect(Collectors.toList());
        sb.append(String.join(",", sampleValues)).append("\r\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportRecordsToCsv(UUID domainId, UUID nodeId, String lang) {
        String effectiveLang = (lang != null && !lang.isBlank()) ? lang : "ko";
        List<FieldDefinition> fields = getEffectiveFields(domainId, nodeId);
        List<Record> records = (nodeId != null)
                ? recordRepository.findByNodeId(nodeId, Pageable.unpaged()).getContent()
                : recordRepository.findAllByDomainId(domainId);

        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF"); // UTF-8 BOM

        // Header Row: Field Display Names
        List<String> headerNames = fields.stream().map(f -> {
            String name = (f.getName() != null)
                    ? f.getName().getOrDefault(effectiveLang, f.getName().getOrDefault("en", f.getKey()))
                    : f.getKey();
            return escapeCsv(name);
        }).collect(Collectors.toList());
        sb.append(String.join(",", headerNames)).append("\r\n");

        // Record Rows
        for (Record r : records) {
            Map<String, Object> data = parseData(r.getData());
            List<String> rowValues = fields.stream().map(f -> {
                Object val = data.get(f.getKey());
                if (val == null) return "";
                if (val instanceof Map) {
                    Map<?, ?> valMap = (Map<?, ?>) val;
                    Object localized = valMap.get(effectiveLang);
                    return escapeCsv(localized != null ? String.valueOf(localized) : String.valueOf(val));
                }
                return escapeCsv(String.valueOf(val));
            }).collect(Collectors.toList());

            sb.append(String.join(",", rowValues)).append("\r\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<FieldDefinition> getEffectiveFields(UUID domainId, UUID nodeId) {
        List<FieldDefinition> fields = new ArrayList<>();
        if (domainId != null) {
            fields.addAll(fieldDefinitionRepository.findDomainFieldsWithSort(domainId));
        }
        if (nodeId != null) {
            fields.addAll(fieldDefinitionRepository.findNodeFieldsWithSort(nodeId));
        }
        return fields;
    }

    private String getSampleValue(FieldDefinition f) {
        String type = f.getType() != null ? f.getType() : "STRING";
        switch (type.toUpperCase()) {
            case "NUMBER": return "1000";
            case "DATE": return "2026-08-15";
            case "BOOLEAN": return "true";
            case "ENUM": return "OPTION_A";
            default: return "예시 텍스트";
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
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
