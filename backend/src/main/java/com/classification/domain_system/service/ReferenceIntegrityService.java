package com.classification.domain_system.service;

import com.classification.domain_system.dto.ReferenceIntegrityDto;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferenceIntegrityService {

    private final RecordRepository recordRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(readOnly = true)
    public ReferenceIntegrityDto.IntegrityReportResponse scanDomainIntegrity(UUID domainId) {
        List<Record> records = recordRepository.findAllByDomainId(domainId);
        List<FieldDefinition> fields = fieldDefinitionRepository.findDomainFieldsWithSort(domainId);

        List<String> refFieldKeys = fields.stream()
                .filter(f -> "RECORD_REF".equalsIgnoreCase(f.getType())
                        || f.getKey().endsWith("RecordId")
                        || f.getKey().endsWith("RefId")
                        || f.getKey().contains("parent")
                        || f.getKey().contains("deptId"))
                .map(FieldDefinition::getKey)
                .collect(Collectors.toList());

        List<ReferenceIntegrityDto.OrphanReferenceItem> violations = new ArrayList<>();

        for (Record r : records) {
            String recordCode = "REC-" + r.getId().toString().substring(0, 8);
            Map<String, Object> data = parseData(r.getData());

            for (String fieldKey : refFieldKeys) {
                Object val = data.get(fieldKey);
                if (val == null || String.valueOf(val).isBlank() || String.valueOf(val).equals("-")) continue;

                String targetIdStr = String.valueOf(val).trim();
                UUID targetUuid = parseUuidSafely(targetIdStr);

                if (targetUuid != null) {
                    Optional<Record> targetRecordOpt = recordRepository.findById(targetUuid);
                    if (targetRecordOpt.isEmpty()) {
                        violations.add(ReferenceIntegrityDto.OrphanReferenceItem.builder()
                                .sourceRecordId(r.getId())
                                .sourceRecordCode(recordCode)
                                .sourceFieldKey(fieldKey)
                                .targetRecordId(targetIdStr)
                                .issueType("TARGET_NOT_FOUND")
                                .message(String.format("참조 대상 레코드(%s)가 존재하지 않습니다 (고아 참조).", targetIdStr))
                                .build());
                    } else {
                        Record target = targetRecordOpt.get();
                        if ("REJECTED".equalsIgnoreCase(target.getStatus()) || "DELETED".equalsIgnoreCase(target.getStatus())) {
                            violations.add(ReferenceIntegrityDto.OrphanReferenceItem.builder()
                                    .sourceRecordId(r.getId())
                                    .sourceRecordCode(recordCode)
                                    .sourceFieldKey(fieldKey)
                                    .targetRecordId(targetIdStr)
                                    .issueType("TARGET_REJECTED")
                                    .message(String.format("참조 대상 레코드(%s)가 %s 상태입니다.", targetIdStr, target.getStatus()))
                                    .build());
                        }
                    }
                }
            }
        }

        int score = Math.max(0, 100 - violations.size() * 10);

        return ReferenceIntegrityDto.IntegrityReportResponse.builder()
                .domainId(domainId)
                .totalScannedRecords(records.size())
                .totalReferenceFields(refFieldKeys.size())
                .orphanCount(violations.size())
                .integrityScore(score)
                .violations(violations)
                .build();
    }

    private UUID parseUuidSafely(String str) {
        try {
            return UUID.fromString(str);
        } catch (Exception e) {
            return null;
        }
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
