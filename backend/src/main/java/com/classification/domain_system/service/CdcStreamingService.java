package com.classification.domain_system.service;

import com.classification.domain_system.dto.CdcStreamDto;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CdcStreamingService {

    private final RecordHistoryRepository recordHistoryRepository;
    private final ObjectMapper objectMapper;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @org.springframework.context.annotation.Lazy
    private RecordService recordService;

    @Transactional(readOnly = true)
    public CdcStreamDto.CdcStreamResponse getLiveCdcEvents(UUID domainId, UUID recordId) {
        if (domainId == null && recordId == null) {
            return CdcStreamDto.CdcStreamResponse.builder()
                    .events(Collections.emptyList())
                    .activeOffset(0L)
                    .eventsPerSecond(0.0)
                    .build();
        }

        List<RecordHistory> histories;
        if (recordId != null) {
            histories = recordHistoryRepository.findByRecordIdOrderByChangedAtDesc(recordId);
        } else {
            histories = recordHistoryRepository.findTop50ByDomainIdOrderByChangedAtDesc(domainId);
        }
        List<CdcStreamDto.CdcEventItem> events = new ArrayList<>();

        for (RecordHistory h : histories) {
            String op = "u";
            if ("CREATE".equalsIgnoreCase(h.getChangeType())) {
                op = "c";
            } else if ("DELETE".equalsIgnoreCase(h.getChangeType())) {
                op = "d";
            }

            String historyIdStr = h.getId() != null ? h.getId().toString() : UUID.randomUUID().toString();
            String recordIdStr = h.getRecordId() != null ? h.getRecordId().toString() : "00000000";

            String eventId = "CDC-" + (historyIdStr.length() >= 8 ? historyIdStr.substring(0, 8) : historyIdStr);
            String recordCode = "REC-" + (recordIdStr.length() >= 8 ? recordIdStr.substring(0, 8) : recordIdStr);

            String domainCode = "DOM-" + (domainId != null ? (domainId.toString().length() >= 8 ? domainId.toString().substring(0, 8) : domainId.toString()) : "GLOBAL");
            String connector = (h.getSourceSystem() != null && !h.getSourceSystem().isBlank()) ? h.getSourceSystem() : "postgres-wal-cdc";

            UUID nodeId = (h.getRecord() != null && h.getRecord().getNode() != null) ? h.getRecord().getNode().getId() : null;
            String rawPrev = h.getPreviousData();
            String rawNew = h.getNewData();

            List<String> changedFields = (recordService != null) ? recordService.computeChangedFieldKeys(rawPrev, rawNew) : Collections.emptyList();

            String maskedPrev = (nodeId != null && recordService != null) ? recordService.processDataForRead(nodeId, rawPrev) : rawPrev;
            String maskedNew = (nodeId != null && recordService != null) ? recordService.processDataForRead(nodeId, rawNew) : rawNew;

            events.add(CdcStreamDto.CdcEventItem.builder()
                    .eventId(eventId)
                    .timestamp(h.getChangedAt() != null ? h.getChangedAt() : LocalDateTime.now())
                    .operation(op)
                    .domainCode(domainCode)
                    .recordCode(recordCode)
                    .sourceConnector(connector)
                    .beforePayload(parseJsonToMap(maskedPrev))
                    .afterPayload(parseJsonToMap(maskedNew))
                    .changedFields(changedFields)
                    .build());
        }

        long activeOffset = (long) events.size();
        double eps = events.isEmpty() ? 0.0 : Math.round((events.size() / 60.0) * 100.0) / 100.0;

        return CdcStreamDto.CdcStreamResponse.builder()
                .events(events)
                .activeOffset(activeOffset)
                .eventsPerSecond(eps)
                .build();
    }

    private Map<String, Object> parseJsonToMap(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.debug("Failed to parse history data JSON: {}", jsonStr, e);
            return Map.of("raw", jsonStr);
        }
    }
}
