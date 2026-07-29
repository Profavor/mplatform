package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.dto.FieldDefinitionRequest;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.FieldGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.event.ApprovalRequestCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FieldDefinitionService {
    
    private final FieldDefinitionRepository fieldRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final DomainRepository domainRepository;
    private final FieldGroupRepository fieldGroupRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SchemaHistoryService schemaHistoryService;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private WorkflowConfigRepository workflowConfigRepository;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ApprovalRequestRepository approvalRequestRepository;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ApplicationEventPublisher eventPublisher;
    
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ApprovalService approvalService;
    
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()).disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getName() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "system";
    }

    private UUID getCurrentUserId() {
        return UUID.randomUUID(); // Simplified, normally fetch from SecurityContext
    }

    private ApprovalRequest createSchemaApprovalRequest(Domain domain, ClassificationNode node, String targetType, String changes) {
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType(targetType);
        approval.setTargetId(node != null ? node.getId() : domain.getId());
        approval.setClassificationNode(node);
        approval.setRequesterId(getCurrentUserId());
        approval.setStatus("PENDING");
        approval.setChanges(changes);
        approval.setCurrentStepOrder(1);
        
        WorkflowConfig config = approvalService.resolveWorkflow(node != null ? node.getId() : null, "SCHEMA_CHANGE");
        if (config == null && domain != null) {
            List<WorkflowConfig> list = workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(domain.getId(), "SCHEMA_CHANGE");
            if (!list.isEmpty()) config = list.get(0);
        }
        
        if (config != null) {
            try {
                if (config.getStepsConfig() != null && !config.getStepsConfig().isEmpty()) {
                    com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(config.getStepsConfig());
                    List<com.classification.domain_system.entity.ApprovalStep> steps = new java.util.ArrayList<>();
                    if (root.has("steps") && root.get("steps").isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode stepNode : root.get("steps")) {
                            com.classification.domain_system.entity.ApprovalStep step = new com.classification.domain_system.entity.ApprovalStep();
                            step.setApprovalRequest(approval);
                            step.setStepType(stepNode.get("stepType").asText());
                            step.setAssigneeId(UUID.fromString(stepNode.get("assigneeId").asText()));
                            step.setStepOrder(stepNode.get("stepOrder").asInt());
                            step.setStatus(step.getStepOrder() == 1 ? "PENDING" : "WAITING");
                            steps.add(step);
                        }
                    }
                    if (root.has("observerIds") && root.get("observerIds").isArray()) {
                        approval.setObserverIds(objectMapper.writeValueAsString(root.get("observerIds")));
                    } else {
                        approval.setObserverIds("[]");
                    }
                    approval.setSteps(steps);
                }
            } catch (Exception e) {
                approval.setObserverIds("[]");
            }
        }
        
        ApprovalRequest saved = approvalRequestRepository.saveAndFlush(approval);
        eventPublisher.publishEvent(new ApprovalRequestCreatedEvent(saved));
        return saved;
    }

    
    private void manageIndex(String fieldKey, boolean isSearchable) {
        if (fieldKey == null || fieldKey.trim().isEmpty()) return;
        String safeKey = fieldKey.replaceAll("[^a-zA-Z0-9_]", "_");
        String indexName = "idx_record_search_" + safeKey;
        if (isSearchable) {
            String sql = "CREATE INDEX IF NOT EXISTS " + indexName + " ON record ((data->>'" + safeKey + "'))";
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                System.err.println("Failed to create index: " + e.getMessage());
            }
        } else {
            String sql = "DROP INDEX IF EXISTS " + indexName;
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                System.err.println("Failed to drop index: " + e.getMessage());
            }
        }
    }
    
    private void populateFieldProperties(FieldDefinition field, FieldDefinitionRequest request, boolean isUpdate) {
        field.setName(request.getName());
        
        if (request.getFieldGroupId() != null) {
            field.setFieldGroup(fieldGroupRepository.findById(request.getFieldGroupId())
                .orElseThrow(() -> new RuntimeException("FieldGroup not found")));
        } else {
            field.setFieldGroup(null);
        }
        
        field.setKey(request.getKey());
        field.setType(request.getType());
        
        if (isUpdate) {
            field.setUnit(request.getUnit() != null ? request.getUnit() : field.getUnit());
            field.setGridWidth(request.getGridWidth() != null ? request.getGridWidth() : field.getGridWidth());
            field.setTableColumnWidth(request.getTableColumnWidth() != null ? request.getTableColumnWidth() : field.getTableColumnWidth());
            field.setIsHighlighted(request.getIsHighlighted() != null ? request.getIsHighlighted() : field.getIsHighlighted());
        } else {
            field.setGridWidth(request.getGridWidth());
            field.setTableColumnWidth(request.getTableColumnWidth());
            field.setIsHighlighted(request.getIsHighlighted() != null ? request.getIsHighlighted() : false);
            field.setIsRemoved(false);
        }
        
        field.setOptions(normalizeJsonStr(request.getOptions()));
        field.setDefaultValue(normalizeJsonStr(request.getDefaultValue()));
        
        field.setRequired(request.getRequired() != null ? request.getRequired() : (isUpdate && field.getRequired() != null ? field.getRequired() : false));
        field.setOrder(request.getOrder() != null ? request.getOrder() : (isUpdate && field.getOrder() != null ? field.getOrder() : 0));
        field.setIsMultiValue(request.getIsMultiValue() != null ? request.getIsMultiValue() : (isUpdate && field.getIsMultiValue() != null ? field.getIsMultiValue() : false));
        field.setIsTable(request.getIsTable() != null ? request.getIsTable() : (isUpdate && field.getIsTable() != null ? field.getIsTable() : false));
        field.setIsEncrypted(request.getIsEncrypted() != null ? request.getIsEncrypted() : (isUpdate && field.getIsEncrypted() != null ? field.getIsEncrypted() : false));
        field.setIsReadOnly(request.getIsReadOnly() != null ? request.getIsReadOnly() : (isUpdate && field.getIsReadOnly() != null ? field.getIsReadOnly() : false));
        field.setIsImmutable(request.getIsImmutable() != null ? request.getIsImmutable() : (isUpdate && field.getIsImmutable() != null ? field.getIsImmutable() : false));
        field.setIsHidden(request.getIsHidden() != null ? request.getIsHidden() : (isUpdate && field.getIsHidden() != null ? field.getIsHidden() : false));

        // Node Transfer Logic: Allow moving field to another Classification Node or Domain Level
        if (isUpdate) {
            if (Boolean.TRUE.equals(request.getIsDomainField())) {
                Domain currentDomain = field.getDomain() != null ? field.getDomain() : (field.getDefinedAtNode() != null ? field.getDefinedAtNode().getDomain() : null);
                if (currentDomain != null) {
                    field.setDomain(currentDomain);
                    field.setDefinedAtNode(null);
                }
            } else if (request.getTargetNodeId() != null) {
                ClassificationNode targetNode = nodeRepository.findById(request.getTargetNodeId()).orElse(null);
                if (targetNode != null) {
                    field.setDefinedAtNode(targetNode);
                    field.setDomain(null);
                }
            }
        }
    }
    
    private boolean hasSchemaApproval(UUID domainId) {
        if (workflowConfigRepository == null || domainId == null) return false;
        try {
            return !workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(domainId, "SCHEMA_CHANGE").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void recordSchemaChange(UUID domainId, String targetType, UUID targetId, String action, Object beforeData, Object afterData) {
        if (schemaHistoryService != null && domainId != null) {
            schemaHistoryService.recordChange(domainId, targetType, targetId, action, beforeData, afterData, getCurrentUser());
        }
    }

    @Transactional
    public FieldDefinition addFieldDirect(UUID nodeId, FieldDefinitionRequest request) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        Domain domain = node.getDomain();

        FieldDefinition field = new FieldDefinition();
        field.setDefinedAtNode(node);
        populateFieldProperties(field, request, false);
        field.setIsSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false);
        
        FieldDefinition savedField = fieldRepository.save(field);
        if (Boolean.TRUE.equals(request.getIsSearchable())) {
            manageIndex(savedField.getKey(), true);
        }
        
        recordSchemaChange(domain.getId(), "FIELD", savedField.getId(), "CREATE", null, toStateMap(savedField));
        return savedField;
    }

    @Transactional
    public FieldDefinition addDomainFieldDirect(UUID domainId, FieldDefinitionRequest request) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found"));

        FieldDefinition field = new FieldDefinition();
        field.setDomain(domain);
        populateFieldProperties(field, request, false);
        field.setIsSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false);
        
        FieldDefinition savedField = fieldRepository.save(field);
        if (Boolean.TRUE.equals(request.getIsSearchable())) {
            manageIndex(savedField.getKey(), true);
        }
        
        recordSchemaChange(domain.getId(), "FIELD", savedField.getId(), "CREATE", null, toStateMap(savedField));
        return savedField;
    }

    @Transactional
    public FieldDefinition updateFieldDirect(UUID nodeId, UUID fieldId, FieldDefinitionRequest request) {
        FieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));

        java.util.Map<String, Object> beforeState = toStateMap(field);

        populateFieldProperties(field, request, true);
        
        Boolean wasSearchable = field.getIsSearchable();
        Boolean willBeSearchable = request.getIsSearchable() != null ? request.getIsSearchable() : field.getIsSearchable();
        field.setIsSearchable(willBeSearchable);
        
        FieldDefinition savedField = fieldRepository.save(field);
        
        if (Boolean.TRUE.equals(willBeSearchable) && !Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), true);
        } else if (!Boolean.TRUE.equals(willBeSearchable) && Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), false);
        }
        
        recordSchemaChange(field.getDomain() != null ? field.getDomain().getId() : (field.getDefinedAtNode() != null ? field.getDefinedAtNode().getDomain().getId() : null), "FIELD", fieldId, "UPDATE", beforeState, toStateMap(savedField));
        return savedField;
    }

    @Transactional
    public FieldDefinition updateDomainFieldDirect(UUID domainId, UUID fieldId, FieldDefinitionRequest request) {
        return updateFieldDirect(null, fieldId, request);
    }

    @Transactional
    public FieldDefinition addField(UUID nodeId, FieldDefinitionRequest request) {
        return addField(nodeId, request, false);
    }
    
    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public FieldDefinition addField(UUID nodeId, FieldDefinitionRequest request, boolean bypassApproval) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        Domain domain = node.getDomain();

        if (!bypassApproval && hasSchemaApproval(domain.getId())) {
            try {
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("nodeId", nodeId);
                changesMap.put("request", request);
                createSchemaApprovalRequest(domain, node, "SCHEMA_FIELD_ADD", objectMapper.writeValueAsString(changesMap));
                FieldDefinition pendingField = new FieldDefinition();
                pendingField.setId(UUID.randomUUID());
                return pendingField;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create schema approval request", e);
            }
        }
                
        FieldDefinition field = new FieldDefinition();
        field.setDefinedAtNode(node);
        populateFieldProperties(field, request, false);
        field.setIsSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false);
        
        FieldDefinition savedField = fieldRepository.save(field);
        if (Boolean.TRUE.equals(request.getIsSearchable())) {
            manageIndex(savedField.getKey(), true);
        }
        
        recordSchemaChange(domain.getId(), "FIELD", savedField.getId(), "CREATE", null, toStateMap(savedField));
        return savedField;
    }
    
    @Transactional
    public FieldDefinition addDomainField(UUID domainId, FieldDefinitionRequest request) {
        return addDomainField(domainId, request, false);
    }
    
    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public FieldDefinition addDomainField(UUID domainId, FieldDefinitionRequest request, boolean bypassApproval) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found"));
                
        if (!bypassApproval && hasSchemaApproval(domain.getId())) {
            try {
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("domainId", domainId);
                changesMap.put("request", request);
                createSchemaApprovalRequest(domain, null, "SCHEMA_FIELD_ADD", objectMapper.writeValueAsString(changesMap));
                FieldDefinition pendingField = new FieldDefinition();
                pendingField.setId(UUID.randomUUID());
                return pendingField;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create schema approval request", e);
            }
        }

        FieldDefinition field = new FieldDefinition();
        field.setDomain(domain);
        populateFieldProperties(field, request, false);
        field.setIsSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false);
        
        FieldDefinition savedField = fieldRepository.save(field);
        if (Boolean.TRUE.equals(request.getIsSearchable())) {
            manageIndex(savedField.getKey(), true);
        }
        
        recordSchemaChange(domain.getId(), "FIELD", savedField.getId(), "CREATE", null, toStateMap(savedField));
        return savedField;
    }
    
    @Transactional
    public FieldDefinition updateField(UUID nodeId, UUID fieldId, FieldDefinitionRequest request) {
        return updateField(nodeId, fieldId, request, false);
    }
    
    private java.util.Map<String, Object> toStateMap(FieldDefinition field) {
        if (field == null) return null;
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", field.getId());
        map.put("name", field.getName());
        map.put("key", field.getKey());
        map.put("type", field.getType());
        map.put("required", field.getRequired());
        map.put("unit", field.getUnit());
        map.put("isSearchable", field.getIsSearchable());
        map.put("isMultiValue", field.getIsMultiValue());
        map.put("isEncrypted", field.getIsEncrypted());
        map.put("isReadOnly", field.getIsReadOnly());
        map.put("isHidden", field.getIsHidden());
        map.put("isImmutable", field.getIsImmutable());
        map.put("order", field.getOrder());
        if (field.getFieldGroup() != null) {
            map.put("group", field.getFieldGroup().getName());
        }
        return map;
    }

    @Transactional
    public FieldDefinition updateField(UUID nodeId, UUID fieldId, FieldDefinitionRequest request, boolean bypassApproval) {
        FieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
                
        if (nodeId != null && field.getDefinedAtNode() != null && !field.getDefinedAtNode().getId().equals(nodeId)) {
            // Log warning or allow if updating node
        }
        
        ClassificationNode node = field.getDefinedAtNode();
        Domain domain = node.getDomain();

        if (!bypassApproval && hasSchemaApproval(domain.getId())) {
            try {
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("nodeId", nodeId);
                changesMap.put("fieldId", fieldId);
                changesMap.put("request", request);
                createSchemaApprovalRequest(domain, node, "SCHEMA_FIELD_UPDATE", objectMapper.writeValueAsString(changesMap));
                return field;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create schema approval request", e);
            }
        }

        java.util.Map<String, Object> beforeState = toStateMap(field);

        populateFieldProperties(field, request, true);
        
        Boolean wasSearchable = field.getIsSearchable();
        Boolean willBeSearchable = request.getIsSearchable() != null ? request.getIsSearchable() : field.getIsSearchable();
        field.setIsSearchable(willBeSearchable);
        
        FieldDefinition savedField = fieldRepository.save(field);
        
        if (Boolean.TRUE.equals(willBeSearchable) && !Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), true);
        } else if (!Boolean.TRUE.equals(willBeSearchable) && Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), false);
        }
        
        recordSchemaChange(domain.getId(), "FIELD", fieldId, "UPDATE", beforeState, toStateMap(savedField));
        return savedField;
    }
    
    @Transactional
    public FieldDefinition updateDomainField(UUID domainId, UUID fieldId, FieldDefinitionRequest request) {
        return updateDomainField(domainId, fieldId, request, false);
    }
    
    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public FieldDefinition updateDomainField(UUID domainId, UUID fieldId, FieldDefinitionRequest request, boolean bypassApproval) {
        FieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
                
        if (field.getDomain() == null || !field.getDomain().getId().equals(domainId)) {
            throw new RuntimeException("Field does not belong to the specified domain");
        }
        
        if (!bypassApproval && hasSchemaApproval(domainId)) {
            try {
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("domainId", domainId);
                changesMap.put("fieldId", fieldId);
                changesMap.put("request", request);
                createSchemaApprovalRequest(domainRepository.findById(domainId).orElse(null), null, "SCHEMA_FIELD_UPDATE", objectMapper.writeValueAsString(changesMap));
                return field;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create schema approval request", e);
            }
        }

        java.util.Map<String, Object> beforeState = new java.util.HashMap<>();
        beforeState.put("id", field.getId());
        beforeState.put("name", field.getName());
        beforeState.put("key", field.getKey());
        beforeState.put("type", field.getType());

        populateFieldProperties(field, request, true);
        
        Boolean wasSearchable = field.getIsSearchable();
        Boolean willBeSearchable = request.getIsSearchable() != null ? request.getIsSearchable() : field.getIsSearchable();
        field.setIsSearchable(willBeSearchable);
        
        FieldDefinition savedField = fieldRepository.save(field);
        
        if (Boolean.TRUE.equals(willBeSearchable) && !Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), true);
        } else if (!Boolean.TRUE.equals(willBeSearchable) && Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), false);
        }
        
        recordSchemaChange(domainId, "FIELD", fieldId, "UPDATE", beforeState, toStateMap(savedField));
        return savedField;
    }
    
    @org.springframework.cache.annotation.Cacheable(value = "effectiveFields", key = "#nodeId", unless = "#result == null")
    @Transactional(readOnly = true)
    public List<FieldDefinition> getEffectiveFields(UUID nodeId) {
        if (nodeId == null) {
            return java.util.Collections.emptyList();
        }

        ClassificationNode node = nodeRepository.findById(nodeId).orElse(null);
        List<FieldDefinition> effectiveFields = new java.util.ArrayList<>();
        
        if (node != null) {
            List<FieldDefinition> domainFields = fieldRepository.findDomainFieldsWithSort(node.getDomain().getId());
            List<FieldDefinition> nodeFields = fieldRepository.findNodeFieldsWithSort(nodeId);
            
            List<UUID> pathIds = new java.util.ArrayList<>();
            ClassificationNode current = node.getParent();
            while (current != null) {
                pathIds.add(current.getId());
                current = current.getParent();
            }
                
            List<FieldDefinition> inheritedFields = fieldRepository.findByDefinedAtNode_IdIn(pathIds);
            
            effectiveFields.addAll(domainFields);
            effectiveFields.addAll(inheritedFields);
            effectiveFields.addAll(nodeFields);
        } else {
            Domain domain = domainRepository.findById(nodeId).orElse(null);
            if (domain != null) {
                effectiveFields.addAll(fieldRepository.findDomainFieldsWithSort(domain.getId()));
            }
        }

        effectiveFields.sort((f1, f2) -> {
            int s1 = f1.getFieldGroup() != null && f1.getFieldGroup().getSector() != null ? (f1.getFieldGroup().getSector().getSortOrder() != null ? f1.getFieldGroup().getSector().getSortOrder() : 9999) : 9999;
            int s2 = f2.getFieldGroup() != null && f2.getFieldGroup().getSector() != null ? (f2.getFieldGroup().getSector().getSortOrder() != null ? f2.getFieldGroup().getSector().getSortOrder() : 9999) : 9999;
            if (s1 != s2) return Integer.compare(s1, s2);
            
            int g1 = f1.getFieldGroup() != null ? (f1.getFieldGroup().getSortOrder() != null ? f1.getFieldGroup().getSortOrder() : 9999) : 9999;
            int g2 = f2.getFieldGroup() != null ? (f2.getFieldGroup().getSortOrder() != null ? f2.getFieldGroup().getSortOrder() : 9999) : 9999;
            if (g1 != g2) return Integer.compare(g1, g2);
            
            int o1 = f1.getOrder() != null ? f1.getOrder() : 9999;
            int o2 = f2.getOrder() != null ? f2.getOrder() : 9999;
            return Integer.compare(o1, o2);
        });

        return effectiveFields;
    }

    @Transactional(readOnly = true)
    public Page<FieldDefinition> getEffectiveFieldsPage(UUID nodeId, Pageable pageable) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        return fieldRepository.findEffectiveFieldsWithPagination(nodeId, node.getDomain().getId(), pageable);
    }
    
    @Transactional(readOnly = true)
    public List<FieldDefinition> getDomainFields(UUID domainId) {
        return fieldRepository.findDomainFieldsWithSort(domainId);
    }
    
    private String normalizeJsonStr(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        val = val.trim();
        if (val.startsWith("{") || val.startsWith("[")) {
            return val;
        }
        String[] parts = val.split("\\s*,\\s*");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parts.length; i++) {
            sb.append("\"").append(parts[i].replace("\"", "\\\"")).append("\"");
            if (i < parts.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
