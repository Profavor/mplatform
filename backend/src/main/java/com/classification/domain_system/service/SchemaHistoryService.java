package com.classification.domain_system.service;

import com.classification.domain_system.entity.SchemaHistory;
import com.classification.domain_system.repository.ClassificationNodeRepository;
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
    private final ClassificationNodeRepository nodeRepository;
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private FieldDefinitionService fieldDefinitionService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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

    @Transactional(readOnly = true)
    public java.util.List<com.classification.domain_system.entity.FieldDefinition> getEffectiveFieldsAsOf(UUID nodeId, java.time.LocalDateTime asOf) {
        if (nodeId == null || asOf == null) {
            return java.util.Collections.emptyList();
        }

        com.classification.domain_system.entity.ClassificationNode node = nodeRepository != null ? nodeRepository.findById(nodeId).orElse(null) : null;
        if (node == null || node.getDomain() == null) {
            return java.util.Collections.emptyList();
        }

        java.util.List<com.classification.domain_system.entity.FieldDefinition> currentFields = fieldDefinitionService != null ? fieldDefinitionService.getEffectiveFields(nodeId) : java.util.Collections.emptyList();
        java.util.Map<UUID, com.classification.domain_system.entity.FieldDefinition> fieldMap = new java.util.HashMap<>();
        if (currentFields != null) {
            for (com.classification.domain_system.entity.FieldDefinition f : currentFields) {
                if (f != null && f.getId() != null) {
                    fieldMap.put(f.getId(), f);
                }
            }
        }

        java.util.List<SchemaHistory> histories = repository.findByDomainIdAndChangedAtAfterOrderByChangedAtDesc(node.getDomain().getId(), asOf);
        if (histories != null) {
            for (SchemaHistory h : histories) {
                if (!"FIELD".equalsIgnoreCase(h.getTargetType())) continue;
                UUID fieldId = h.getTargetId();
                if ("CREATE".equalsIgnoreCase(h.getAction())) {
                    fieldMap.remove(fieldId);
                } else if ("UPDATE".equalsIgnoreCase(h.getAction())) {
                    if (h.getBeforeData() != null && fieldMap.containsKey(fieldId)) {
                        try {
                            com.classification.domain_system.entity.FieldDefinition field = fieldMap.get(fieldId);
                            com.fasterxml.jackson.databind.JsonNode beforeNode = objectMapper.readTree(h.getBeforeData());
                            if (beforeNode.has("name") && !beforeNode.get("name").isNull()) {
                                java.util.Map<String, String> nameMap = objectMapper.convertValue(beforeNode.get("name"), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                                field.setName(nameMap);
                            }
                            if (beforeNode.has("key") && !beforeNode.get("key").isNull()) {
                                field.setKey(beforeNode.get("key").asText());
                            }
                        } catch (Exception e) {
                            log.warn("[SchemaHistory] Failed to deserialize beforeData for field {}", fieldId, e);
                        }
                    }
                } else if ("DELETE".equalsIgnoreCase(h.getAction())) {
                    if (h.getBeforeData() != null) {
                        try {
                            com.classification.domain_system.entity.FieldDefinition restoredField = new com.classification.domain_system.entity.FieldDefinition();
                            restoredField.setId(fieldId);
                            com.fasterxml.jackson.databind.JsonNode beforeNode = objectMapper.readTree(h.getBeforeData());
                            if (beforeNode.has("name") && !beforeNode.get("name").isNull()) {
                                java.util.Map<String, String> nameMap = objectMapper.convertValue(beforeNode.get("name"), new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, String>>() {});
                                restoredField.setName(nameMap);
                            }
                            if (beforeNode.has("key") && !beforeNode.get("key").isNull()) {
                                restoredField.setKey(beforeNode.get("key").asText());
                            }
                            fieldMap.put(fieldId, restoredField);
                        } catch (Exception e) {
                            log.warn("[SchemaHistory] Failed to restore deleted field {}", fieldId, e);
                        }
                    }
                }
            }
        }

        return new java.util.ArrayList<>(fieldMap.values());
    }
}

