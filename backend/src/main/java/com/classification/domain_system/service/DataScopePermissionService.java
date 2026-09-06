package com.classification.domain_system.service;

import com.classification.domain_system.dto.DataScopePermissionDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.DataScopePermission;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DataScopePermissionRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataScopePermissionService {

    private final DataScopePermissionRepository scopeRepository;
    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final PermissionAuditService auditService;

    @Transactional(readOnly = true)
    public List<DataScopePermissionDto.Response> getUserScopes(String userId) {
        List<DataScopePermission> list = scopeRepository.findByUserId(userId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional
    public DataScopePermissionDto.Response grantScope(String userId, DataScopePermissionDto.Request request, String operator) {
        if (request == null || request.getDomainId() == null) {
            throw new IllegalArgumentException("domainId is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Domain domain = domainRepository.findById(request.getDomainId())
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found: " + request.getDomainId()));

        ClassificationNode node = null;
        if (request.getNodeId() != null) {
            node = nodeRepository.findById(request.getNodeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassificationNode not found: " + request.getNodeId()));
        }

        String level = request.getPermissionLevel() != null ? request.getPermissionLevel().trim().toUpperCase() : "READ";

        Optional<DataScopePermission> existing = scopeRepository.findByUserIdAndDomainIdAndNodeId(
                userId, request.getDomainId(), request.getNodeId());

        DataScopePermission scope;
        String beforeVal = null;
        if (existing.isPresent()) {
            scope = existing.get();
            beforeVal = "Level=" + scope.getPermissionLevel();
            scope.setPermissionLevel(level);
        } else {
            scope = DataScopePermission.builder()
                    .user(user)
                    .domain(domain)
                    .node(node)
                    .permissionLevel(level)
                    .createdBy(operator)
                    .build();
        }

        DataScopePermission saved = scopeRepository.save(scope);

        // Audit Log
        String scopeDesc = resolveDomainName(domain);
        if (node != null) {
            scopeDesc += " > " + resolveNodeName(node);
        }

        auditService.recordLog(
                user.getId(), user.getUsername(), "GRANT_SCOPE", "DATA_SCOPE",
                saved.getId().toString(), scopeDesc,
                beforeVal, "Level=" + level,
                operator, null
        );

        return toResponse(saved);
    }

    @Transactional
    public void revokeScope(UUID scopeId, String operator) {
        DataScopePermission scope = scopeRepository.findById(scopeId)
                .orElseThrow(() -> new ResourceNotFoundException("DataScopePermission not found: " + scopeId));

        String userId = scope.getUser().getId();
        String username = scope.getUser().getUsername();
        String scopeDesc = resolveDomainName(scope.getDomain()) + (scope.getNode() != null ? (" > " + resolveNodeName(scope.getNode())) : "");

        scopeRepository.delete(scope);

        // Audit Log
        auditService.recordLog(
                userId, username, "REVOKE_SCOPE", "DATA_SCOPE",
                scopeId.toString(), scopeDesc,
                "Level=" + scope.getPermissionLevel(), null,
                operator, null
        );
    }

    private String resolveDomainName(Domain domain) {
        if (domain == null || domain.getName() == null || domain.getName().isEmpty()) {
            return domain != null && domain.getId() != null ? domain.getId().toString() : "Unknown";
        }
        return domain.getName().getOrDefault("ko", domain.getName().values().iterator().next());
    }

    private String resolveNodeName(ClassificationNode node) {
        if (node == null || node.getName() == null || node.getName().isEmpty()) {
            return node != null && node.getId() != null ? node.getId().toString() : "Unknown";
        }
        return node.getName().getOrDefault("ko", node.getName().values().iterator().next());
    }

    /**
     * 사용자가 특정 도메인 및 노드에 대해 지정된 레벨의 권한을 갖고 있는지 판정
     */
    @Transactional(readOnly = true)
    public boolean hasScopeAccess(User user, UUID domainId, UUID nodeId, String requiredLevel) {
        if (user == null || domainId == null) return false;

        // 시스템 전역 관리자(ROLE_ADMIN, SUPER_ADMIN)는 모든 도메인/노드에 최고 권한 보유
        if (user.getRole() != null) {
            String roleUpper = user.getRole().toUpperCase();
            if (roleUpper.contains("ADMIN") || roleUpper.contains("SUPER")) {
                return true;
            }
        }

        List<DataScopePermission> scopes = scopeRepository.findByUserIdAndDomainId(user.getId(), domainId);
        if (scopes.isEmpty()) return false;

        for (DataScopePermission s : scopes) {
            // 도메인 전체 스코프(node == null)이거나 특정 노드 일치
            boolean nodeMatch = s.getNode() == null || (nodeId != null && s.getNode().getId().equals(nodeId));
            if (nodeMatch) {
                if (isLevelSufficient(s.getPermissionLevel(), requiredLevel)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isLevelSufficient(String grantedLevel, String requiredLevel) {
        if (grantedLevel == null) return false;
        String g = grantedLevel.trim().toUpperCase();
        String r = requiredLevel != null ? requiredLevel.trim().toUpperCase() : "READ";

        if ("ADMIN".equals(g)) return true;
        if ("WRITE".equals(g)) return "WRITE".equals(r) || "READ".equals(r);
        if ("READ".equals(g)) return "READ".equals(r);

        return false;
    }

    public DataScopePermissionDto.Response toResponse(DataScopePermission scope) {
        String code = "SCP-" + (scope.getId() != null ? scope.getId().toString().substring(0, 8) : "00000000");

        return DataScopePermissionDto.Response.builder()
                .id(scope.getId())
                .scopeCode(code)
                .userId(scope.getUser().getId())
                .username(scope.getUser().getUsername())
                .domainId(scope.getDomain().getId())
                .domainName(scope.getDomain().getName())
                .nodeId(scope.getNode() != null ? scope.getNode().getId() : null)
                .nodeName(scope.getNode() != null ? scope.getNode().getName() : null)
                .permissionLevel(scope.getPermissionLevel())
                .createdAt(scope.getCreatedAt())
                .createdBy(scope.getCreatedBy())
                .build();
    }
}
