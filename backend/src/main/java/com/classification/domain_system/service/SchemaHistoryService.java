package com.classification.domain_system.service;

import com.classification.domain_system.entity.SchemaHistory;
import com.classification.domain_system.repository.SchemaHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchemaHistoryService {

    private final SchemaHistoryRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()).disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Transactional
    public SchemaHistory recordChange(UUID domainId, String targetType, UUID targetId, 
                                      String action, Object beforeData, Object afterData, 
                                      String changedBy) {
        SchemaHistory history = new SchemaHistory();
        history.setDomainId(domainId);
        history.setTargetType(targetType);
        history.setTargetId(targetId);
        history.setAction(action);
        try {
            history.setBeforeData(beforeData != null ? (beforeData instanceof String ? (String) beforeData : objectMapper.writeValueAsString(beforeData)) : null);
        } catch (Exception e) {
            log.error("[SchemaHistory] Failed to serialize before data", e);
        }
        try {
            history.setAfterData(afterData != null ? (afterData instanceof String ? (String) afterData : objectMapper.writeValueAsString(afterData)) : null);
        } catch (Exception e) {
            log.error("[SchemaHistory] Failed to serialize after data", e);
        }
        history.setChangedBy(changedBy != null ? changedBy : "SYSTEM");
        return repository.save(history);
    }

    @Transactional(readOnly = true)
    public java.util.List<SchemaHistory> getDomainHistory(UUID domainId) {
        return repository.findByDomainIdOrderByChangedAtDesc(domainId);
    }

}

