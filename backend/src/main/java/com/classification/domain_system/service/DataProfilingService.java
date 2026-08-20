package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataProfilingReportDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataProfilingService {

    private final DomainRepository domainRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public DataProfilingReportDto getProfilingReport(UUID domainId) {
        Domain domain = domainRepository.findById(domainId).orElse(null);
        if (domain == null) {
            ClassificationNode node = nodeRepository.findById(domainId).orElse(null);
            if (node != null && node.getDomain() != null) {
                domain = node.getDomain();
                domainId = domain.getId();
            } else {
                throw new ResourceNotFoundException("Domain not found: " + domainId);
            }
        }

        String domainName = domain.getName() != null ? domain.getName().getOrDefault("ko", domain.getName().getOrDefault("en", "Domain")) : "Domain";

        List<Record> records = recordRepository.findAllByDomainId(domainId);
        List<ClassificationNode> nodes = nodeRepository.findByDomain_Id(domainId);
        List<FieldDefinition> domainFields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);
        List<UUID> nodeIds = nodes.stream().map(ClassificationNode::getId).collect(Collectors.toList());
        List<FieldDefinition> nodeFields = !nodeIds.isEmpty() ? fieldDefinitionRepository.findByDefinedAtNode_IdIn(nodeIds) : Collections.emptyList();

        Map<String, FieldDefinition> fieldMap = new LinkedHashMap<>();
        domainFields.forEach(f -> fieldMap.put(f.getKey(), f));
        nodeFields.forEach(f -> fieldMap.putIfAbsent(f.getKey(), f));

        // Parse all records
        List<Map<String, Object>> parsedDataList = new ArrayList<>();
        Map<UUID, Map<String, Object>> recordDataMap = new HashMap<>();

        for (Record r : records) {
            Map<String, Object> data = Collections.emptyMap();
            if (r.getData() != null && !r.getData().isBlank()) {
                try {
                    data = objectMapper.readValue(r.getData(), new TypeReference<Map<String, Object>>() {});
                } catch (Exception ignored) {}
            }
            parsedDataList.add(data);
            recordDataMap.put(r.getId(), data);
        }

        List<DataProfilingReportDto.FieldProfile> fieldProfiles = new ArrayList<>();
        List<DataProfilingReportDto.OutlierRecord> outliers = new ArrayList<>();

        long totalRecords = records.size();

        for (Map.Entry<String, FieldDefinition> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldDefinition fd = entry.getValue();
            String fieldName = fd.getName() != null ? fd.getName().getOrDefault("ko", fd.getName().getOrDefault("en", key)) : key;
            String type = fd.getType() != null ? fd.getType() : "STRING";

            List<Object> rawValues = new ArrayList<>();
            for (Map<String, Object> d : parsedDataList) {
                rawValues.add(d.get(key));
            }

            long nullCount = rawValues.stream().filter(v -> v == null || String.valueOf(v).trim().isEmpty()).count();
            double nullRate = totalRecords > 0 ? ((double) nullCount / totalRecords) * 100.0 : 0.0;

            List<String> nonNullStrings = rawValues.stream()
                    .filter(v -> v != null && !String.valueOf(v).trim().isEmpty())
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            long distinctCount = nonNullStrings.stream().distinct().count();
            double uniquenessRatio = !nonNullStrings.isEmpty() ? ((double) distinctCount / nonNullStrings.size()) * 100.0 : 0.0;

            // Numeric Statistics & IQR Outlier Calculation
            Double min = null;
            Double max = null;
            Double avg = null;
            Double lowerBound = null;
            Double upperBound = null;
            long outlierCount = 0;

            List<Double> numericValues = new ArrayList<>();
            for (Object v : rawValues) {
                if (v != null) {
                    try {
                        numericValues.add(Double.parseDouble(String.valueOf(v).replaceAll("[^0-9.-]", "")));
                    } catch (Exception ignored) {}
                }
            }

            if (numericValues.size() >= 4) {
                Collections.sort(numericValues);
                min = numericValues.get(0);
                max = numericValues.get(numericValues.size() - 1);
                avg = numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

                int q1Idx = (int) (numericValues.size() * 0.25);
                int q3Idx = (int) (numericValues.size() * 0.75);
                double q1 = numericValues.get(q1Idx);
                double q3 = numericValues.get(q3Idx);
                double iqr = q3 - q1;

                lowerBound = q1 - 1.5 * iqr;
                upperBound = q3 + 1.5 * iqr;

                for (Record r : records) {
                    Map<String, Object> data = recordDataMap.get(r.getId());
                    if (data != null && data.containsKey(key)) {
                        try {
                            double val = Double.parseDouble(String.valueOf(data.get(key)).replaceAll("[^0-9.-]", ""));
                            if (val < lowerBound || val > upperBound) {
                                outlierCount++;
                                outliers.add(DataProfilingReportDto.OutlierRecord.builder()
                                        .recordId(r.getId())
                                        .fieldKey(key)
                                        .value(val)
                                        .reason(String.format("IQR 통계 이상치 (범위: %.1f ~ %.1f, 입력값: %.1f)", lowerBound, upperBound, val))
                                        .build());
                            }
                        } catch (Exception ignored) {}
                    }
                }
            } else if (!numericValues.isEmpty()) {
                min = numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                max = numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                avg = numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            }

            fieldProfiles.add(DataProfilingReportDto.FieldProfile.builder()
                    .fieldKey(key)
                    .fieldName(fieldName)
                    .fieldType(type)
                    .totalCount(totalRecords)
                    .nullCount(nullCount)
                    .nullRate(Math.round(nullRate * 10.0) / 10.0)
                    .distinctCount(distinctCount)
                    .uniquenessRatio(Math.round(uniquenessRatio * 10.0) / 10.0)
                    .minValue(min != null ? Math.round(min * 10.0) / 10.0 : null)
                    .maxValue(max != null ? Math.round(max * 10.0) / 10.0 : null)
                    .avgValue(avg != null ? Math.round(avg * 10.0) / 10.0 : null)
                    .iqrLowerBound(lowerBound != null ? Math.round(lowerBound * 10.0) / 10.0 : null)
                    .iqrUpperBound(upperBound != null ? Math.round(upperBound * 10.0) / 10.0 : null)
                    .outlierCount(outlierCount)
                    .build());
        }

        return DataProfilingReportDto.builder()
                .domainId(domain.getId())
                .domainName(domainName)
                .totalRecords(totalRecords)
                .scannedAt(LocalDateTime.now())
                .fieldProfiles(fieldProfiles)
                .outliers(outliers)
                .build();
    }

    @Transactional(readOnly = true)
    public List<com.classification.domain_system.dto.DataProfilingResponse> profileDomainData(UUID domainId) {
        DataProfilingReportDto report = getProfilingReport(domainId);
        List<com.classification.domain_system.dto.DataProfilingResponse> responses = new ArrayList<>();

        for (DataProfilingReportDto.FieldProfile fp : report.getFieldProfiles()) {
            responses.add(com.classification.domain_system.dto.DataProfilingResponse.builder()
                    .fieldKey(fp.getFieldKey())
                    .fieldName(fp.getFieldName() != null && !fp.getFieldName().isBlank() ? fp.getFieldName() : fp.getFieldKey())
                    .totalCount(fp.getTotalCount())
                    .nullCount(fp.getNullCount())
                    .nullRatio(fp.getNullRate() / 100.0)
                    .cardinality(fp.getDistinctCount())
                    .topValues(Collections.emptyMap())
                    .build());
        }

        return responses;
    }
}
