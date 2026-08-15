package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.ApprovalStepRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.entity.enums.ApprovalTargetType;
import com.classification.domain_system.entity.enums.RecordStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.classification.domain_system.context.AuthContext;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.classification.domain_system.entity.FieldDefinition;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import com.classification.domain_system.exception.*;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {
    
    private final ApprovalRequestRepository approvalRepository;
    private final ApprovalStepRepository stepRepository;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final WorkflowResolver workflowResolver;
    private final ApprovalNotificationFacade notificationFacade;
        private final DataQualityService dqService;
    private final RecordHistoryRepository recordHistoryRepository;
    private final FieldDefinitionService fieldDefinitionService;
    private final MatchingService matchingService;
    private final UserRepository userRepository;
    @org.springframework.context.annotation.Lazy
    private final ApprovalDelegationService delegationService;
        private final com.classification.domain_system.repository.DomainRepository domainRepository;
    private final com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    private final CalculatedFieldEvaluator calculatedFieldEvaluator;
    private final com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;
    private final RecordHistoryWriter recordHistoryWriter;
    private final com.classification.domain_system.repository.RoleRepository roleRepository;
    private final DataMaskingService dataMaskingService;
    private final AuthContext authContext;
    private final ApprovalQueryService approvalQueryService;
    private final com.classification.domain_system.repository.BatchJobRepository batchJobRepository;
    private final com.classification.domain_system.repository.StagingRecordRepository stagingRecordRepository;

    public String resolveRoleDisplayName(String roleCode) {
        return approvalQueryService.resolveRoleDisplayName(roleCode);
    }


    private String recomputeCalculatedFields(UUID nodeId, String dataJson) {
        return calculatedFieldEvaluator.recomputeCalculatedFields(nodeId, dataJson);
    }

    private void logHistory(Record record, String changeType, String changedBy, String prevData, String newData, UUID approvalRequestId) {
        recordHistoryWriter.logHistory(record, changeType, changedBy, prevData, newData, approvalRequestId);
    }

    private void revertRecordStatusOnRejection(ApprovalRequest approval) {
        if (ApprovalTargetType.RECORD.name().equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
            record.setStatus(ApprovalStatus.REJECTED.name());
            recordRepository.saveAndFlush(record);
        } else if (ApprovalTargetType.RECORD_UPDATE.name().equals(approval.getTargetType()) || ApprovalTargetType.RECORD_DELETE.name().equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
            record.setStatus(RecordStatus.ACTIVE.name());
            recordRepository.saveAndFlush(record);
        } else if (ApprovalTargetType.BATCH_RECORD.name().equals(approval.getTargetType())) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode changesNode = mapper.readTree(approval.getChanges());
                List<UUID> recordIds = new ArrayList<>();
                if (changesNode.has("recordIds") && changesNode.get("recordIds").isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode id : changesNode.get("recordIds")) {
                        recordIds.add(UUID.fromString(id.asText()));
                    }
                }
                List<Record> records = recordRepository.findAllById(recordIds);
                for (Record record : records) {
                    record.setStatus(ApprovalStatus.REJECTED.name());
                }
                recordRepository.saveAllAndFlush(records);
                
                UUID batchId = UUID.fromString(changesNode.get("batchId").asText());
                batchJobRepository.findById(batchId).ifPresent(job -> {
                    job.setStatus("FAILED");
                    batchJobRepository.save(job);
                });
                
                List<com.classification.domain_system.entity.StagingRecord> stagings = stagingRecordRepository.findByBatchId(batchId);
                for (com.classification.domain_system.entity.StagingRecord sr : stagings) {
                    if ("PENDING_APPROVAL".equals(sr.getStatus())) {
                        sr.setStatus("ERROR");
                        sr.setErrorMessage("Approval rejected");
                    }
                }
                stagingRecordRepository.saveAll(stagings);
            } catch (Exception e) {
                log.error("Error applying rejection for BATCH_RECORD", e);
            }
        } else if (approval.getTargetType() != null && approval.getTargetType().startsWith("SCHEMA_")) {
            log.info("Schema change request {} was rejected, no record status to revert", approval.getId());
        }
    }


    /**
     * Workflow 관리 화면에서 저장한 targetId(username)와 요청자의 UUID를 매칭합니다.
     * targetId가 UUID 형식이면 직접 비교, username 형식이면 UserRepository를 통해 변환 후 비교합니다.
     */
    private boolean matchesUserIdentity(String targetId, String requesterId) {
        if (targetId == null || targetId.isBlank() || requesterId == null) return false;
        // 1. UUID 직접 비교
        if (targetId.equalsIgnoreCase(requesterId)) return true;
        // 2. targetId가 username인 경우 → DB에서 User 조회하여 UUID 비교
        try {
            java.util.Optional<User> user = userRepository.findByUsername(targetId);
            if (user.isPresent() && user.get().getId() != null) {
                return requesterId.equalsIgnoreCase(user.get().getId());
            }
        } catch (Exception e) {
            log.debug("Failed to resolve username '{}' to UUID", targetId, e);
        }
        return false;
    }

    public List<String> extractEditableFields(WorkflowConfig config, String requesterId, String userRole) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode perm : root.get("permissions")) {
                    String targetType = perm.has("targetType") ? perm.get("targetType").asText() : "";
                    String targetId = perm.has("targetId") ? perm.get("targetId").asText() : "";
                    String targetRole = perm.has("targetRole") ? perm.get("targetRole").asText() : "";

                    boolean matchesUser = "USER".equalsIgnoreCase(targetType) && matchesUserIdentity(targetId, requesterId);
                    boolean matchesRole = "ROLE".equalsIgnoreCase(targetType) && userRole != null &&
                            (targetRole.equalsIgnoreCase(userRole)
                             || ("ROLE_" + targetRole).equalsIgnoreCase(userRole)
                             || targetRole.equalsIgnoreCase("ROLE_" + userRole));
                    boolean matchesAll = "ALL".equalsIgnoreCase(targetType) || "EVERYONE".equalsIgnoreCase(targetType) || "*".equals(targetType) || targetType.isBlank();

                    if (matchesUser || matchesRole || matchesAll) {
                        if (perm.has("fieldPermissions") && perm.get("fieldPermissions").has("editableFields")) {
                            List<String> list = new java.util.ArrayList<>();
                            for (com.fasterxml.jackson.databind.JsonNode f : perm.get("fieldPermissions").get("editableFields")) {
                                list.add(f.asText());
                            }
                            return list;
                        }
                        if (perm.has("editableFields") && perm.get("editableFields").isArray()) {
                            List<String> list = new java.util.ArrayList<>();
                            for (com.fasterxml.jackson.databind.JsonNode f : perm.get("editableFields")) {
                                list.add(f.asText());
                            }
                            return list;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse field permissions", e);
        }
        return null;
    }

    public List<String> extractReadOnlyFields(WorkflowConfig config, String requesterId, String userRole) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode perm : root.get("permissions")) {
                    String targetType = perm.has("targetType") ? perm.get("targetType").asText() : "";
                    String targetId = perm.has("targetId") ? perm.get("targetId").asText() : "";
                    String targetRole = perm.has("targetRole") ? perm.get("targetRole").asText() : "";

                    boolean matchesUser = "USER".equalsIgnoreCase(targetType) && matchesUserIdentity(targetId, requesterId);
                    boolean matchesRole = "ROLE".equalsIgnoreCase(targetType) && userRole != null &&
                            (targetRole.equalsIgnoreCase(userRole)
                             || ("ROLE_" + targetRole).equalsIgnoreCase(userRole)
                             || targetRole.equalsIgnoreCase("ROLE_" + userRole));
                    boolean matchesAll = "ALL".equalsIgnoreCase(targetType) || "EVERYONE".equalsIgnoreCase(targetType) || "*".equals(targetType) || targetType.isBlank();

                    if (matchesUser || matchesRole || matchesAll) {
                        if (perm.has("readOnlyFields") && perm.get("readOnlyFields").isArray()) {
                            List<String> list = new java.util.ArrayList<>();
                            for (com.fasterxml.jackson.databind.JsonNode f : perm.get("readOnlyFields")) {
                                list.add(f.asText());
                            }
                            return list;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse read-only field permissions", e);
        }
        return null;
    }

    public List<String> extractHiddenFields(WorkflowConfig config, String requesterId, String userRole) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode perm : root.get("permissions")) {
                    String targetType = perm.has("targetType") ? perm.get("targetType").asText() : "";
                    String targetId = perm.has("targetId") ? perm.get("targetId").asText() : "";
                    String targetRole = perm.has("targetRole") ? perm.get("targetRole").asText() : "";

                    boolean matchesUser = "USER".equalsIgnoreCase(targetType) && matchesUserIdentity(targetId, requesterId);
                    boolean matchesRole = "ROLE".equalsIgnoreCase(targetType) && userRole != null &&
                            (targetRole.equalsIgnoreCase(userRole)
                             || ("ROLE_" + targetRole).equalsIgnoreCase(userRole)
                             || targetRole.equalsIgnoreCase("ROLE_" + userRole));
                    boolean matchesAll = "ALL".equalsIgnoreCase(targetType) || "EVERYONE".equalsIgnoreCase(targetType) || "*".equals(targetType) || targetType.isBlank();

                    if (matchesUser || matchesRole || matchesAll) {
                        if (perm.has("hiddenFields") && perm.get("hiddenFields").isArray()) {
                            List<String> list = new java.util.ArrayList<>();
                            for (com.fasterxml.jackson.databind.JsonNode f : perm.get("hiddenFields")) {
                                list.add(f.asText());
                            }
                            return list;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse hidden field permissions", e);
        }
        return null;
    }

    public com.fasterxml.jackson.databind.JsonNode extractRuleName(WorkflowConfig config, String requesterId, String userRole) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode perm : root.get("permissions")) {
                    String targetType = perm.has("targetType") ? perm.get("targetType").asText() : "";
                    String targetId = perm.has("targetId") ? perm.get("targetId").asText() : "";
                    String targetRole = perm.has("targetRole") ? perm.get("targetRole").asText() : "";

                    boolean matchesUser = "USER".equalsIgnoreCase(targetType) && matchesUserIdentity(targetId, requesterId);
                    boolean matchesRole = "ROLE".equalsIgnoreCase(targetType) && userRole != null && targetRole.equalsIgnoreCase(userRole);

                    if ((matchesUser || matchesRole) && perm.has("ruleName")) {
                        return perm.get("ruleName");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse ruleName from permissions", e);
        }
        return null;
    }

    public void validateUserActionPermission(WorkflowConfig config, String requesterId, String userRole, String requestedAction) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) {
            return;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode perm : root.get("permissions")) {
                    String targetType = perm.has("targetType") ? perm.get("targetType").asText() : "";
                    String targetId = perm.has("targetId") ? perm.get("targetId").asText() : "";
                    String targetRole = perm.has("targetRole") ? perm.get("targetRole").asText() : "";

                    boolean matchesUser = "USER".equalsIgnoreCase(targetType) && matchesUserIdentity(targetId, requesterId);
                    boolean matchesRole = "ROLE".equalsIgnoreCase(targetType) && userRole != null && targetRole.equalsIgnoreCase(userRole);

                    if (matchesUser || matchesRole) {
                        if (perm.has("allowedActions") && perm.get("allowedActions").isArray()) {
                            boolean allowed = false;
                            for (com.fasterxml.jackson.databind.JsonNode act : perm.get("allowedActions")) {
                                if (act.asText().equalsIgnoreCase(requestedAction)) {
                                    allowed = true;
                                    break;
                                }
                            }
                            if (!allowed) {
                                throw new BusinessException(ErrorCode.ACCESS_DENIED, 
                                    "사용자에게 " + requestedAction + " 행위 권한이 허용되지 않았습니다.");
                            }
                        }
                    }
                }
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Failed to validate action permission", e);
        }
    }

    @Transactional
    public ApprovalRequest requestRecordCreation(UUID nodeId, RecordRequest request) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Node not found"));
                
        // Check if Domain has the required mapping fields
        Domain domain = node.getDomain();
        if (domain == null || domain.getIdentifierFieldId() == null || domain.getDisplayNameFieldId() == null) {
            throw new BusinessException(ErrorCode.DOMAIN_MISSING_FIELD_MAPPING, "Domain is missing required field mappings (ID or Name). Please configure the domain settings first.");
        }
        
        WorkflowConfig workflowConfig = (request != null && request.getWorkflowConfigId() != null)
                ? workflowResolver.resolveWorkflowById(request.getWorkflowConfigId())
                : workflowResolver.resolveWorkflow(nodeId, "CREATE");
        validateUserActionPermission(workflowConfig, request.getRequesterId(), null, "CREATE");
        List<String> editableFields = extractEditableFields(workflowConfig, request.getRequesterId(), null);

        // 1. Data Quality Check
        DataQualityService.DQResult dq = dqService.validateData(nodeId, request.getData(), null, editableFields);
        if (dq != null && !dq.isValid) {
            throw new BusinessException(ErrorCode.DATA_QUALITY_CHECK_FAILED, "Data Quality Check Failed: " + String.join(", ", dq.errors));
        }

        // 1.5. Duplicate Check (Golden Record) -> UPSERT Behavior
        MatchingService.DuplicateResult dup = matchingService.checkDuplicates(nodeId, request.getData());
        if (dup.hasDuplicates) {
            if (dup.duplicateRecordIds != null && dup.duplicateRecordIds.size() == 1) {
                // Exactly one duplicate found -> Convert to UPDATE request
                return requestRecordUpdate(dup.duplicateRecordIds.get(0), request);
            } else {
                throw new BusinessException(ErrorCode.DEDUPLICATION_FAILED, "Deduplication Failed: " + dup.message);
            }
        }

        // 2. Create Record in PENDING_APPROVAL status with calculated fields
        String computedData = recomputeCalculatedFields(nodeId, request.getData());
        Record record = new Record();
        record.setNode(node);
        record.setStatus(RecordStatus.PENDING_APPROVAL.name());
        record.setData(computedData);
        record = recordRepository.save(record);
        
        // 3. Create ApprovalRequest
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType("RECORD");
        approval.setTargetId(record.getId());
        approval.setClassificationNode(record.getNode());
        approval.setRequesterId(request.getRequesterId());
        approval.setStatus(ApprovalStatus.PENDING.name());
        approval.setChanges(computedData);
        approval.setCurrentStepOrder(1);
        
        // 4. Create Steps based on dynamic request
        WorkflowConfig config = workflowResolver.resolveWorkflow(nodeId, "CREATE");
        workflowResolver.buildDynamicSteps(approval, config);
        
        // 5. Add Requester's DRAFT Step (stepOrder = 0)
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(request.getRequesterId());
        draftStep.setStepOrder(0);
        draftStep.setStatus(ApprovalStatus.SUBMITTED.name());
        draftStep.setComment(request.getComment());
        approval.getSteps().add(draftStep);
        
        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
    }
    
    @Transactional
    public ApprovalRequest requestBatchRecordCreation(
            UUID domainId, UUID batchId, List<UUID> recordIds, String requesterId) {
        
        ClassificationNode anyNode = nodeRepository.findFirstByDomain_IdAndIsDeletedFalse(domainId);
        WorkflowConfig config = workflowResolver.resolveWorkflow(
            anyNode != null ? anyNode.getId() : domainId, "CREATE");
        
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType("BATCH_RECORD");
        approval.setTargetId(batchId);
        if (anyNode != null) {
            approval.setClassificationNode(anyNode);
        }
        approval.setRequesterId(requesterId);
        approval.setStatus(ApprovalStatus.PENDING.name());
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            approval.setChanges(mapper.writeValueAsString(Map.of(
                "batchId", batchId,
                "recordIds", recordIds,
                "totalRecords", recordIds.size()
            )));
        } catch (Exception e) {
            approval.setChanges("{}");
        }
        
        approval.setCurrentStepOrder(1);
        
        workflowResolver.buildDynamicSteps(approval, config);
        
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(requesterId);
        draftStep.setStepOrder(0);
        draftStep.setStatus(ApprovalStatus.SUBMITTED.name());
        draftStep.setComment("Batch Import: " + recordIds.size() + " records");
        approval.getSteps().add(draftStep);
        
        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
    }

    @Transactional
    public ApprovalRequest requestRecordUpdate(UUID recordId, RecordRequest request) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
        
        if ("PENDING_APPROVAL".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.UPDATE_PENDING_CREATION, "Cannot update a record that is pending creation approval.");
        }
        
        List<ApprovalRequest> pendingUpdates = approvalRepository.findByTargetIdAndStatus(recordId, "PENDING");
        boolean hasPendingUpdate = pendingUpdates.stream().anyMatch(a -> ApprovalTargetType.RECORD_UPDATE.name().equals(a.getTargetType()));
        if (hasPendingUpdate) {
            throw new BusinessException(ErrorCode.UPDATE_PENDING_UPDATE, "This record is already under a pending update approval.");
        }
        
        UUID nodeId = record.getNode().getId();
        
        WorkflowConfig workflowConfig = workflowResolver.resolveWorkflow(nodeId, "UPDATE");
        validateUserActionPermission(workflowConfig, request.getRequesterId(), null, "UPDATE");
        List<String> editableFields = extractEditableFields(workflowConfig, request.getRequesterId(), null);

        // 1. Data Quality Check
        DataQualityService.DQResult dq = dqService.validateData(nodeId, request.getData(), recordId, editableFields);
        if (dq != null && !dq.isValid) {
            throw new BusinessException(ErrorCode.DATA_QUALITY_CHECK_FAILED, "Data Quality Check Failed: " + String.join(", ", dq.errors));
        }

        // 1.5. Duplicate Check
        MatchingService.DuplicateResult dup = matchingService.checkDuplicates(nodeId, request.getData());
        if (dup.hasDuplicates) {
            // Exclude self from duplicates
            dup.duplicateRecordIds.remove(recordId);
            if (!dup.duplicateRecordIds.isEmpty()) {
                throw new BusinessException(ErrorCode.DEDUPLICATION_FAILED, "Deduplication Failed: " + dup.message);
            }
        }
        
        // 2. Prepare changes JSON (before and after) with calculated fields recomputed
        String beforeJson = recomputeCalculatedFields(nodeId, record.getData());
        String afterJson = recomputeCalculatedFields(nodeId, request.getData());
        String changes = String.format("{\"before\": %s, \"after\": %s}", 
            beforeJson != null && !beforeJson.isBlank() ? beforeJson : "{}", 
            afterJson != null && !afterJson.isBlank() ? afterJson : "{}");
        
        // 3. Create ApprovalRequest
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType("RECORD_UPDATE");
        approval.setTargetId(record.getId());
        
        record.setStatus(RecordStatus.PENDING_APPROVAL.name());
        recordRepository.save(record);
        approval.setClassificationNode(record.getNode());
        approval.setRequesterId(request.getRequesterId());
        approval.setStatus(ApprovalStatus.PENDING.name());
        approval.setChanges(changes);
        approval.setCurrentStepOrder(1);
        
        // 4. Create Steps based on dynamic request
        WorkflowConfig config = workflowResolver.resolveWorkflow(record.getNode().getId(), "UPDATE");
        workflowResolver.buildDynamicSteps(approval, config);
        
        // 5. Add Requester's DRAFT Step (stepOrder = 0)
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(request.getRequesterId());
        draftStep.setStepOrder(0);
        draftStep.setStatus(ApprovalStatus.SUBMITTED.name());
        draftStep.setComment(request.getComment());
        approval.getSteps().add(draftStep);
        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
    }
    
    @Transactional
    public ApprovalRequest requestRecordDeletion(UUID recordId, RecordRequest request) {
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));

        List<UUID> referencingRecordIds = fieldDefinitionRepository.findByType("DOMAIN_REFERENCE").stream()
                .flatMap(field -> recordRepository.findReferencingRecords(
                        field.getKey(), recordId.toString(), recordId).stream())
                .map(Record::getId)
                .distinct()
                .toList();
        if (!referencingRecordIds.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.RECORD_REFERENCED_BY_OTHERS,
                    "Record is referenced by " + referencingRecordIds.size()
                            + " record(s): " + referencingRecordIds);
        }
        
        if ("PENDING_APPROVAL".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.DELETE_PENDING_CREATION, "Cannot delete a record that is pending creation approval.");
        }
        
        UUID nodeId = record.getNode().getId();
        
        // Create ApprovalRequest for DELETE
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType("RECORD_DELETE");
        approval.setTargetId(record.getId());
        approval.setClassificationNode(record.getNode());
        approval.setRequesterId(request.getRequesterId());
        approval.setStatus(ApprovalStatus.PENDING.name());
        approval.setChanges(record.getData());
        approval.setCurrentStepOrder(1);
        
        // Create Steps based on dynamic request
        WorkflowConfig config = workflowResolver.resolveWorkflow(record.getNode().getId(), "DELETE");
        workflowResolver.buildDynamicSteps(approval, config);
        
        // Add Requester's DRAFT Step (stepOrder = 0)
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(request.getRequesterId());
        draftStep.setStepOrder(0);
        draftStep.setStatus(ApprovalStatus.SUBMITTED.name());
        draftStep.setComment(request.getComment() != null ? request.getComment() : "Deletion requested");
        approval.getSteps().add(draftStep);
        
        // Update record status to PENDING_APPROVAL
        record.setStatus(RecordStatus.PENDING_APPROVAL.name());
        recordRepository.save(record);
        
        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
    }
        
    private boolean isStepAssigneeOrRoleMatch(ApprovalStep step, String approverId) {
        if (step == null || approverId == null) return false;

        // 1. Check if user has Admin Permission (Allows Admin Proxy Approval)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean hasAdminPerm = auth.getAuthorities().stream()
                    .anyMatch(a -> {
                        String authStr = a.getAuthority();
                        return "admin:write".equalsIgnoreCase(authStr) || "*".equals(authStr) || "*:*".equalsIgnoreCase(authStr);
                    });
            if (hasAdminPerm) {
                return true;
            }
        }

        // 2. Check explicit Assignee ID Match
        if (approverId.equals(step.getAssigneeId())) {
            return true;
        }

        // 3. Check Assignee Role Match
        if (step.getAssigneeRole() != null && !step.getAssigneeRole().isBlank()) {
            User user = userRepository.findById(approverId).orElse(null);
            if (user != null && step.getAssigneeRole().equalsIgnoreCase(user.getRole())) {
                return true;
            }
        }

        // 4. Check Active Delegation Match (Proxy Approver)
        if (step.getAssigneeId() != null && delegationService != null) {
            if (delegationService.isDelegatedApprover(approverId, step.getAssigneeId())) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public ApprovalRequest approveStep(UUID stepId, String approverId, String comment) {
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!isStepAssigneeOrRoleMatch(step, approverId)) {
            throw new CustomAccessDeniedException(ErrorCode.NOT_STEP_ASSIGNEE, "You are not the assignee for this step");
        }
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        boolean isProxy = step.getAssigneeId() != null && !approverId.equals(step.getAssigneeId()) && delegationService != null && delegationService.isDelegatedApprover(approverId, step.getAssigneeId());
        String finalComment = (isProxy ? "[대결] " : "") + (comment != null ? comment : "");

        step.setStatus(ApprovalStatus.APPROVED.name());
        step.setComment(finalComment);
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        if (notificationFacade != null) {
            try {
                String approverName = userRepository.findById(approverId).map(User::getUsername).orElse(approverId);
                notificationFacade.processStepApprovalNotifications(approval, null, approverName);
            } catch (Exception ignored) {}
        }
        notificationFacade.publishApprovalStepApproved(approval, step);
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest rejectStep(UUID stepId, String approverId, String comment) {
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!isStepAssigneeOrRoleMatch(step, approverId)) {
            throw new CustomAccessDeniedException(ErrorCode.NOT_STEP_ASSIGNEE, "You are not the assignee for this step");
        }
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }

        boolean isProxy = step.getAssigneeId() != null && !approverId.equals(step.getAssigneeId()) && delegationService != null && delegationService.isDelegatedApprover(approverId, step.getAssigneeId());
        String finalComment = (isProxy ? "[대결] " : "") + (comment != null ? comment : "");
        
        step.setStatus(ApprovalStatus.REJECTED.name());
        step.setComment(finalComment);
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        approval.setStatus(ApprovalStatus.REJECTED.name());
        approvalRepository.saveAndFlush(approval);
        
        if (notificationFacade != null) {
            try {
                String approverName = userRepository.findById(approverId).map(User::getUsername).orElse(approverId);
                notificationFacade.processStepRejectionNotifications(approval, null, approverName);
            } catch (Exception ignored) {}
        }
        
        revertRecordStatusOnRejection(approval);
        notificationFacade.sendRejectionNotification(approval, approverId, comment);
        broadcastRejectionEvent(approval);
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest adminApproveStep(UUID stepId, String adminId, String comment) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
                
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus(ApprovalStatus.APPROVED.name());
        step.setComment((comment != null && !comment.trim().isEmpty() ? comment + " " : "") + "(Admin Proxy)");
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        notificationFacade.publishApprovalStepApproved(approval, step);
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest adminRejectStep(UUID stepId, String adminId, String comment) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
                
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus(ApprovalStatus.REJECTED.name());
        step.setComment((comment != null && !comment.trim().isEmpty() ? comment + " " : "") + "(Admin Proxy)");
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        approval.setStatus(ApprovalStatus.REJECTED.name());
        approvalRepository.saveAndFlush(approval);
        
        revertRecordStatusOnRejection(approval);
        notificationFacade.sendRejectionNotification(approval, adminId, comment);
        broadcastRejectionEvent(approval);
        
        return approval;
    }
    
    private void broadcastRejectionEvent(ApprovalRequest approval) {
        if (webSocketPublisher != null) {
            java.util.Map<String, Object> payload = java.util.Map.of(
                    "eventType", ApprovalStatus.REJECTED.name(),
                    "approvalId", approval.getId(),
                    "status", ApprovalStatus.REJECTED.name(),
                    "targetType", approval.getTargetType() != null ? approval.getTargetType() : "",
                    "targetId", approval.getTargetId() != null ? approval.getTargetId() : ""
            );
            webSocketPublisher.publishApprovalEvent("/topic/approvals/" + approval.getId(), payload);
            webSocketPublisher.publishApprovalEvent("/topic/approvals/status-changes", payload);
        }
    }
    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getPendingRequests(Pageable pageable) {
        return approvalQueryService.getPendingRequests(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getAllRequests(String search, String status, String filterModel, Pageable pageable) {
        return approvalQueryService.getAllRequests(search, status, filterModel, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, Pageable pageable) {
        return approvalQueryService.getMyTodos(assigneeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, String userRole, Pageable pageable) {
        return approvalQueryService.getMyTodos(assigneeId, userRole, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, java.util.Collection<String> userRoles, Pageable pageable) {
        return approvalQueryService.getMyTodos(assigneeId, userRoles, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, String username, java.util.Collection<String> userRoles, Pageable pageable) {
        return approvalQueryService.getMyTodos(assigneeId, username, userRoles, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getMyRequests(String requesterId, Pageable pageable) {
        return approvalQueryService.getMyRequests(requesterId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getMyRequests(String requesterId, String username, Pageable pageable) {
        return approvalQueryService.getMyRequests(requesterId, username, pageable);
    }

    public ApprovalRequest maskChangesForRead(ApprovalRequest approval) {
        return approvalQueryService.maskChangesForRead(approval);
    }

    @Transactional(readOnly = true)
    public ApprovalRequest getRequestById(UUID id) {
        return approvalQueryService.getRequestById(id);
    }

    @Transactional
    public ApprovalRequest requestRecordMerge(RecordMergeService.MergeRequest request, String requesterId) {
        if (request == null || request.survivorRecordId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Survivor record ID is required.");
        }
        Record survivor = recordRepository.findById(request.survivorRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Survivor record not found: " + request.survivorRecordId));

        if (RecordStatus.MERGED.name().equalsIgnoreCase(survivor.getStatus()) || RecordStatus.REJECTED.name().equalsIgnoreCase(survivor.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Cannot merge into an inactive/merged record.");
        }

        if (request.mergedRecordIds != null) {
            for (UUID mergedId : request.mergedRecordIds) {
                if (mergedId.equals(survivor.getId())) continue;
                Record m = recordRepository.findById(mergedId)
                        .orElseThrow(() -> new ResourceNotFoundException("Merged record not found: " + mergedId));
                if (m.getNode() != null && survivor.getNode() != null && !m.getNode().getId().equals(survivor.getNode().getId())) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "Cannot merge records from a different node/domain.");
                }
            }
        }

        // P1. 미리 DQ 검증 수행
        Map<String, Object> finalDataMap = new java.util.HashMap<>();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            if (survivor.getData() != null) {
                Map<String, Object> sMap = mapper.readValue(survivor.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                finalDataMap.putAll(sMap);
            }
            if (request.fieldResolutions != null) {
                for (Map.Entry<String, UUID> entry : request.fieldResolutions.entrySet()) {
                    Record chosen = recordRepository.findById(entry.getValue()).orElse(null);
                    if (chosen != null && chosen.getData() != null) {
                        Map<String, Object> cMap = mapper.readValue(chosen.getData(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        if (cMap.containsKey(entry.getKey())) {
                            finalDataMap.put(entry.getKey(), cMap.get(entry.getKey()));
                        }
                    }
                }
            }
            String mergedJson = mapper.writeValueAsString(finalDataMap);
            if (survivor.getNode() != null) {
                DataQualityService.DQResult dqResult = dqService.validateData(survivor.getNode().getId(), mergedJson, survivor.getId(), null);
                if (dqResult != null && !dqResult.isValid) {
                    throw new BusinessException(ErrorCode.DATA_QUALITY_CHECK_FAILED, String.join(", ", dqResult.errors));
                }
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.error("Failed to validate DQ for record merge request", e);
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Error preparing merge data for approval.");
        }

        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType(ApprovalTargetType.RECORD_MERGE.name());
        approval.setTargetId(survivor.getId());
        approval.setRequesterId(requesterId);
        approval.setClassificationNode(survivor.getNode());
        approval.setStatus(ApprovalStatus.PENDING.name());

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            approval.setChanges(mapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Failed to serialize merge request.");
        }

        UUID nodeId = survivor.getNode() != null ? survivor.getNode().getId() : null;
        List<WorkflowConfig> configs = workflowResolver.resolveWorkflows(nodeId, "MERGE");
        WorkflowConfig config = configs != null && !configs.isEmpty() ? configs.get(0) : null;
        workflowResolver.buildDynamicSteps(approval, config);
        approval.setCurrentStepOrder(1);

        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
    }
}
