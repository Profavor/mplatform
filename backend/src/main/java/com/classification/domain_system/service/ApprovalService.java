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
import com.classification.domain_system.repository.WorkflowConfigRepository;
import com.classification.domain_system.repository.RecordHistoryRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.entity.RecordHistory;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.dto.RecordRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.classification.domain_system.entity.FieldDefinition;

import org.springframework.context.ApplicationEventPublisher;
import com.classification.domain_system.event.ApprovalRequestCreatedEvent;
import com.classification.domain_system.event.ApprovalStepApprovedEvent;
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
    private final WorkflowConfigRepository workflowConfigRepository;
    private final DataQualityService dqService;
    private final RecordHistoryRepository recordHistoryRepository;
    private final FieldDefinitionService fieldDefinitionService;
    private final MatchingService matchingService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final com.classification.domain_system.repository.DomainRepository domainRepository;
    private final com.classification.domain_system.repository.FieldDefinitionRepository fieldDefinitionRepository;
    private final CalculatedFieldEvaluator calculatedFieldEvaluator;
    private final RecordHistoryWriter recordHistoryWriter;

    private static final java.util.Set<String> ALLOWED_FILTER_KEYS = java.util.Set.of(
            "domainName", "classificationName", "requesterId", "changes", "summary",
            "status", "targetType", "targetId", "id", "currentStepOrder", "createdAt", "updatedAt"
    );


    private String recomputeCalculatedFields(UUID nodeId, String dataJson) {
        return calculatedFieldEvaluator.recomputeCalculatedFields(nodeId, dataJson);
    }

    private void logHistory(Record record, String changeType, String changedBy, String prevData, String newData, UUID approvalRequestId) {
        recordHistoryWriter.logHistory(record, changeType, changedBy, prevData, newData, approvalRequestId);
    }

    private void revertRecordStatusOnRejection(ApprovalRequest approval) {
        if ("RECORD".equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
            record.setStatus("REJECTED");
            recordRepository.saveAndFlush(record);
        } else if ("RECORD_UPDATE".equals(approval.getTargetType()) || "RECORD_DELETE".equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
            record.setStatus("ACTIVE");
            recordRepository.saveAndFlush(record);
        } else if (approval.getTargetType() != null && approval.getTargetType().startsWith("SCHEMA_")) {
            log.info("Schema change request {} was rejected, no record status to revert", approval.getId());
        }
    }

    private boolean isEffectiveConfig(WorkflowConfig config) {
        return config != null && config.getStepsConfig() != null 
            && !config.getStepsConfig().isEmpty() 
            && !config.getStepsConfig().equals("{\"steps\":[],\"observerIds\":[]}");
    }

    public List<WorkflowConfig> resolveWorkflows(UUID nodeId, String actionType) {
        return resolveWorkflowsForUser(nodeId, actionType, null, null);
    }

    public List<WorkflowConfig> resolveWorkflowsForUser(UUID nodeId, String actionType, String requesterId, String userRole) {
        if (nodeId == null) return java.util.Collections.emptyList();

        List<WorkflowConfig> rawList = new java.util.ArrayList<>();
        ClassificationNode current = nodeRepository.findById(nodeId).orElse(null);
        Domain targetDomain = null;

        if (current != null) {
            targetDomain = current.getDomain();
            while (current != null) {
                List<WorkflowConfig> confs = workflowConfigRepository.findByNodeIdAndActionType(
                    current.getId(), actionType
                );
                List<WorkflowConfig> effective = confs.stream()
                        .filter(c -> !Boolean.FALSE.equals(c.getIsActive()))
                        .filter(this::isEffectiveConfig)
                        .toList();
                if (!effective.isEmpty()) {
                    rawList = effective;
                    break;
                }
                current = current.getParent();
            }
        } else if (domainRepository != null) {
            targetDomain = domainRepository.findById(nodeId).orElse(null);
        }

        // Fallback to domain level
        if (rawList.isEmpty() && targetDomain != null) {
            List<WorkflowConfig> domainConfs = workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(
                targetDomain.getId(), actionType
            );
            rawList = domainConfs.stream()
                    .filter(c -> !Boolean.FALSE.equals(c.getIsActive()))
                    .filter(this::isEffectiveConfig)
                    .toList();
        }

        if (requesterId == null && (userRole == null || userRole.isBlank())) {
            return rawList;
        }

        // Filter workflows that allow user to perform this action
        return rawList.stream()
                .filter(config -> allowsUserAction(config, actionType, requesterId, userRole))
                .toList();
    }

    private boolean allowsUserAction(WorkflowConfig config, String actionType, String requesterId, String userRole) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) return true;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(config.getStepsConfig());
            if (!root.has("permissions") || !root.get("permissions").isArray() || root.get("permissions").isEmpty()) {
                return true;
            }
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
                    if (perm.has("allowedActions") && perm.get("allowedActions").isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode act : perm.get("allowedActions")) {
                            if (act.asText().equalsIgnoreCase(actionType) || act.asText().equals("*") || "ALL".equalsIgnoreCase(act.asText())) {
                                return true;
                            }
                        }
                    } else {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            switch (actionType) {
                case "RECORD": return true;
                case "RECORD_UPDATE": return true;
                case "RECORD_DELETE": return true;
                case "RECORD_MERGE": return true;
            }
            return true;
        }
    }

    public WorkflowConfig resolveWorkflow(UUID nodeId, String actionType) {
        List<WorkflowConfig> list = resolveWorkflows(nodeId, actionType);
        if (list.isEmpty()) return null;
        // Prefer default workflow if set, otherwise first
        return list.stream().filter(c -> Boolean.TRUE.equals(c.getIsDefault())).findFirst().orElse(list.get(0));
    }

    public WorkflowConfig resolveWorkflowById(UUID workflowId) {
        if (workflowId == null) return null;
        return workflowConfigRepository.findById(workflowId).orElse(null);
    }

    private void buildDynamicSteps(ApprovalRequest approval, WorkflowConfig config) {
        List<ApprovalStep> steps = new java.util.ArrayList<>();
        try {
            if (config != null && config.getStepsConfig() != null && !config.getStepsConfig().isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(config.getStepsConfig());
                
                if (root.has("approvalLine") && root.get("approvalLine").isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode stepNode : root.get("approvalLine")) {
                        ApprovalStep step = new ApprovalStep();
                        step.setApprovalRequest(approval);
                        step.setStepType(stepNode.has("stepType") ? stepNode.get("stepType").asText() : "APPROVAL");
                        step.setStepOrder(stepNode.get("stepOrder").asInt());
                        step.setStatus(step.getStepOrder() == 1 ? "PENDING" : "WAITING");
                        
                        if (stepNode.has("assigneeId") && !stepNode.get("assigneeId").asText().isBlank()) {
                            step.setAssigneeId(stepNode.get("assigneeId").asText());
                        }
                        if (stepNode.has("assigneeRole") && !stepNode.get("assigneeRole").asText().isBlank()) {
                            step.setAssigneeRole(stepNode.get("assigneeRole").asText());
                        }
                        steps.add(step);
                    }
                } else if (root.has("steps") && root.get("steps").isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode stepNode : root.get("steps")) {
                        ApprovalStep step = new ApprovalStep();
                        step.setApprovalRequest(approval);
                        step.setStepType(stepNode.has("stepType") ? stepNode.get("stepType").asText() : "APPROVAL");
                        if (stepNode.has("assigneeId") && !stepNode.get("assigneeId").asText().isBlank()) {
                            step.setAssigneeId(stepNode.get("assigneeId").asText());
                        }
                        step.setStepOrder(stepNode.get("stepOrder").asInt());
                        step.setStatus(step.getStepOrder() == 1 ? "PENDING" : "WAITING");
                        steps.add(step);
                    }
                }
                
                if (root.has("observerIds") && root.get("observerIds").isArray()) {
                    approval.setObserverIds(mapper.writeValueAsString(root.get("observerIds")));
                } else {
                    approval.setObserverIds("[]");
                }
            } else {
                approval.setObserverIds("[]");
            }
        } catch (Exception e) {
            approval.setObserverIds("[]");
            log.error("Failed to parse observerIds", e);
        }
        approval.getSteps().addAll(steps);
    }

    public void enrichUserNames(ApprovalRequest request) {
        if (request == null) return;
        if (request.getRequesterId() != null) {
            userRepository.findById(request.getRequesterId())
                .ifPresentOrElse(
                    u -> request.setRequesterName(u.getUsername()),
                    () -> request.setRequesterName(request.getRequesterId())
                );
        }
        if (request.getSteps() != null) {
            for (ApprovalStep step : request.getSteps()) {
                if (step.getAssigneeId() != null) {
                    userRepository.findById(step.getAssigneeId())
                        .ifPresentOrElse(
                            u -> step.setAssigneeName(u.getUsername()),
                            () -> step.setAssigneeName(step.getAssigneeId())
                        );
                }
            }
        }
        if (request.getObserverIds() != null && !request.getObserverIds().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<String> obsIds = mapper.readValue(request.getObserverIds(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                if (obsIds != null) {
                    List<String> names = obsIds.stream().map(id -> 
                        userRepository.findById(id).map(com.classification.domain_system.entity.User::getUsername).orElse(id)
                    ).collect(Collectors.toList());
                    request.setObserverNames(names);
                }
            } catch (Exception e) {
                log.warn("Failed to parse observerIds for enrichment", e);
            }
        }
    }

    public void enrichUserNames(List<ApprovalRequest> requests) {
        if (requests == null) return;
        requests.forEach(this::enrichUserNames);
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
                ? resolveWorkflowById(request.getWorkflowConfigId())
                : resolveWorkflow(nodeId, "CREATE");
        validateUserActionPermission(workflowConfig, request.getRequesterId(), null, "CREATE");
        List<String> editableFields = extractEditableFields(workflowConfig, request.getRequesterId(), null);

        // 1. Data Quality Check
        DataQualityService.DQResult dq = dqService.validateData(nodeId, request.getData(), null, editableFields);
        if (!dq.isValid) {
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
        record.setStatus("PENDING_APPROVAL");
        record.setData(computedData);
        record = recordRepository.save(record);
        
        // 3. Create ApprovalRequest
        ApprovalRequest approval = new ApprovalRequest();
        approval.setTargetType("RECORD");
        approval.setTargetId(record.getId());
        approval.setClassificationNode(record.getNode());
        approval.setRequesterId(request.getRequesterId());
        approval.setStatus("PENDING");
        approval.setChanges(computedData);
        approval.setCurrentStepOrder(1);
        
        // 4. Create Steps based on dynamic request
        WorkflowConfig config = resolveWorkflow(nodeId, "CREATE");
        buildDynamicSteps(approval, config);
        
        // 5. Add Requester's DRAFT Step (stepOrder = 0)
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(request.getRequesterId());
        draftStep.setStepOrder(0);
        draftStep.setStatus("SUBMITTED");
        draftStep.setComment(request.getComment());
        approval.getSteps().add(draftStep);
        
        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        eventPublisher.publishEvent(new ApprovalRequestCreatedEvent(saved));
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
        boolean hasPendingUpdate = pendingUpdates.stream().anyMatch(a -> "RECORD_UPDATE".equals(a.getTargetType()));
        if (hasPendingUpdate) {
            throw new BusinessException(ErrorCode.UPDATE_PENDING_UPDATE, "This record is already under a pending update approval.");
        }
        
        UUID nodeId = record.getNode().getId();
        
        WorkflowConfig workflowConfig = resolveWorkflow(nodeId, "UPDATE");
        validateUserActionPermission(workflowConfig, request.getRequesterId(), null, "UPDATE");
        List<String> editableFields = extractEditableFields(workflowConfig, request.getRequesterId(), null);

        // 1. Data Quality Check
        DataQualityService.DQResult dq = dqService.validateData(nodeId, request.getData(), recordId, editableFields);
        if (!dq.isValid) {
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
        
        record.setStatus("PENDING_APPROVAL");
        recordRepository.save(record);
        approval.setClassificationNode(record.getNode());
        approval.setRequesterId(request.getRequesterId());
        approval.setStatus("PENDING");
        approval.setChanges(changes);
        approval.setCurrentStepOrder(1);
        
        // 4. Create Steps based on dynamic request
        WorkflowConfig config = resolveWorkflow(record.getNode().getId(), "UPDATE");
        buildDynamicSteps(approval, config);
        
        // 5. Add Requester's DRAFT Step (stepOrder = 0)
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(request.getRequesterId());
        draftStep.setStepOrder(0);
        draftStep.setStatus("SUBMITTED");
        draftStep.setComment(request.getComment());
        approval.getSteps().add(draftStep);
        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        eventPublisher.publishEvent(new ApprovalRequestCreatedEvent(saved));
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
        approval.setStatus("PENDING");
        approval.setChanges(record.getData());
        approval.setCurrentStepOrder(1);
        
        // Create Steps based on dynamic request
        WorkflowConfig config = resolveWorkflow(record.getNode().getId(), "DELETE");
        buildDynamicSteps(approval, config);
        
        // Add Requester's DRAFT Step (stepOrder = 0)
        ApprovalStep draftStep = new ApprovalStep();
        draftStep.setApprovalRequest(approval);
        draftStep.setStepType("DRAFT");
        draftStep.setAssigneeId(request.getRequesterId());
        draftStep.setStepOrder(0);
        draftStep.setStatus("SUBMITTED");
        draftStep.setComment(request.getComment() != null ? request.getComment() : "Deletion requested");
        approval.getSteps().add(draftStep);
        
        // Update record status to PENDING_APPROVAL
        record.setStatus("PENDING_APPROVAL");
        recordRepository.save(record);
        
        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        eventPublisher.publishEvent(new ApprovalRequestCreatedEvent(saved));
        return saved;
    }
        private boolean isStepAssigneeOrRoleMatch(ApprovalStep step, String approverId) {
        if (step == null || approverId == null) return false;
        if (approverId.equals(step.getAssigneeId())) {
            return true;
        }
        if (step.getAssigneeRole() != null && !step.getAssigneeRole().isBlank()) {
            User user = userRepository.findById(approverId).orElse(null);
            if (user != null && step.getAssigneeRole().equalsIgnoreCase(user.getRole())) {
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
        if (!"PENDING".equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus("APPROVED");
        step.setComment(comment);
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        if (notificationService != null) {
            try {
                String approverName = userRepository.findById(approverId).map(User::getUsername).orElse(approverId);
                notificationService.updateApprovalNotificationsToProcessed(approval.getId(), approverName, "APPROVED");
            } catch (Exception ignored) {}
        }
        eventPublisher.publishEvent(new ApprovalStepApprovedEvent(approval, step));
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest rejectStep(UUID stepId, String approverId, String comment) {
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!isStepAssigneeOrRoleMatch(step, approverId)) {
            throw new CustomAccessDeniedException(ErrorCode.NOT_STEP_ASSIGNEE, "You are not the assignee for this step");
        }
        if (!"PENDING".equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus("REJECTED");
        step.setComment(comment);
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        approval.setStatus("REJECTED");
        approvalRepository.saveAndFlush(approval);
        
        if (notificationService != null) {
            try {
                String approverName = userRepository.findById(approverId).map(User::getUsername).orElse(approverId);
                notificationService.updateApprovalNotificationsToProcessed(approval.getId(), approverName, "REJECTED");
            } catch (Exception ignored) {}
        }
        
        revertRecordStatusOnRejection(approval);
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest adminApproveStep(UUID stepId, String adminId, String comment) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
                
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!"PENDING".equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus("APPROVED");
        step.setComment((comment != null && !comment.isBlank() ? comment + " " : "") + "(Admin Proxy)");
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        eventPublisher.publishEvent(new ApprovalStepApprovedEvent(approval, step));
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest adminRejectStep(UUID stepId, String adminId, String comment) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
                
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!"PENDING".equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus("REJECTED");
        step.setComment((comment != null && !comment.isBlank() ? comment + " " : "") + "(Admin Proxy)");
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        approval.setStatus("REJECTED");
        approvalRepository.saveAndFlush(approval);
        
        revertRecordStatusOnRejection(approval);
        
        return approval;
    }
    
    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getPendingRequests(Pageable pageable) {
        return approvalRepository.findByStatusOrderByCreatedAtDesc("PENDING", pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getAllRequests(String search, String status, String filterModel, Pageable pageable) {
        final org.springframework.data.domain.Sort sort = pageable.getSort();
        Pageable unSortedPageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        
        org.springframework.data.jpa.domain.Specification<ApprovalRequest> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            if (status != null && !status.trim().isEmpty()) {
                String[] statuses = status.split(",");
                jakarta.persistence.criteria.CriteriaBuilder.In<String> inClause = cb.in(root.get("status"));
                for (String s : statuses) {
                    inClause.value(s.trim());
                }
                predicates.add(inClause);
            }
            
            if (filterModel != null && !filterModel.trim().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(filterModel);
                    java.util.Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = rootNode.fields();
                    
                    while (fields.hasNext()) {
                        Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> fieldEntry = fields.next();
                        String fieldKey = fieldEntry.getKey();
                        com.fasterxml.jackson.databind.JsonNode filterInfo = fieldEntry.getValue();
                        
                        if (!ALLOWED_FILTER_KEYS.contains(fieldKey)) {
                            throw new BusinessException(ErrorCode.INVALID_INPUT, "Filter key not allowed: " + fieldKey);
                        }
                        
                        if (filterInfo.has("filterType")) {
                            String filterType = filterInfo.get("filterType").asText();
                            
                             if ("set".equals(filterType) && filterInfo.has("values")) {
                                List<String> vals = new ArrayList<>();
                                for (com.fasterxml.jackson.databind.JsonNode vNode : filterInfo.get("values")) {
                                    vals.add(vNode.asText());
                                }
                                if (!vals.isEmpty()) {
                                    if ("domainName".equals(fieldKey) || "classificationName".equals(fieldKey)) {
                                        jakarta.persistence.criteria.Join<Object, Object> nodeJoin = root.join("classificationNode", jakarta.persistence.criteria.JoinType.LEFT);
                                        if ("classificationName".equals(fieldKey)) {
                                            List<jakarta.persistence.criteria.Predicate> orPreds = new ArrayList<>();
                                            for (String v : vals) {
                                                orPreds.add(cb.like(cb.lower(nodeJoin.get("name").as(String.class)), "%" + v.toLowerCase() + "%"));
                                            }
                                            predicates.add(cb.or(orPreds.toArray(new jakarta.persistence.criteria.Predicate[0])));
                                        } else {
                                            jakarta.persistence.criteria.Join<Object, Object> domainJoin = nodeJoin.join("domain", jakarta.persistence.criteria.JoinType.LEFT);
                                            List<jakarta.persistence.criteria.Predicate> orPreds = new ArrayList<>();
                                            for (String v : vals) {
                                                orPreds.add(cb.like(cb.lower(domainJoin.get("name").as(String.class)), "%" + v.toLowerCase() + "%"));
                                            }
                                            predicates.add(cb.or(orPreds.toArray(new jakarta.persistence.criteria.Predicate[0])));
                                        }
                                    } else {
                                        jakarta.persistence.criteria.CriteriaBuilder.In<String> inClause = cb.in(root.get(fieldKey));
                                        for (String v : vals) {
                                            inClause.value(v);
                                        }
                                        predicates.add(inClause);
                                    }
                                }
                            }
                            else if ("text".equals(filterType) && filterInfo.has("filter")) {
                                String textValue = filterInfo.get("filter").asText().trim().toLowerCase();
                                String type = filterInfo.has("type") ? filterInfo.get("type").asText() : "contains";
                                String likePattern = "%" + textValue + "%";
                                if ("equals".equals(type)) likePattern = textValue;
                                else if ("startsWith".equals(type)) likePattern = textValue + "%";
                                else if ("endsWith".equals(type)) likePattern = "%" + textValue;
                                
                                if ("domainName".equals(fieldKey) || "classificationName".equals(fieldKey)) {
                                    jakarta.persistence.criteria.Join<Object, Object> nodeJoin = root.join("classificationNode", jakarta.persistence.criteria.JoinType.LEFT);
                                    if ("classificationName".equals(fieldKey)) {
                                        predicates.add(cb.like(cb.lower(nodeJoin.get("name").as(String.class)), likePattern));
                                    } else {
                                        jakarta.persistence.criteria.Join<Object, Object> domainJoin = nodeJoin.join("domain", jakarta.persistence.criteria.JoinType.LEFT);
                                        predicates.add(cb.like(cb.lower(domainJoin.get("name").as(String.class)), likePattern));
                                    }
                                } else if ("requesterId".equals(fieldKey)) {
                                    predicates.add(cb.like(cb.lower(root.get("requesterId").as(String.class)), likePattern));
                                } else if ("changes".equals(fieldKey) || "summary".equals(fieldKey)) {
                                    jakarta.persistence.criteria.Expression<String> changesText = cb.function("concat", String.class, root.get("changes"), cb.literal(""));
                                    predicates.add(cb.like(cb.lower(changesText), likePattern));
                                } else {
                                    predicates.add(cb.like(cb.lower(root.get(fieldKey).as(String.class)), likePattern));
                                }
                            }
                            else if ("date".equals(filterType) && filterInfo.has("dateFrom")) {
                                String type = filterInfo.has("type") ? filterInfo.get("type").asText() : "equals";
                                String dateFromStr = filterInfo.get("dateFrom").asText();
                                String dateToStr = filterInfo.has("dateTo") && !filterInfo.get("dateTo").isNull() ? filterInfo.get("dateTo").asText() : null;
                                
                                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                if (dateFromStr.length() == 10) dateFromStr += " 00:00:00";
                                if (dateFromStr.contains("T")) dateFromStr = dateFromStr.replace("T", " ");
                                if (dateFromStr.length() > 19) dateFromStr = dateFromStr.substring(0, 19);
                                java.time.LocalDateTime dateFrom = java.time.LocalDateTime.parse(dateFromStr, dtf);
                                
                                java.time.LocalDateTime dateTo = null;
                                if (dateToStr != null) {
                                    if (dateToStr.length() == 10) dateToStr += " 23:59:59";
                                    if (dateToStr.contains("T")) dateToStr = dateToStr.replace("T", " ");
                                    if (dateToStr.length() > 19) dateToStr = dateToStr.substring(0, 19);
                                    dateTo = java.time.LocalDateTime.parse(dateToStr, dtf);
                                }
                                
                                if ("equals".equals(type)) {
                                    predicates.add(cb.between(root.get(fieldKey), dateFrom, dateFrom.plusDays(1).minusSeconds(1)));
                                } else if ("greaterThan".equals(type)) {
                                    predicates.add(cb.greaterThan(root.get(fieldKey), dateFrom));
                                } else if ("lessThan".equals(type)) {
                                    predicates.add(cb.lessThan(root.get(fieldKey), dateFrom));
                                } else if ("inRange".equals(type) && dateTo != null) {
                                    predicates.add(cb.between(root.get(fieldKey), dateFrom, dateTo));
                                }
                            }
                        }
                    }
                } catch (BusinessException be) {
                    throw be;
                } catch (Exception e) {
                    log.error("Failed to process dynamic search predicate", e);
                }
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                jakarta.persistence.criteria.Join<Object, Object> nodeJoin = root.join("classificationNode", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("targetType")), likePattern),
                    cb.like(cb.lower(root.get("status")), likePattern),
                    cb.like(cb.lower(cb.function("concat", String.class, root.get("changes"), cb.literal(""))), likePattern),
                    cb.like(cb.lower(nodeJoin.get("name").as(String.class)), likePattern)
                ));
            }
            
            if (sort != null && sort.isSorted()) {
                Map<UUID, String> idKeyMap = new java.util.HashMap<>();
                Map<UUID, String> nameKeyMap = new java.util.HashMap<>();
                try {
                    List<com.classification.domain_system.entity.Domain> domains = domainRepository.findAll();
                    for (com.classification.domain_system.entity.Domain d : domains) {
                        if (d.getIdentifierFieldId() != null) {
                            com.classification.domain_system.entity.FieldDefinition fd = fieldDefinitionRepository.findById(d.getIdentifierFieldId()).orElse(null);
                            if (fd != null) idKeyMap.put(d.getId(), fd.getKey());
                        }
                        if (d.getDisplayNameFieldId() != null) {
                            com.classification.domain_system.entity.FieldDefinition fd = fieldDefinitionRepository.findById(d.getDisplayNameFieldId()).orElse(null);
                            if (fd != null) nameKeyMap.put(d.getId(), fd.getKey());
                        }
                    }
                } catch (Exception e) {}

                List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
                for (org.springframework.data.domain.Sort.Order o : sort) {
                    String prop = o.getProperty();
                    boolean isAsc = o.isAscending();
                    
                    jakarta.persistence.criteria.Expression<?> expression;
                    if ("classificationNode.name".equals(prop)) {
                        jakarta.persistence.criteria.Join<Object, Object> nodeJoin = root.join("classificationNode", jakarta.persistence.criteria.JoinType.LEFT);
                        expression = nodeJoin.get("name");
                    } else if ("classificationNode.domain.name".equals(prop)) {
                        jakarta.persistence.criteria.Join<Object, Object> nodeJoin = root.join("classificationNode", jakarta.persistence.criteria.JoinType.LEFT);
                        jakarta.persistence.criteria.Join<Object, Object> domainJoin = nodeJoin.join("domain", jakarta.persistence.criteria.JoinType.LEFT);
                        expression = domainJoin.get("name");
                    } else if ("idAttribute".equals(prop) || "nameAttribute".equals(prop)) {
                        try {
                            jakarta.persistence.criteria.Join<Object, Object> nodeJoin = root.join("classificationNode", jakarta.persistence.criteria.JoinType.LEFT);
                            jakarta.persistence.criteria.Join<Object, Object> domainJoin = nodeJoin.join("domain", jakarta.persistence.criteria.JoinType.LEFT);
                            jakarta.persistence.criteria.Expression<UUID> domainIdExpr = domainJoin.get("id");
                            
                            Map<UUID, String> targetMap = "idAttribute".equals(prop) ? idKeyMap : nameKeyMap;
                            
                            jakarta.persistence.criteria.CriteriaBuilder.Case<String> caseExpr = cb.selectCase();
                            
                            for (Map.Entry<UUID, String> entry : targetMap.entrySet()) {
                                String keyStr = entry.getValue();
                                
                                jakarta.persistence.criteria.Expression<String> extractAfter = cb.function("jsonb_extract_path_text", String.class, 
                                    root.get("changes"), cb.literal("after"), cb.literal(keyStr));
                                jakarta.persistence.criteria.Expression<String> extractData = cb.function("jsonb_extract_path_text", String.class, 
                                    root.get("changes"), cb.literal("data"), cb.literal(keyStr));
                                jakarta.persistence.criteria.Expression<String> extractRoot = cb.function("jsonb_extract_path_text", String.class, 
                                    root.get("changes"), cb.literal(keyStr));
                                
                                jakarta.persistence.criteria.Expression<String> extractText = cb.coalesce(
                                    extractAfter, 
                                    cb.coalesce(extractData, extractRoot)
                                );
                                
                                caseExpr = caseExpr.when(cb.equal(domainIdExpr, entry.getKey()), extractText);
                            }
                            
                            expression = caseExpr.otherwise(cb.literal(""));
                        } catch (Exception ex) {
                            log.error("Failed to build sort expression for displayName", ex);
                            expression = root.get("changes").as(String.class);
                        }
                    } else if ("summary".equals(prop)) {
                        expression = root.get("changes").as(String.class);
                    } else {
                        expression = root.get(prop);
                    }
                    
                    orders.add(isAsc ? cb.asc(expression) : cb.desc(expression));
                }
                query.orderBy(orders);
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        
        org.springframework.data.domain.Pageable unsortedPageable = org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return approvalRepository.findAll(spec, unsortedPageable);
    }
    
    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, Pageable pageable) {
        return getMyTodos(assigneeId, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, String userRole, Pageable pageable) {
        if (userRole != null && !userRole.isBlank()) {
            return stepRepository.findMyPendingSteps(assigneeId, userRole, "PENDING", pageable);
        }
        return stepRepository.findByAssigneeIdAndStatus(assigneeId, "PENDING", pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getMyRequests(String requesterId, Pageable pageable) {
        return approvalRepository.findByRequesterIdOrderByCreatedAtDesc(requesterId, pageable);
    }

    @Transactional(readOnly = true)
    public ApprovalRequest getRequestById(UUID id) {
        return approvalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("ApprovalRequest not found"));
    }

    @Transactional
    public ApprovalRequest requestRecordMerge(RecordMergeService.MergeRequest request, String requesterId) {
        if (request == null || request.survivorRecordId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Survivor record ID is required.");
        }
        Record survivor = recordRepository.findById(request.survivorRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Survivor record not found: " + request.survivorRecordId));

        if ("MERGED".equalsIgnoreCase(survivor.getStatus()) || "REJECTED".equalsIgnoreCase(survivor.getStatus())) {
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
        approval.setTargetType("RECORD_MERGE");
        approval.setTargetId(survivor.getId());
        approval.setRequesterId(requesterId);
        approval.setClassificationNode(survivor.getNode());
        approval.setStatus("PENDING");

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            approval.setChanges(mapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Failed to serialize merge request.");
        }

        UUID nodeId = survivor.getNode() != null ? survivor.getNode().getId() : null;
        List<WorkflowConfig> configs = resolveWorkflows(nodeId, "MERGE");
        WorkflowConfig config = configs != null && !configs.isEmpty() ? configs.get(0) : null;
        buildDynamicSteps(approval, config);
        approval.setCurrentStepOrder(1);

        ApprovalRequest saved = approvalRepository.saveAndFlush(approval);
        eventPublisher.publishEvent(new ApprovalRequestCreatedEvent(saved));
        return saved;
    }
}
