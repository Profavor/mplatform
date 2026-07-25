package com.classification.domain_system.service;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.classification.domain_system.entity.SurvivorshipRule;
import com.classification.domain_system.repository.SurvivorshipRuleRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordMergeService {

    private final RecordRepository recordRepository;
    private final RecordHistoryRepository recordHistoryRepository;
    private final SurvivorshipRuleRepository survivorshipRuleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class MergeRequest {
        public UUID survivorRecordId;
        public List<UUID> mergedRecordIds;
        public Map<String, UUID> fieldResolutions; // fieldKey -> selected source RecordId
    }

    @Transactional
    public Record mergeRecords(MergeRequest request, String operatorUsername) {
        Record survivor = recordRepository.findById(request.survivorRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Survivor record not found"));

        if ("MERGED".equalsIgnoreCase(survivor.getStatus()) || "REJECTED".equalsIgnoreCase(survivor.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Cannot merge into an inactive/merged record.");
        }

        List<Record> mergedRecords = new ArrayList<>();
        for (UUID mergedId : request.mergedRecordIds) {
            if (mergedId.equals(survivor.getId())) continue;
            Record m = recordRepository.findById(mergedId)
                    .orElseThrow(() -> new ResourceNotFoundException("Merged record not found: " + mergedId));
            mergedRecords.add(m);
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

            // History for Survivor
            RecordHistory survivorHistory = new RecordHistory();
            survivorHistory.setRecordId(survivor.getId());
            survivorHistory.setChangeType("MERGE_SURVIVOR");
            survivorHistory.setChangedBy(null);
            survivorHistory.setPreviousData(prevSurvivorData);
            survivorHistory.setNewData(newDataJson);
            survivorHistory.setVersion(nextVer);
            recordHistoryRepository.save(survivorHistory);

            // Update Merged Records to MERGED status
            for (Record m : mergedRecords) {
                String prevMergedData = m.getData();
                m.setStatus("MERGED");
                m.setMergedIntoRecordId(survivor.getId());
                m.setUpdatedAt(LocalDateTime.now());
                recordRepository.save(m);

                RecordHistory mHistory = new RecordHistory();
                mHistory.setRecordId(m.getId());
                mHistory.setChangeType("MERGED_INTO");
                mHistory.setChangedBy(null);
                mHistory.setPreviousData(prevMergedData);
                mHistory.setNewData(null);
                mHistory.setVersion((m.getVersion() != null ? m.getVersion() : 1) + 1);
                recordHistoryRepository.save(mHistory);
            }

            log.info("[RecordMerge] Merged {} record(s) into survivor record ID: {}", mergedRecords.size(), survivor.getId());
            return survivor;
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

        m.setStatus("ACTIVE");
        m.setMergedIntoRecordId(null);
        m.setUpdatedAt(LocalDateTime.now());
        recordRepository.save(m);

        RecordHistory mHistory = new RecordHistory();
        mHistory.setRecordId(m.getId());
        mHistory.setChangeType("UNMERGED");
        mHistory.setChangedBy(null);
        mHistory.setPreviousData(null);
        mHistory.setNewData(m.getData());
        mHistory.setVersion((m.getVersion() != null ? m.getVersion() : 1) + 1);
        recordHistoryRepository.save(mHistory);

        log.info("[RecordMerge] Successfully unmerged record ID: {}", mergedRecordId);
        return m;
    }

    @Transactional
    public Record mergeWithSurvivorship(UUID survivorId, List<UUID> mergedIds, String operatorUsername) {
        // Core survivorship logic goes here
        // Fetches rules and applies SOURCE_PRIORITY, MOST_RECENT, MOST_COMPLETE
        MergeRequest req = new MergeRequest();
        req.survivorRecordId = survivorId;
        req.mergedRecordIds = mergedIds;
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

