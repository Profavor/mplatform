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
import lombok.extern.slf4j.Slf4j;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.event.ApprovalRequestCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Set;
import java.util.HashSet;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;

import com.classification.domain_system.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.classification.domain_system.context.AuthContext authContext;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.classification.domain_system.repository.UserRepository userRepository;

    private String getCurrentUser() {
        if (authContext != null && authContext.getUserId() != null && !authContext.getUserId().isBlank()) {
            return authContext.getUserId();
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getName() != null) {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equalsIgnoreCase(name)) {
                return name;
            }
        }
        return null;
    }

    private String getCurrentUserId() {
        if (authContext != null && authContext.getUserId() != null && !authContext.getUserId().isBlank()) {
            return authContext.getUserId();
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null && SecurityContextHolder.getContext().getAuthentication().getName() != null) {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (name != null && !name.isBlank() && !"anonymousUser".equalsIgnoreCase(name)) {
                return name;
            }
        }
        String user = getCurrentUser();
        if (user != null && !user.isBlank()) {
            return user;
        }
        throw new com.classification.domain_system.exception.BusinessException(
            com.classification.domain_system.exception.ErrorCode.ACCESS_DENIED,
            "인증된 사용자 정보가 존재하지 않습니다."
        );
    }

    public boolean hasPendingSchemaApproval(UUID domainId, UUID nodeId) {
        if (approvalRequestRepository == null) return false;
        List<ApprovalRequest> list = new java.util.ArrayList<>();
        if (domainId != null) {
            list.addAll(approvalRequestRepository.findByTargetIdAndStatus(domainId, "PENDING"));
        }
        if (nodeId != null) {
            list.addAll(approvalRequestRepository.findByTargetIdAndStatus(nodeId, "PENDING"));
        }
        return list.stream().anyMatch(r -> r.getTargetType() != null && r.getTargetType().startsWith("SCHEMA_"));
    }

    public List<UUID> getPendingFieldIds(UUID domainId, UUID nodeId) {
        if (approvalRequestRepository == null) return java.util.Collections.emptyList();
        List<ApprovalRequest> list = new java.util.ArrayList<>();
        if (domainId != null) {
            list.addAll(approvalRequestRepository.findByTargetIdAndStatus(domainId, "PENDING"));
        }
        if (nodeId != null) {
            list.addAll(approvalRequestRepository.findByTargetIdAndStatus(nodeId, "PENDING"));
        }
        List<UUID> pendingFieldIds = new java.util.ArrayList<>();
        for (ApprovalRequest r : list) {
            if (r.getTargetType() != null && r.getTargetType().startsWith("SCHEMA_")) {
                if (r.getChanges() != null && !r.getChanges().isBlank()) {
                    try {
                        JsonNode root = objectMapper.readTree(r.getChanges());
                        if (root.has("fieldId")) {
                            pendingFieldIds.add(UUID.fromString(root.get("fieldId").asText()));
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        return pendingFieldIds;
    }

    private void validateNoPendingFieldApproval(UUID fieldId) {
        if (fieldId == null || approvalRequestRepository == null) return;
        List<ApprovalRequest> pendingRequests = approvalRequestRepository.findAll().stream()
                .filter(r -> "PENDING".equalsIgnoreCase(r.getStatus()) && r.getTargetType() != null && r.getTargetType().startsWith("SCHEMA_"))
                .filter(r -> {
                    if (r.getChanges() == null || r.getChanges().isBlank()) return false;
                    try {
                        JsonNode root = objectMapper.readTree(r.getChanges());
                        if (root.has("fieldId") && fieldId.toString().equalsIgnoreCase(root.get("fieldId").asText())) {
                            return true;
                        }
                    } catch (Exception ignored) {}
                    return false;
                })
                .toList();

        if (!pendingRequests.isEmpty()) {
            throw new com.classification.domain_system.exception.BusinessException(
                com.classification.domain_system.exception.ErrorCode.INVALID_INPUT,
                "해당 필드에 대해 이미 진행 중인 결재 요청이 존재합니다. 기존 결재 완료 후 다시 시도해주세요."
            );
        }
    }

    private ApprovalRequest createSchemaApprovalRequest(Domain domain, ClassificationNode node, String targetType, java.util.Map<String, Object> changesMap, String reason) {
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType(targetType);
        approval.setTargetId(node != null ? node.getId() : domain.getId());
        approval.setClassificationNode(node);
        approval.setRequesterId(getCurrentUserId());
        approval.setStatus("PENDING");
        if (domain != null && changesMap != null) {
            changesMap.put("domainId", domain.getId());
            if (domain.getName() != null && !domain.getName().isEmpty()) {
                String dName = domain.getName().containsKey("ko") ? domain.getName().get("ko") : domain.getName().values().iterator().next();
                changesMap.put("domainName", dName);
            }
        }
        try {
            approval.setChanges(objectMapper.writeValueAsString(changesMap));
        } catch (Exception e) {
            approval.setChanges("{}");
        }
        approval.setReason(reason);
        approval.setCurrentStepOrder(1);
        
        WorkflowConfig config = approvalService.resolveWorkflow(node != null ? node.getId() : (domain != null ? domain.getId() : null), "SCHEMA_CHANGE");
        if (config == null && domain != null) {
            List<WorkflowConfig> list = workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(domain.getId(), "SCHEMA_CHANGE");
            if (list.isEmpty()) {
                list = workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(domain.getId(), "UPDATE");
            }
            if (!list.isEmpty()) config = list.get(0);
        }
        
        if (config != null) {
            approvalService.buildDynamicSteps(approval, config);
        } else {
            // Default 1-step System Approval Workflow fallback
            ApprovalStep defaultStep = new ApprovalStep();
            defaultStep.setApprovalRequest(approval);
            defaultStep.setStepOrder(1);
            defaultStep.setStepType("APPROVAL");
            defaultStep.setAssigneeRole("SYSTEM_ADMIN");
            defaultStep.setStatus("PENDING");
            approval.getSteps().add(defaultStep);
        }
        
        // Add Requester's DRAFT Step (stepOrder = 0)
        String currentUserId = getCurrentUserId();
        String currentUsername = currentUserId;
        if (userRepository != null && currentUserId != null) {
            java.util.Optional<com.classification.domain_system.entity.User> uOpt = userRepository.findById(currentUserId);
            if (uOpt.isEmpty()) uOpt = userRepository.findByUsername(currentUserId);
            if (uOpt.isPresent()) currentUsername = uOpt.get().getUsername();
        }
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(currentUserId);
        draftStep.setAssigneeName(currentUsername);
        draftStep.setStepOrder(0);
        draftStep.setStatus("APPROVED");
        draftStep.setComment(reason);
        approval.getSteps().add(0, draftStep);
        
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
        if (request.getHint() != null) {
            field.setHint(request.getHint());
        }
        
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
        field.setMaskingPattern(request.getMaskingPattern() != null ? request.getMaskingPattern() : (isUpdate && field.getMaskingPattern() != null ? field.getMaskingPattern() : "GENERIC"));

        // Node Transfer Logic: Allow moving or specifying field to another Classification Node or Domain Level
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
    
    private boolean hasSchemaApproval(UUID domainId) {
        if (workflowConfigRepository == null || domainId == null) return false;
        try {
            return !workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(domainId, "SCHEMA_CHANGE").isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void recordSchemaChange(UUID domainId, String targetType, UUID targetId, String action, Object beforeData, Object afterData) {
        recordSchemaChange(domainId, targetType, targetId, action, beforeData, afterData, getCurrentUser());
    }

    private void recordSchemaChange(UUID domainId, String targetType, UUID targetId, String action, Object beforeData, Object afterData, String changedBy) {
        if (schemaHistoryService != null && domainId != null) {
            String operator = (changedBy != null && !changedBy.isBlank()) ? changedBy : getCurrentUser();
            schemaHistoryService.recordChange(domainId, targetType, targetId, action, beforeData, afterData, operator);
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
                String reason = request.getReason() != null ? request.getReason() : request.getComment();
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("nodeId", nodeId);
                changesMap.put("request", request);
                if (reason != null) changesMap.put("reason", reason);
                createSchemaApprovalRequest(domain, node, "SCHEMA_FIELD_ADD", changesMap, reason);
                FieldDefinition pendingField = new FieldDefinition();
                pendingField.setId(UUID.randomUUID());
                return pendingField;
            } catch (Exception e) {
                if (e instanceof com.classification.domain_system.exception.BusinessException) throw e;
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
                String reason = request.getReason() != null ? request.getReason() : request.getComment();
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("domainId", domainId);
                changesMap.put("request", request);
                if (reason != null) changesMap.put("reason", reason);
                createSchemaApprovalRequest(domain, null, "SCHEMA_FIELD_ADD", changesMap, reason);
                FieldDefinition pendingField = new FieldDefinition();
                pendingField.setId(UUID.randomUUID());
                return pendingField;
            } catch (Exception e) {
                if (e instanceof com.classification.domain_system.exception.BusinessException) throw e;
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
        map.put("hint", field.getHint());
        map.put("key", field.getKey());
        map.put("type", field.getType());
        map.put("required", field.getRequired());
        map.put("unit", field.getUnit());
        map.put("isSearchable", field.getIsSearchable());
        map.put("isMultiValue", field.getIsMultiValue());
        map.put("isEncrypted", field.getIsEncrypted());
        map.put("isReadOnly", field.getIsReadOnly());
        map.put("isHidden", field.getIsHidden());
        map.put("maskingPattern", field.getMaskingPattern() != null ? field.getMaskingPattern() : "GENERIC");
        map.put("isImmutable", field.getIsImmutable());
        map.put("approvalStatus", field.getApprovalStatus() != null ? field.getApprovalStatus() : "ACTIVE");
        map.put("isPendingApproval", Boolean.TRUE.equals(field.getIsPendingApproval()));
        if (field.getApprovalRequestId() != null) {
            map.put("approvalRequestId", field.getApprovalRequestId());
        }
        map.put("order", field.getOrder());
        if (field.getFieldGroup() != null) {
            map.put("fieldGroupId", field.getFieldGroup().getId());
            map.put("group", field.getFieldGroup().getName());
        }
        return map;
    }

    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public FieldDefinition updateField(UUID nodeId, UUID fieldId, FieldDefinitionRequest request, boolean bypassApproval) {
        FieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));

        if (nodeId != null && field.getDefinedAtNode() != null && !field.getDefinedAtNode().getId().equals(nodeId)) {
            throw new RuntimeException("Field does not belong to the specified node");
        }
        
        ClassificationNode node = field.getDefinedAtNode();
        Domain domain = node.getDomain();

        if (!bypassApproval && hasSchemaApproval(domain.getId())) {
            validateNoPendingFieldApproval(fieldId);
            try {
                String reason = request.getReason() != null ? request.getReason() : request.getComment();
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("nodeId", nodeId);
                changesMap.put("fieldId", fieldId);
                changesMap.put("before", toStateMap(field));
                changesMap.put("request", request);
                if (reason != null) changesMap.put("reason", reason);
                createSchemaApprovalRequest(domain, node, "SCHEMA_FIELD_UPDATE", changesMap, reason);
                return field;
            } catch (Exception e) {
                if (e instanceof com.classification.domain_system.exception.BusinessException) throw e;
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
                
        Domain domain = field.getDomain() != null ? field.getDomain() : (field.getDefinedAtNode() != null ? field.getDefinedAtNode().getDomain() : null);
        if (domain == null || !domain.getId().equals(domainId)) {
            throw new RuntimeException("Field does not belong to the specified domain");
        }
        
        if (!bypassApproval && hasSchemaApproval(domainId)) {
            validateNoPendingFieldApproval(fieldId);
            try {
                String reason = request.getReason() != null ? request.getReason() : request.getComment();
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("domainId", domainId);
                changesMap.put("fieldId", fieldId);
                changesMap.put("before", toStateMap(field));
                changesMap.put("request", request);
                if (reason != null) changesMap.put("reason", reason);
                createSchemaApprovalRequest(domainRepository.findById(domainId).orElse(null), null, "SCHEMA_FIELD_UPDATE", changesMap, reason);
                return field;
            } catch (Exception e) {
                log.error("Failed to create schema approval request details:", e);
                if (e instanceof com.classification.domain_system.exception.BusinessException) throw e;
                throw new RuntimeException("Failed to create schema approval request: " + (e.getMessage() != null ? e.getMessage() : e.toString()), e);
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
        
        recordSchemaChange(domainId, "FIELD", fieldId, "UPDATE", beforeState, toStateMap(savedField));
        return savedField;
    }

    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public void deleteDomainField(UUID domainId, UUID fieldId) {
        deleteDomainField(domainId, fieldId, false, null);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public void deleteDomainField(UUID domainId, UUID fieldId, boolean bypassApproval, String reason) {
        deleteDomainField(domainId, fieldId, bypassApproval, reason, null);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public void deleteDomainField(UUID domainId, UUID fieldId, boolean bypassApproval, String reason, String changedBy) {
        FieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found: " + fieldId));

        Domain domain = field.getDomain() != null ? field.getDomain() : (field.getDefinedAtNode() != null ? field.getDefinedAtNode().getDomain() : null);
        UUID actualDomainId = domain != null ? domain.getId() : domainId;

        if (!bypassApproval && hasSchemaApproval(actualDomainId)) {
            validateNoPendingFieldApproval(fieldId);
            try {
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("domainId", actualDomainId);
                changesMap.put("fieldId", fieldId);
                changesMap.put("fieldKey", field.getKey());
                changesMap.put("fieldName", field.getName());
                changesMap.put("before", toStateMap(field));
                if (reason != null) changesMap.put("reason", reason);
                createSchemaApprovalRequest(domain != null ? domain : domainRepository.findById(domainId).orElse(null), field.getDefinedAtNode(), "SCHEMA_FIELD_DELETE", changesMap, reason);
                return;
            } catch (Exception e) {
                log.error("Failed to create schema approval request for field deletion:", e);
                if (e instanceof com.classification.domain_system.exception.BusinessException) throw e;
                throw new RuntimeException("Failed to create schema approval request for field deletion", e);
            }
        }

        field.setIsRemoved(true);
        fieldRepository.saveAndFlush(field);
        recordSchemaChange(actualDomainId, "FIELD", fieldId, "DELETE", toStateMap(field), null, changedBy);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public void deleteField(UUID nodeId, UUID fieldId) {
        deleteDomainField(null, fieldId, false, null);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public void deleteDomainFieldDirect(UUID domainId, UUID fieldId) {
        deleteDomainFieldDirect(domainId, fieldId, null);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "effectiveFields", allEntries = true)
    @Transactional
    public void deleteDomainFieldDirect(UUID domainId, UUID fieldId, String changedBy) {
        deleteDomainField(domainId, fieldId, true, "Final approval passed", changedBy);
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

        UUID domainId = null;
        if (node != null && node.getDomain() != null) {
            domainId = node.getDomain().getId();
        } else {
            Domain domain = domainRepository.findById(nodeId).orElse(null);
            if (domain != null) {
                domainId = domain.getId();
            }
        }
        if (domainId != null) {
            populatePendingApprovalStatus(domainId, effectiveFields);
        }
        return effectiveFields;
    }

    @Transactional(readOnly = true)
    public Page<FieldDefinition> getEffectiveFieldsPage(UUID nodeId, Pageable pageable) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        List<FieldDefinition> allFields = getEffectiveFields(nodeId);
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int start = Math.min(page * size, allFields.size());
        int end = Math.min((page + 1) * size, allFields.size());
        List<FieldDefinition> content = allFields.subList(start, end);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, allFields.size());
    }
    
    @Transactional(readOnly = true)
    public List<FieldDefinition> getDomainFields(UUID domainId) {
        List<FieldDefinition> dbList = fieldRepository.findDomainFieldsWithSort(domainId);
        List<FieldDefinition> list = new java.util.ArrayList<>(dbList);
        populatePendingApprovalStatus(domainId, list);
        return list;
    }

    private void populatePendingApprovalStatus(UUID domainId, List<FieldDefinition> fields) {
        if (fields == null || approvalRequestRepository == null) return;
        List<ApprovalRequest> pendingRequests = approvalRequestRepository.findByStatus("PENDING");
        if (pendingRequests == null || pendingRequests.isEmpty()) return;

        Map<UUID, UUID> fieldIdToReqId = new HashMap<>();
        Map<String, UUID> keyToReqId = new HashMap<>();

        for (ApprovalRequest req : pendingRequests) {
            if (req.getChanges() != null && !req.getChanges().isBlank()) {
                try {
                    JsonNode json = objectMapper.readTree(req.getChanges());
                    
                    if (json.has("fieldId")) {
                        try {
                            UUID fId = UUID.fromString(json.get("fieldId").asText());
                            fieldIdToReqId.put(fId, req.getId());
                        } catch (Exception ignored) {}
                    }

                    String key = json.has("key") ? json.get("key").asText() : (json.has("fieldKey") ? json.get("fieldKey").asText() : null);
                    if (key != null) {
                        keyToReqId.put(key, req.getId());
                    }
                } catch (Exception ignored) {}
            }
        }

        for (FieldDefinition f : fields) {
            UUID reqId = null;
            if (f.getId() != null && fieldIdToReqId.containsKey(f.getId())) {
                reqId = fieldIdToReqId.get(f.getId());
            } else if (f.getKey() != null && keyToReqId.containsKey(f.getKey())) {
                reqId = keyToReqId.get(f.getKey());
            }
            if (reqId != null) {
                f.setApprovalStatus("PENDING_APPROVAL");
                f.setIsPendingApproval(true);
                f.setApprovalRequestId(reqId);
            }
        }
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
