package com.classification.domain_system.service;

import com.classification.domain_system.dto.MemoApprovalRequest;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.*;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.entity.enums.ApprovalTargetType;
import com.classification.domain_system.entity.enums.RecordStatus;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalRequestCreationService {

    private final ApprovalRequestRepository approvalRepository;
    private final ApprovalStepRepository stepRepository;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final WorkflowResolver workflowResolver;
    private final ApprovalNotificationFacade notificationFacade;
    private final DataQualityService dqService;
    private final MatchingService matchingService;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final CalculatedFieldEvaluator calculatedFieldEvaluator;
    private final ApprovalFieldPermissionService permissionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String recomputeCalculatedFields(UUID nodeId, String dataJson) {
        return calculatedFieldEvaluator.recomputeCalculatedFields(nodeId, dataJson);
    }

    @Transactional
    public ApprovalRequest requestRecordCreation(UUID nodeId, RecordRequest request) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Node not found"));
                
        Domain domain = node.getDomain();
        if (domain == null || domain.getIdentifierFieldId() == null || domain.getDisplayNameFieldId() == null) {
            throw new BusinessException(ErrorCode.DOMAIN_MISSING_FIELD_MAPPING, "Domain is missing required field mappings (ID or Name). Please configure the domain settings first.");
        }
        
        WorkflowConfig workflowConfig = (request != null && request.getWorkflowConfigId() != null)
                ? workflowResolver.resolveWorkflowById(request.getWorkflowConfigId())
                : workflowResolver.resolveWorkflow(nodeId, "CREATE");
        permissionService.validateUserActionPermission(workflowConfig, request.getRequesterId(), null, "CREATE");
        List<String> editableFields = permissionService.extractEditableFields(workflowConfig, request.getRequesterId(), null);

        // 1. Data Quality Check
        DataQualityService.DQResult dq = dqService.validateData(nodeId, request.getData(), null, editableFields);
        if (dq != null && !dq.isValid) {
            throw new BusinessException(ErrorCode.DATA_QUALITY_CHECK_FAILED, "Data Quality Check Failed: " + String.join(", ", dq.errors));
        }

        // 1.5. Duplicate Check (Golden Record) -> UPSERT Behavior
        MatchingService.DuplicateResult dup = matchingService.checkDuplicates(nodeId, request.getData());
        if (dup.hasDuplicates) {
            if (dup.duplicateRecordIds != null && dup.duplicateRecordIds.size() == 1) {
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
        approval.addStep(draftStep);
        workflowResolver.enrichUserNames(approval);
        ApprovalRequest saved = approvalRepository.save(approval);
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            for (ApprovalStep s : approval.getSteps()) {
                s.setApprovalRequest(saved);
            }
            stepRepository.saveAll(approval.getSteps());
        }
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
            approval.setChanges(objectMapper.writeValueAsString(Map.of(
                "batchId", batchId,
                "recordIds", recordIds,
                "totalRecords", recordIds.size()
            )));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Failed to serialize batch record approval payload: " + e.getMessage());
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
        approval.addStep(draftStep);
        workflowResolver.enrichUserNames(approval);
        ApprovalRequest saved = approvalRepository.save(approval);
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            for (ApprovalStep s : approval.getSteps()) {
                s.setApprovalRequest(saved);
            }
            stepRepository.saveAll(approval.getSteps());
        }
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
        permissionService.validateUserActionPermission(workflowConfig, request.getRequesterId(), null, "UPDATE");
        List<String> editableFields = permissionService.extractEditableFields(workflowConfig, request.getRequesterId(), null);

        // 1. Data Quality Check
        DataQualityService.DQResult dq = dqService.validateData(nodeId, request.getData(), recordId, editableFields);
        if (dq != null && !dq.isValid) {
            throw new BusinessException(ErrorCode.DATA_QUALITY_CHECK_FAILED, "Data Quality Check Failed: " + String.join(", ", dq.errors));
        }

        // 1.5. Duplicate Check
        MatchingService.DuplicateResult dup = matchingService.checkDuplicates(nodeId, request.getData());
        if (dup.hasDuplicates) {
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
        approval.addStep(draftStep);
        workflowResolver.enrichUserNames(approval);
        ApprovalRequest saved = approvalRepository.save(approval);
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            for (ApprovalStep s : approval.getSteps()) {
                s.setApprovalRequest(saved);
            }
            stepRepository.saveAll(approval.getSteps());
        }
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
        
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType("RECORD_DELETE");
        approval.setTargetId(record.getId());
        approval.setClassificationNode(record.getNode());
        approval.setRequesterId(request.getRequesterId());
        approval.setStatus(ApprovalStatus.PENDING.name());
        approval.setChanges(record.getData());
        approval.setCurrentStepOrder(1);
        
        WorkflowConfig config = workflowResolver.resolveWorkflow(record.getNode().getId(), "DELETE");
        workflowResolver.buildDynamicSteps(approval, config);
        
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(request.getRequesterId());
        draftStep.setStepOrder(0);
        draftStep.setStatus(ApprovalStatus.SUBMITTED.name());
        draftStep.setComment(request.getComment() != null ? request.getComment() : "Deletion requested");
        approval.addStep(draftStep);
        
        record.setStatus(RecordStatus.PENDING_APPROVAL.name());
        recordRepository.save(record);
        workflowResolver.enrichUserNames(approval);
        ApprovalRequest saved = approvalRepository.save(approval);
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            for (ApprovalStep s : approval.getSteps()) {
                s.setApprovalRequest(saved);
            }
            stepRepository.saveAll(approval.getSteps());
        }
        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
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

        Map<String, Object> finalDataMap = new HashMap<>();
        try {
            if (survivor.getData() != null) {
                Map<String, Object> sMap = objectMapper.readValue(survivor.getData(), new TypeReference<Map<String, Object>>() {});
                finalDataMap.putAll(sMap);
            }
            if (request.fieldResolutions != null) {
                for (Map.Entry<String, UUID> entry : request.fieldResolutions.entrySet()) {
                    Record chosen = recordRepository.findById(entry.getValue()).orElse(null);
                    if (chosen != null && chosen.getData() != null) {
                        Map<String, Object> cMap = objectMapper.readValue(chosen.getData(), new TypeReference<Map<String, Object>>() {});
                        if (cMap.containsKey(entry.getKey())) {
                            finalDataMap.put(entry.getKey(), cMap.get(entry.getKey()));
                        }
                    }
                }
            }
            String mergedJson = objectMapper.writeValueAsString(finalDataMap);
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
            approval.setChanges(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Failed to serialize merge request.");
        }

        UUID nodeId = survivor.getNode() != null ? survivor.getNode().getId() : null;
        List<WorkflowConfig> configs = workflowResolver.resolveWorkflows(nodeId, "MERGE");
        WorkflowConfig config = configs != null && !configs.isEmpty() ? configs.get(0) : null;
        workflowResolver.buildDynamicSteps(approval, config);
        approval.setCurrentStepOrder(1);
        workflowResolver.enrichUserNames(approval);

        ApprovalRequest saved = approvalRepository.save(approval);
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            for (ApprovalStep s : approval.getSteps()) {
                s.setApprovalRequest(saved);
            }
            stepRepository.saveAll(approval.getSteps());
        }
        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
    }

    @Transactional
    public ApprovalRequest requestMemoApproval(MemoApprovalRequest request, String requesterId) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Title is required");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Content is required");
        }
        if (request.getSteps() == null || request.getSteps().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "At least one approval step is required");
        }

        UUID memoId = UUID.randomUUID();
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType(ApprovalTargetType.MEMO.name());
        approval.setTargetId(memoId);
        approval.setRequesterId(requesterId);
        approval.setStatus(ApprovalStatus.PENDING.name());

        try {
            Map<String, Object> changesMap = new HashMap<>();
            changesMap.put("title", request.getTitle());
            changesMap.put("content", request.getContent());
            changesMap.put("comment", request.getComment() != null ? request.getComment() : "");
            changesMap.put("attachments", request.getAttachments() != null ? request.getAttachments() : Collections.emptyList());
            approval.setChanges(objectMapper.writeValueAsString(changesMap));

            if (request.getObserverIds() != null && !request.getObserverIds().isEmpty()) {
                approval.setObserverIds(objectMapper.writeValueAsString(request.getObserverIds()));
            } else {
                approval.setObserverIds("[]");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                "Failed to serialize memo approval payload: " + e.getMessage());
        }

        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(requesterId);
        draftStep.setStepOrder(0);
        draftStep.setStatus(ApprovalStatus.SUBMITTED.name());
        draftStep.setComment(request.getComment() != null ? request.getComment() : "기안 상신");
        approval.addStep(draftStep);

        int minStepOrder = Integer.MAX_VALUE;
        for (MemoApprovalRequest.MemoStepItem item : request.getSteps()) {
            ApprovalStep step = new ApprovalStep();
            step.setApprovalRequest(approval);
            step.setStepType(item.getStepType() != null && !item.getStepType().isBlank() ? item.getStepType().toUpperCase() : "APPROVAL");
            step.setAssigneeId(item.getAssigneeId());
            step.setAssigneeRole(item.getAssigneeRole());
            int order = item.getStepOrder() != null && item.getStepOrder() > 0 ? item.getStepOrder() : 1;
            step.setStepOrder(order);
            if (order < minStepOrder) {
                minStepOrder = order;
            }
            approval.addStep(step);
        }

        final int initialCurrentOrder = minStepOrder != Integer.MAX_VALUE ? minStepOrder : 1;
        approval.setCurrentStepOrder(initialCurrentOrder);

        for (ApprovalStep s : approval.getSteps()) {
            if (s.getStepOrder() != null && s.getStepOrder() > 0) {
                if (s.getStepOrder().equals(initialCurrentOrder)) {
                    s.setStatus(ApprovalStatus.PENDING.name());
                } else {
                    s.setStatus(ApprovalStatus.WAITING.name());
                }
            }
        }
        workflowResolver.enrichUserNames(approval);

        ApprovalRequest saved = approvalRepository.save(approval);
        if (approval.getSteps() != null && !approval.getSteps().isEmpty()) {
            for (ApprovalStep s : approval.getSteps()) {
                s.setApprovalRequest(saved);
            }
            stepRepository.saveAll(approval.getSteps());
        }

        notificationFacade.publishApprovalRequestCreated(saved);
        return saved;
    }
}
