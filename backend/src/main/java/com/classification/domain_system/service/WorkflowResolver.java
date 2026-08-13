package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import com.classification.domain_system.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowResolver {

    private final WorkflowConfigRepository workflowConfigRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final com.classification.domain_system.repository.DomainRepository domainRepository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public List<WorkflowConfig> resolveWorkflows(UUID nodeId, String actionType) {
        return resolveWorkflowsForUser(nodeId, actionType, null, null);
    }

    public List<WorkflowConfig> resolveWorkflowsForUser(UUID nodeId, String actionType, String requesterId, String userRole) {
        if (nodeId == null) return Collections.emptyList();

        List<WorkflowConfig> rawList = new ArrayList<>();
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

    private boolean isEffectiveConfig(WorkflowConfig config) {
        // Example check (could be expanded with dates etc.)
        return config != null && config.getStepsConfig() != null && !config.getStepsConfig().isEmpty();
    }

    private boolean allowsUserAction(WorkflowConfig config, String actionType, String requesterId, String userRole) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) return true;
        try {
            JsonNode root = mapper.readTree(config.getStepsConfig());
            if (!root.has("permissions") || !root.get("permissions").isArray() || root.get("permissions").isEmpty()) {
                return true;
            }
            for (JsonNode perm : root.get("permissions")) {
                String targetType = perm.has("targetType") ? perm.get("targetType").asText() : "";
                String targetId = perm.has("targetId") ? perm.get("targetId").asText() : "";
                String targetRole = perm.has("targetRole") ? perm.get("targetRole").asText() : "";

                boolean matchesUser = "USER".equalsIgnoreCase(targetType) && targetId.equalsIgnoreCase(requesterId);
                boolean matchesRole = "ROLE".equalsIgnoreCase(targetType) && userRole != null &&
                        (targetRole.equalsIgnoreCase(userRole)
                         || ("ROLE_" + targetRole).equalsIgnoreCase(userRole)
                         || targetRole.equalsIgnoreCase("ROLE_" + userRole));
                boolean matchesAll = "ALL".equalsIgnoreCase(targetType) || "EVERYONE".equalsIgnoreCase(targetType) || "*".equals(targetType) || targetType.isBlank();

                if (matchesUser || matchesRole || matchesAll) {
                    if (perm.has("allowedActions") && perm.get("allowedActions").isArray()) {
                        for (JsonNode act : perm.get("allowedActions")) {
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
        return list.stream().filter(c -> Boolean.TRUE.equals(c.getIsDefault())).findFirst().orElse(list.get(0));
    }

    public WorkflowConfig resolveWorkflowById(UUID workflowId) {
        if (workflowId == null) return null;
        return workflowConfigRepository.findById(workflowId).orElse(null);
    }

    public void buildDynamicSteps(ApprovalRequest approval, WorkflowConfig config) {
        List<ApprovalStep> steps = new ArrayList<>();
        try {
            if (config != null && config.getStepsConfig() != null && !config.getStepsConfig().isEmpty()) {
                JsonNode root = mapper.readTree(config.getStepsConfig());
                
                JsonNode stepsArray = null;
                if (root.has("approvalLine") && root.get("approvalLine").isArray()) {
                    stepsArray = root.get("approvalLine");
                } else if (root.has("steps") && root.get("steps").isArray()) {
                    stepsArray = root.get("steps");
                }

                if (stepsArray != null) {
                    for (JsonNode stepNode : stepsArray) {
                        ApprovalStep step = new ApprovalStep();
                        step.setApprovalRequest(approval);
                        step.setStepType(stepNode.has("stepType") ? stepNode.get("stepType").asText() : "APPROVAL");
                        step.setStepOrder(stepNode.has("stepOrder") ? stepNode.get("stepOrder").asInt() : (steps.size() + 1));
                        step.setStatus(step.getStepOrder() == 1 ? ApprovalStatus.PENDING.name() : ApprovalStatus.WAITING.name());
                        
                        if (stepNode.has("assigneeId") && !stepNode.get("assigneeId").asText().isBlank() && !"null".equalsIgnoreCase(stepNode.get("assigneeId").asText())) {
                            step.setAssigneeId(stepNode.get("assigneeId").asText());
                        }
                        if (stepNode.has("assigneeRole") && !stepNode.get("assigneeRole").asText().isBlank() && !"null".equalsIgnoreCase(stepNode.get("assigneeRole").asText())) {
                            step.setAssigneeRole(stepNode.get("assigneeRole").asText());
                        }
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
            log.error("Failed to parse observerIds / stepsConfig", e);
        }
        approval.getSteps().addAll(steps);
    }

    public void enrichUserNames(ApprovalRequest request) {
        if (request == null) return;
        List<User> allUsers = userRepository.findAll();
        if (request.getRequesterId() != null) {
            String reqId = request.getRequesterId();
            Optional<User> uOpt = userRepository.findById(reqId);
            if (uOpt.isEmpty()) {
                uOpt = userRepository.findByUsername(reqId);
            }
            if (uOpt.isEmpty() && !allUsers.isEmpty()) {
                uOpt = allUsers.stream().filter(u -> u.getId().equalsIgnoreCase(reqId) || u.getUsername().equalsIgnoreCase(reqId)).findFirst();
                if (uOpt.isEmpty() && allUsers.size() == 1) {
                    uOpt = Optional.of(allUsers.get(0));
                }
            }
            if (uOpt.isPresent()) {
                request.setRequesterName(uOpt.get().getUsername());
            } else if (!allUsers.isEmpty()) {
                request.setRequesterName(allUsers.get(0).getUsername());
            } else {
                request.setRequesterName(reqId);
            }
        }
        if (request.getSteps() != null) {
            for (ApprovalStep step : request.getSteps()) {
                if (step.getAssigneeId() != null && !step.getAssigneeId().isBlank() && !"null".equalsIgnoreCase(step.getAssigneeId())) {
                    String assId = step.getAssigneeId();
                    Optional<User> uOpt = userRepository.findById(assId);
                    if (uOpt.isEmpty()) {
                        uOpt = userRepository.findByUsername(assId);
                    }
                    if (uOpt.isEmpty() && !allUsers.isEmpty()) {
                        uOpt = allUsers.stream().filter(u -> u.getId().equalsIgnoreCase(assId) || u.getUsername().equalsIgnoreCase(assId)).findFirst();
                        if (uOpt.isEmpty() && allUsers.size() == 1) {
                            uOpt = Optional.of(allUsers.get(0));
                        }
                    }
                    if (uOpt.isPresent()) {
                        step.setAssigneeName(uOpt.get().getUsername());
                    } else if (!allUsers.isEmpty() && assId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                        step.setAssigneeName(allUsers.get(0).getUsername());
                    } else {
                        step.setAssigneeName(assId);
                    }
                } else if (step.getAssigneeRole() != null && !step.getAssigneeRole().isBlank() && !"null".equalsIgnoreCase(step.getAssigneeRole())) {
                    String roleDisp = resolveRoleDisplayName(step.getAssigneeRole());
                    step.setAssigneeName("역할: " + roleDisp);
                } else {
                    // Try to repair step if assignee is missing by checking current WorkflowConfig
                    try {
                        UUID nodeId = request.getClassificationNode() != null ? request.getClassificationNode().getId() : null;
                        UUID targetId = request.getTargetId();
                        WorkflowConfig config = resolveWorkflow(nodeId != null ? nodeId : targetId, "SCHEMA_CHANGE");
                        if (config == null && targetId != null) {
                            List<WorkflowConfig> list = workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(targetId, "SCHEMA_CHANGE");
                            if (list.isEmpty()) {
                                list = workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(targetId, "UPDATE");
                            }
                            if (!list.isEmpty()) config = list.get(0);
                        }
                        if (config != null && config.getStepsConfig() != null) {
                            JsonNode root = mapper.readTree(config.getStepsConfig());
                            JsonNode stepsArray = root.has("approvalLine") ? root.get("approvalLine") : (root.has("steps") ? root.get("steps") : null);
                            if (stepsArray != null && stepsArray.isArray()) {
                                for (JsonNode stepNode : stepsArray) {
                                    int order = stepNode.has("stepOrder") ? stepNode.get("stepOrder").asInt() : 1;
                                    if (order == step.getStepOrder()) {
                                        if (stepNode.has("assigneeRole") && !stepNode.get("assigneeRole").asText().isBlank()) {
                                            String role = stepNode.get("assigneeRole").asText();
                                            step.setAssigneeRole(role);
                                            step.setAssigneeName("역할: " + resolveRoleDisplayName(role));
                                        } else if (stepNode.has("assigneeId") && !stepNode.get("assigneeId").asText().isBlank()) {
                                            String aId = stepNode.get("assigneeId").asText();
                                            step.setAssigneeId(aId);
                                            step.setAssigneeName(aId);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("Fallback step repair failed", ex);
                    }
                    if (step.getAssigneeName() == null) {
                        step.setAssigneeName("지정되지 않음");
                    }
                }
            }
        }
    }

    private String resolveRoleDisplayName(String roleCode) {
        if (roleCode == null) return "Unknown";
        String r = roleCode.replace("ROLE_", "");
        if ("SYSTEM_ADMIN".equalsIgnoreCase(r)) return "시스템 관리자";
        if ("DOMAIN_ADMIN".equalsIgnoreCase(r)) return "도메인 관리자";
        if ("DATA_STEWARD".equalsIgnoreCase(r)) return "데이터 스튜어드";
        if ("DATA_OWNER".equalsIgnoreCase(r)) return "데이터 오너";
        if ("USER".equalsIgnoreCase(r)) return "일반 사용자";
        if ("GUEST".equalsIgnoreCase(r)) return "게스트";
        return r;
    }
}
