package com.classification.domain_system.service;

import com.classification.domain_system.entity.*;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.event.MasterDataChangedEvent;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordMergeService {

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final SurvivorshipRuleRepository survivorshipRuleRepository;
    private final SourcePriorityRepository sourcePriorityRepository;
    private final RecordFieldSourceRepository recordFieldSourceRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class MergeRequest {
        public UUID survivorRecordId;
        public List<UUID> mergedRecordIds;
        public Map<String, UUID> fieldResolutions; // fieldKey -> selected source RecordId
    }

    private UUID parseUserUuid(String username) {
        if (username == null || username.isBlank()) return null;
        try {
            return UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Transactional
    public Record mergeRecords(MergeRequest request, String operatorUsername) {
        Record survivor = recordRepository.findById(request.survivorRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Survivor record not found"));

        if ("MERGED".equalsIgnoreCase(survivor.getStatus()) || "REJECTED".equalsIgnoreCase(survivor.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Cannot merge into an inactive/merged record.");
        }

        List<Record> mergedRecords = new ArrayList<>();
        if (request.mergedRecordIds != null) {
            for (UUID mergedId : request.mergedRecordIds) {
                if (mergedId.equals(survivor.getId())) continue;
                Record m = recordRepository.findById(mergedId)
                        .orElseThrow(() -> new ResourceNotFoundException("Merged record not found: " + mergedId));
                if (m.getNode() != null && survivor.getNode() != null && !m.getNode().getId().equals(survivor.getNode().getId())) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "Cannot merge records from a different node/domain.");
                }
                mergedRecords.add(m);
            }
        }

        String prevSurvivorData = survivor.getData();

        // 1. Build resolved data
        Map<String, Object> finalDataMap = new HashMap<>();
        try {
            Map<String, Object> survivorMap = objectMapper.readValue(prevSurvivorData != null ? prevSurvivorData : "{}", new TypeReference<Map<String, Object>>() {});
            finalDataMap.putAll(survivorMap);

            if (request.fieldResolutions != null) {
                for (Map.Entry<String, UUID> entry : request.fieldResolutions.entrySet()) {
                    String fieldKey = entry.getKey();
                    UUID chosenRecordId = entry.getValue();

                    Record chosen = recordRepository.findById(chosenRecordId).orElse(null);
                    if (chosen != null && chosen.getData() != null) {
                        Map<String, Object> chosenMap = objectMapper.readValue(chosen.getData(), new TypeReference<Map<String, Object>>() {});
                        if (chosenMap.containsKey(fieldKey)) {
                            finalDataMap.put(fieldKey, chosenMap.get(fieldKey));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[RecordMerge] Data resolution error", e);
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Error resolving merged record data.");
        }

        try {
            String newDataJson = objectMapper.writeValueAsString(finalDataMap);
            survivor.setData(newDataJson);
            int nextVer = (survivor.getVersion() != null ? survivor.getVersion() : 1) + 1;
            survivor.setVersion(nextVer);
            survivor.setUpdatedAt(LocalDateTime.now());
            recordRepository.save(survivor);

            // Update RecordFieldSource (field lineage) for survivor
            for (Map.Entry<String, Object> entry : finalDataMap.entrySet()) {
                String fieldKey = entry.getKey();
                UUID chosenRecordId = (request.fieldResolutions != null && request.fieldResolutions.containsKey(fieldKey))
                        ? request.fieldResolutions.get(fieldKey)
                        : survivor.getId();

                String chosenSourceSystem = null;
                RecordFieldSource chosenRfs = recordFieldSourceRepository.findByRecordIdAndFieldKey(chosenRecordId, fieldKey).orElse(null);
                if (chosenRfs != null && chosenRfs.getSourceSystem() != null) {
                    chosenSourceSystem = chosenRfs.getSourceSystem();
                } else {
                    Record chosenRec = recordRepository.findById(chosenRecordId).orElse(null);
                    if (chosenRec != null && chosenRec.getSourceSystem() != null) {
                        chosenSourceSystem = chosenRec.getSourceSystem();
                    }
                }
                if (chosenSourceSystem == null) {
                    chosenSourceSystem = survivor.getSourceSystem() != null ? survivor.getSourceSystem() : "SYSTEM";
                }

                RecordFieldSource survivorRfs = recordFieldSourceRepository
                        .findByRecordIdAndFieldKey(survivor.getId(), fieldKey)
                        .orElseGet(() -> {
                            RecordFieldSource newRfs = new RecordFieldSource();
                            newRfs.setRecordId(survivor.getId());
                            newRfs.setFieldKey(fieldKey);
                            return newRfs;
                        });
                survivorRfs.setSourceSystem(chosenSourceSystem);
                survivorRfs.setUpdatedAt(LocalDateTime.now());
                recordFieldSourceRepository.save(survivorRfs);
            }

            // History for Survivor
            RecordHistory survivorHistory = new RecordHistory();
            survivorHistory.setRecordId(survivor.getId());
            survivorHistory.setChangeType("MERGE_SURVIVOR");
            survivorHistory.setChangedBy(parseUserUuid(operatorUsername));
            survivorHistory.setPreviousData(prevSurvivorData);
            survivorHistory.setNewData(newDataJson);
            survivorHistory.setVersion(nextVer);
            recordHistoryRepository.save(survivorHistory);

            // Update Merged Records to MERGED status & RecordHistory
            for (Record m : mergedRecords) {
                String prevMergedData = m.getData();
                m.setStatus("MERGED");
                m.setMergedIntoRecordId(survivor.getId());
                m.setUpdatedAt(LocalDateTime.now());
                recordRepository.save(m);

                RecordHistory mHistory = new RecordHistory();
                mHistory.setRecordId(m.getId());
                mHistory.setChangeType("MERGED_INTO");
                mHistory.setChangedBy(parseUserUuid(operatorUsername));
                mHistory.setPreviousData(prevMergedData);
                mHistory.setNewData(null);
                mHistory.setVersion((m.getVersion() != null ? m.getVersion() : 1) + 1);
                recordHistoryRepository.save(mHistory);
            }

            // Reference Repointing for records referencing merged records
            List<FieldDefinition> refFields = fieldDefinitionRepository.findByType("DOMAIN_REFERENCE");
            for (Record m : mergedRecords) {
                for (FieldDefinition refField : refFields) {
                    String fieldKey = refField.getKey();
                    List<Record> referencingRecords = recordRepository.findReferencingRecords(fieldKey, m.getId().toString(), m.getId());
                    for (Record refRec : referencingRecords) {
                        String prevRefData = refRec.getData();
                        if (prevRefData == null) continue;
                        try {
                            Map<String, Object> refMap = objectMapper.readValue(prevRefData, new TypeReference<Map<String, Object>>() {});
                            if (m.getId().toString().equals(String.valueOf(refMap.get(fieldKey)))) {
                                refMap.put(fieldKey, survivor.getId().toString());
                                String newRefDataJson = objectMapper.writeValueAsString(refMap);
                                refRec.setData(newRefDataJson);
                                int nextRefVer = (refRec.getVersion() != null ? refRec.getVersion() : 1) + 1;
                                refRec.setVersion(nextRefVer);
                                refRec.setUpdatedAt(LocalDateTime.now());
                                recordRepository.save(refRec);

                                RecordHistory refHistory = new RecordHistory();
                                refHistory.setRecordId(refRec.getId());
                                refHistory.setChangeType("REFERENCE_REPOINTED");
                                refHistory.setChangedBy(parseUserUuid(operatorUsername));
                                refHistory.setPreviousData(prevRefData);
                                refHistory.setNewData(newRefDataJson);
                                refHistory.setVersion(nextRefVer);
                                recordHistoryRepository.save(refHistory);
                            }
                        } catch (Exception e) {
                            log.error("[RecordMerge] Error repointing reference for record: {}", refRec.getId(), e);
                        }
                    }
                }
            }

            // Publish MasterDataChangedEvent
            if (applicationEventPublisher != null) {
                UUID nodeId = survivor.getNode() != null ? survivor.getNode().getId() : null;
                applicationEventPublisher.publishEvent(new MasterDataChangedEvent(this, survivor.getId(), nodeId, "MERGE", survivor.getData()));
                for (Record m : mergedRecords) {
                    UUID mNodeId = m.getNode() != null ? m.getNode().getId() : null;
                    applicationEventPublisher.publishEvent(new MasterDataChangedEvent(this, m.getId(), mNodeId, "MERGED_INTO", m.getData()));
                }
            }

            log.info("[RecordMerge] Merged {} record(s) into survivor record ID: {}", mergedRecords.size(), survivor.getId());
            return survivor;
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("[RecordMerge] Save error", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to complete record merge.");
        }
    }

    @Transactional
    public Record unmergeRecord(UUID mergedRecordId, String operatorUsername) {
        Record m = recordRepository.findById(mergedRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found for unmerge: " + mergedRecordId));

        if (!"MERGED".equalsIgnoreCase(m.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Record is not in MERGED status.");
        }

        UUID survivorId = m.getMergedIntoRecordId();

        m.setStatus("ACTIVE");
        m.setMergedIntoRecordId(null);
        m.setUpdatedAt(LocalDateTime.now());
        recordRepository.save(m);

        RecordHistory mHistory = new RecordHistory();
        mHistory.setRecordId(m.getId());
        mHistory.setChangeType("UNMERGED");
        mHistory.setChangedBy(parseUserUuid(operatorUsername));
        mHistory.setPreviousData(null);
        mHistory.setNewData(m.getData());
        mHistory.setVersion((m.getVersion() != null ? m.getVersion() : 1) + 1);
        recordHistoryRepository.save(mHistory);

        // Roll back survivor data to pre-merge state (previousData of MERGE_SURVIVOR history)
        if (survivorId != null) {
            Record survivor = recordRepository.findById(survivorId).orElse(null);
            if (survivor != null) {
                List<RecordHistory> survivorHistories = recordHistoryRepository.findByRecordIdOrderByChangedAtDesc(survivorId);
                Optional<RecordHistory> mergeHist = survivorHistories.stream()
                        .filter(h -> "MERGE_SURVIVOR".equals(h.getChangeType()))
                        .findFirst();

                if (mergeHist.isPresent() && mergeHist.get().getPreviousData() != null) {
                    String currentSurvivorData = survivor.getData();
                    String rolledBackData = mergeHist.get().getPreviousData();
                    survivor.setData(rolledBackData);
                    int nextVer = (survivor.getVersion() != null ? survivor.getVersion() : 1) + 1;
                    survivor.setVersion(nextVer);
                    survivor.setUpdatedAt(LocalDateTime.now());
                    recordRepository.save(survivor);

                    /*
                     * 트레이드오프: 언머지 시 survivor 레코드 전체를 병합 이전 시점의 previousData로 복원합니다.
                     * 병합 이후 별도의 추가 수정 작업이 있었을 경우 해당 변경사항도 함께 이전 상태로 되돌아가는 한계가 존재합니다.
                     */
                    RecordHistory survivorRollbackHistory = new RecordHistory();
                    survivorRollbackHistory.setRecordId(survivor.getId());
                    survivorRollbackHistory.setChangeType("UNMERGE_SURVIVOR_ROLLBACK");
                    survivorRollbackHistory.setChangedBy(parseUserUuid(operatorUsername));
                    survivorRollbackHistory.setPreviousData(currentSurvivorData);
                    survivorRollbackHistory.setNewData(rolledBackData);
                    survivorRollbackHistory.setVersion(nextVer);
                    recordHistoryRepository.save(survivorRollbackHistory);
                }
            }
        }

        // Publish MasterDataChangedEvent for unmerge
        if (applicationEventPublisher != null) {
            UUID mNodeId = m.getNode() != null ? m.getNode().getId() : null;
            applicationEventPublisher.publishEvent(new MasterDataChangedEvent(this, m.getId(), mNodeId, "UNMERGE", m.getData()));
        }

        log.info("[RecordMerge] Successfully unmerged record ID: {}", mergedRecordId);
        return m;
    }

    @Transactional
    public Record mergeWithSurvivorship(UUID survivorId, List<UUID> mergedIds, String operatorUsername) {
        Record survivor = recordRepository.findById(survivorId)
                .orElseThrow(() -> new ResourceNotFoundException("Survivor record not found"));

        if (survivor.getNode() == null || survivor.getNode().getDomain() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Survivor record does not belong to a valid node/domain.");
        }

        UUID domainId = survivor.getNode().getDomain().getId();
        List<SurvivorshipRule> rules = survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId);

        List<Record> candidateRecords = new ArrayList<>();
        candidateRecords.add(survivor);
        if (mergedIds != null) {
            for (UUID mId : mergedIds) {
                if (mId.equals(survivorId)) continue;
                Record m = recordRepository.findById(mId)
                        .orElseThrow(() -> new ResourceNotFoundException("Merged record not found: " + mId));
                candidateRecords.add(m);
            }
        }

        Map<UUID, Map<String, Object>> candidateDataMaps = new HashMap<>();
        for (Record cand : candidateRecords) {
            try {
                Map<String, Object> dMap = objectMapper.readValue(cand.getData() != null ? cand.getData() : "{}", new TypeReference<Map<String, Object>>() {});
                candidateDataMaps.put(cand.getId(), dMap);
            } catch (Exception e) {
                log.error("[RecordMerge] Failed to parse candidate record data: {}", cand.getId(), e);
            }
        }

        List<SourcePriority> sourcePriorities = sourcePriorityRepository.findByDomainIdOrderByPriorityAsc(domainId);

        Map<String, UUID> fieldResolutions = new HashMap<>();

        for (SurvivorshipRule rule : rules) {
            String fieldKey = rule.getFieldKey();
            if (fieldKey == null || fieldKey.isBlank()) continue;
            String strategy = rule.getStrategy();
            if (strategy == null) continue;

            List<Record> validCandidates = candidateRecords.stream()
                    .filter(cand -> {
                        Map<String, Object> map = candidateDataMaps.get(cand.getId());
                        if (map == null || !map.containsKey(fieldKey)) return false;
                        Object val = map.get(fieldKey);
                        return val != null && !val.toString().isBlank();
                    })
                    .toList();

            if (validCandidates.isEmpty()) {
                continue;
            }

            Record winningRecord = null;

            if ("SOURCE_PRIORITY".equalsIgnoreCase(strategy)) {
                int bestPriority = Integer.MAX_VALUE;
                for (Record cand : validCandidates) {
                    RecordFieldSource fieldSource = recordFieldSourceRepository.findByRecordIdAndFieldKey(cand.getId(), fieldKey).orElse(null);
                    String sys = fieldSource != null ? fieldSource.getSourceSystem() : cand.getSourceSystem();

                    int priority = sourcePriorities.stream()
                            .filter(sp -> sp.getSourceSystem() != null && sp.getSourceSystem().equalsIgnoreCase(sys))
                            .map(SourcePriority::getPriority)
                            .findFirst()
                            .orElse(999);

                    if (priority < bestPriority) {
                        bestPriority = priority;
                        winningRecord = cand;
                    } else if (priority == bestPriority && winningRecord != null && cand.getId().equals(survivorId)) {
                        winningRecord = cand;
                    }
                }
            } else if ("MOST_RECENT".equalsIgnoreCase(strategy)) {
                LocalDateTime latestTime = null;
                for (Record cand : validCandidates) {
                    RecordFieldSource fieldSource = recordFieldSourceRepository.findByRecordIdAndFieldKey(cand.getId(), fieldKey).orElse(null);
                    LocalDateTime candTime = fieldSource != null && fieldSource.getUpdatedAt() != null ? fieldSource.getUpdatedAt() : cand.getUpdatedAt();
                    if (candTime == null) candTime = LocalDateTime.MIN;

                    if (latestTime == null || candTime.isAfter(latestTime)) {
                        latestTime = candTime;
                        winningRecord = cand;
                    } else if (candTime.isEqual(latestTime) && winningRecord != null && cand.getId().equals(survivorId)) {
                        winningRecord = cand;
                    }
                }
            } else if ("MOST_COMPLETE".equalsIgnoreCase(strategy)) {
                /* non-null/non-blank 값 우선, 동률 시 문자열 길이/survivor 우선 */
                int maxLength = -1;
                for (Record cand : validCandidates) {
                    Object val = candidateDataMaps.get(cand.getId()).get(fieldKey);
                    String valStr = val != null ? val.toString() : "";
                    int len = valStr.length();
                    if (len > maxLength) {
                        maxLength = len;
                        winningRecord = cand;
                    } else if (len == maxLength && winningRecord != null && cand.getId().equals(survivorId)) {
                        winningRecord = cand;
                    }
                }
            }

            if (winningRecord != null) {
                fieldResolutions.put(fieldKey, winningRecord.getId());
            }
        }

        MergeRequest req = new MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = mergedIds;
        req.fieldResolutions = fieldResolutions;
        return mergeRecords(req, operatorUsername);
    }

    @Transactional(readOnly = true)
    public List<SurvivorshipRule> getSurvivorshipRules(UUID domainId) {
        return survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId);
    }

    @Transactional
    public void updateSurvivorshipRules(UUID domainId, List<SurvivorshipRule> rules) {
        List<SurvivorshipRule> existing = survivorshipRuleRepository.findByDomainIdOrderByPriorityAsc(domainId);
        survivorshipRuleRepository.deleteAll(existing);
        rules.forEach(r -> {
            r.setDomainId(domainId);
            survivorshipRuleRepository.save(r);
        });
    }
}
