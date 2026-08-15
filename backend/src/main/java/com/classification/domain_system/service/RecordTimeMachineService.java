package com.classification.domain_system.service;

import com.classification.domain_system.dto.RecordTimeMachineDto;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
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
public class RecordTimeMachineService {

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public RecordTimeMachineDto.TimeMachineDiffResponse getTimelineAndDiff(UUID recordId, Integer reqV1, Integer reqV2) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found: " + recordId));

        String recordCode = "REC-" + record.getId().toString().substring(0, 8);
        List<RecordHistory> histories = recordHistoryRepository.findByRecordIdOrderByVersionAsc(recordId);

        Map<Integer, Map<String, Object>> versionDataMap = new TreeMap<>();
        List<RecordTimeMachineDto.VersionInfo> versionInfos = new ArrayList<>();

        if (histories.isEmpty()) {
            int curVer = record.getVersion() != null ? record.getVersion() : 1;
            Map<String, Object> curData = parseData(record.getData());
            versionDataMap.put(curVer, curData);
            versionInfos.add(RecordTimeMachineDto.VersionInfo.builder()
                    .version(curVer)
                    .changeType("INITIAL")
                    .changedBy("SYSTEM")
                    .changedAt(record.getCreatedAt())
                    .changeReason("최초 생성")
                    .build());
        } else {
            for (RecordHistory h : histories) {
                int ver = h.getVersion() != null ? h.getVersion() : 1;
                Map<String, Object> data = parseData(h.getNewData());
                versionDataMap.put(ver, data);
                versionInfos.add(RecordTimeMachineDto.VersionInfo.builder()
                        .version(ver)
                        .changeType(h.getChangeType() != null ? h.getChangeType() : "UPDATE")
                        .changedBy(h.getChangedBy() != null ? h.getChangedBy() : "USER")
                        .changedAt(h.getChangedAt() != null ? h.getChangedAt() : record.getUpdatedAt())
                        .changeReason(h.getSourceSystem() != null ? h.getSourceSystem() : "-")
                        .build());
            }
            // Ensure latest current record data is also indexed
            int currentVer = record.getVersion() != null ? record.getVersion() : 1;
            if (!versionDataMap.containsKey(currentVer)) {
                versionDataMap.put(currentVer, parseData(record.getData()));
            }
        }

        List<Integer> availableVersions = new ArrayList<>(versionDataMap.keySet());
        int latestVer = availableVersions.get(availableVersions.size() - 1);
        int v2 = (reqV2 != null && versionDataMap.containsKey(reqV2)) ? reqV2 : latestVer;
        int v1 = (reqV1 != null && versionDataMap.containsKey(reqV1)) ? reqV1 : (availableVersions.size() > 1 ? availableVersions.get(availableVersions.size() - 2) : v2);

        Map<String, Object> v1Data = versionDataMap.getOrDefault(v1, Collections.emptyMap());
        Map<String, Object> v2Data = versionDataMap.getOrDefault(v2, Collections.emptyMap());

        // Field definitions for localized field name
        UUID domainId = record.getNode() != null && record.getNode().getDomain() != null ? record.getNode().getDomain().getId() : null;
        List<FieldDefinition> fields = domainId != null ? fieldDefinitionRepository.findDomainFieldsWithSort(domainId) : Collections.emptyList();
        Map<String, String> fieldNameMap = new HashMap<>();
        for (FieldDefinition f : fields) {
            String name = (f.getName() != null) ? f.getName().getOrDefault("ko", f.getName().getOrDefault("en", f.getKey())) : f.getKey();
            fieldNameMap.put(f.getKey(), name);
        }

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(v1Data.keySet());
        allKeys.addAll(v2Data.keySet());

        List<RecordTimeMachineDto.FieldDiffItem> diffItems = new ArrayList<>();
        for (String key : allKeys) {
            Object val1 = v1Data.get(key);
            Object val2 = v2Data.get(key);
            String str1 = val1 != null ? String.valueOf(val1) : null;
            String str2 = val2 != null ? String.valueOf(val2) : null;
            String fieldName = fieldNameMap.getOrDefault(key, key);

            String status;
            if (str1 == null && str2 != null) {
                status = "ADDED";
            } else if (str1 != null && str2 == null) {
                status = "REMOVED";
            } else if (!Objects.equals(str1, str2)) {
                status = "MODIFIED";
            } else {
                status = "UNCHANGED";
            }

            diffItems.add(RecordTimeMachineDto.FieldDiffItem.builder()
                    .fieldKey(key)
                    .fieldName(fieldName)
                    .v1Value(str1 != null ? str1 : "-")
                    .v2Value(str2 != null ? str2 : "-")
                    .diffStatus(status)
                    .build());
        }

        return RecordTimeMachineDto.TimeMachineDiffResponse.builder()
                .recordId(recordId)
                .recordCode(recordCode)
                .v1(v1)
                .v2(v2)
                .v1Data(v1Data)
                .v2Data(v2Data)
                .fieldDiffs(diffItems)
                .allVersions(versionInfos)
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
