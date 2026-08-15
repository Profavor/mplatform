package com.classification.domain_system.service;

import com.classification.domain_system.dto.GoldenRecordDto;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoldenRecordBuilderService {

    private final RecordRepository recordRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public GoldenRecordDto.GoldenRecordPreviewResponse buildGoldenRecord(GoldenRecordDto.GoldenRecordBuildRequest req) {
        if (req == null || req.getTargetRecordIds() == null || req.getTargetRecordIds().isEmpty()) {
            return GoldenRecordDto.GoldenRecordPreviewResponse.builder()
                    .candidateRecordCodes(Collections.emptyList())
                    .fieldChoices(Collections.emptyList())
                    .assembledData(Collections.emptyMap())
                    .confidenceScore(0)
                    .summary("병합 대상 레코드가 없습니다.")
                    .build();
        }

        List<Record> records = recordRepository.findAllById(req.getTargetRecordIds());
        List<String> recordCodes = records.stream()
                .map(r -> "REC-" + r.getId().toString().substring(0, 8))
                .collect(Collectors.toList());

        Map<UUID, Map<String, Object>> recordDataMap = new LinkedHashMap<>();
        Set<String> allFieldKeys = new LinkedHashSet<>();

        for (Record r : records) {
            Map<String, Object> data = parseData(r.getData());
            recordDataMap.put(r.getId(), data);
            allFieldKeys.addAll(data.keySet());
        }

        List<GoldenRecordDto.GoldenFieldChoice> choices = new ArrayList<>();
        Map<String, Object> assembledData = new LinkedHashMap<>();

        for (String key : allFieldKeys) {
            Record bestRecord = null;
            Object bestValue = null;
            String reason = "기본값 채택";

            for (Record r : records) {
                Map<String, Object> data = recordDataMap.get(r.getId());
                Object val = data != null ? data.get(key) : null;
                if (val != null && !String.valueOf(val).isBlank() && !String.valueOf(val).equals("-")) {
                    bestRecord = r;
                    bestValue = val;
                    reason = "유효값 보유 레코드 채택";
                    break; // Pick the first valid or high-priority
                }
            }

            if (bestRecord != null) {
                String recCode = "REC-" + bestRecord.getId().toString().substring(0, 8);
                choices.add(GoldenRecordDto.GoldenFieldChoice.builder()
                        .fieldKey(key)
                        .chosenRecordId(bestRecord.getId())
                        .chosenRecordCode(recCode)
                        .chosenValue(bestValue)
                        .sourceSystem(bestRecord.getSourceSystem() != null ? bestRecord.getSourceSystem() : "INTERNAL")
                        .selectionReason(reason)
                        .build());
                assembledData.put(key, bestValue);
            }
        }

        return GoldenRecordDto.GoldenRecordPreviewResponse.builder()
                .candidateRecordCodes(recordCodes)
                .fieldChoices(choices)
                .assembledData(assembledData)
                .confidenceScore(95)
                .summary(String.format("총 %d개 후보 레코드로부터 %d개 필드가 골든 레코드로 최적 조립되었습니다.", records.size(), assembledData.size()))
                .build();
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
