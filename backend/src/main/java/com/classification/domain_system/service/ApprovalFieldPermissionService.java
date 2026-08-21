package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalFieldPermissionService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean matchesUserIdentity(String targetId, String requesterId) {
        if (targetId == null || targetId.isBlank() || requesterId == null) return false;
        if (targetId.equalsIgnoreCase(requesterId)) return true;
        try {
            Optional<User> user = userRepository.findByUsername(targetId);
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
            JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (JsonNode perm : root.get("permissions")) {
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
                            List<String> list = new ArrayList<>();
                            for (JsonNode f : perm.get("fieldPermissions").get("editableFields")) {
                                list.add(f.asText());
                            }
                            return list;
                        }
                        if (perm.has("editableFields") && perm.get("editableFields").isArray()) {
                            List<String> list = new ArrayList<>();
                            for (JsonNode f : perm.get("editableFields")) {
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
            JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (JsonNode perm : root.get("permissions")) {
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
                            List<String> list = new ArrayList<>();
                            for (JsonNode f : perm.get("readOnlyFields")) {
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
            JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (JsonNode perm : root.get("permissions")) {
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
                            List<String> list = new ArrayList<>();
                            for (JsonNode f : perm.get("hiddenFields")) {
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

    public JsonNode extractRuleName(WorkflowConfig config, String requesterId, String userRole) {
        if (config == null || config.getStepsConfig() == null || config.getStepsConfig().isBlank()) {
            return null;
        }
        try {
            JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (JsonNode perm : root.get("permissions")) {
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
            JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("permissions") && root.get("permissions").isArray()) {
                for (JsonNode perm : root.get("permissions")) {
                    String targetType = perm.has("targetType") ? perm.get("targetType").asText() : "";
                    String targetId = perm.has("targetId") ? perm.get("targetId").asText() : "";
                    String targetRole = perm.has("targetRole") ? perm.get("targetRole").asText() : "";

                    boolean matchesUser = "USER".equalsIgnoreCase(targetType) && matchesUserIdentity(targetId, requesterId);
                    boolean matchesRole = "ROLE".equalsIgnoreCase(targetType) && userRole != null && targetRole.equalsIgnoreCase(userRole);

                    if (matchesUser || matchesRole) {
                        if (perm.has("allowedActions") && perm.get("allowedActions").isArray()) {
                            boolean allowed = false;
                            for (JsonNode act : perm.get("allowedActions")) {
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
}
