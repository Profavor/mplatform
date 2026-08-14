package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataProfilingResponse;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataProfilingService {

    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public List<DataProfilingResponse> profileDomainData(UUID domainId) {
        List<UUID> nodeIds = nodeRepository.findByDomain_Id(domainId).stream()
                .map(node -> node.getId())
                .collect(Collectors.toList());

        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }

        // For simplicity and performance, sample up to 1000 records
        List<Record> sampleRecords = recordRepository.findByNodeIdIn(nodeIds, PageRequest.of(0, 1000)).getContent();

        if (sampleRecords.isEmpty()) {
            return Collections.emptyList();
        }

        long totalRecords = sampleRecords.size();
        List<Map<String, Object>> parsedDataList = new ArrayList<>(sampleRecords.size());
        Set<String> allKeys = new HashSet<>();

        for (Record record : sampleRecords) {
            if (record.getData() == null || record.getData().isBlank()) {
                parsedDataList.add(Collections.emptyMap());
                continue;
            }
            try {
                Map<String, Object> data = objectMapper.readValue(record.getData(), new TypeReference<Map<String, Object>>() {});
                parsedDataList.add(data);
                allKeys.addAll(data.keySet());
            } catch (Exception e) {
                parsedDataList.add(Collections.emptyMap());
                log.warn("Failed to parse record data {}", record.getId());
            }
        }

        Map<String, FieldStats> statsMap = new HashMap<>();
        for (String key : allKeys) {
            statsMap.put(key, new FieldStats());
        }

        for (Map<String, Object> data : parsedDataList) {
            for (String key : allKeys) {
                FieldStats stats = statsMap.get(key);
                Object value = data.get(key);
                if (value == null || value.toString().isBlank()) {
                    stats.nullCount++;
                } else {
                    String strValue = value.toString().trim();
                    stats.valueFrequencies.put(strValue, stats.valueFrequencies.getOrDefault(strValue, 0L) + 1);
                }
            }
        }

        List<DataProfilingResponse> responses = new ArrayList<>();
        for (Map.Entry<String, FieldStats> entry : statsMap.entrySet()) {

            String fieldName = entry.getKey();
            FieldStats stats = entry.getValue();

            long nonNullCount = stats.valueFrequencies.values().stream().mapToLong(Long::longValue).sum();
            double nullRatio = (double) stats.nullCount / totalRecords;
            
            // Top 5 values
            Map<String, Long> topValues = stats.valueFrequencies.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            responses.add(DataProfilingResponse.builder()
                    .fieldName(fieldName)
                    .totalCount(totalRecords)
                    .nullCount(stats.nullCount)
                    .nullRatio(nullRatio)
                    .cardinality(stats.valueFrequencies.size())
                    .topValues(topValues)
                    .build());
        }

        return responses;
    }

    private static class FieldStats {
        long nullCount = 0;
        Map<String, Long> valueFrequencies = new HashMap<>();
    }
}
