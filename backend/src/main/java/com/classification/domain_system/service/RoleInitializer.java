package com.classification.domain_system.service;

import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class RoleInitializer {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Transactional
    public void createDefaultRolesForOrg(UUID orgId) {
        if (orgId == null) return;

        // 해당 조직에 이미 역할이 존재하는 경우(사용자가 수정/삭제한 경우 포함) 더 이상 초기화를 진행하지 않음
        if (!roleRepository.findByOrganizationId(orgId).isEmpty()) {
            return;
        }

        createSystemRole(orgId, "ROLE_ADMIN", "{\"ko\":\"시스템 관리자\",\"en\":\"System Admin\"}", "{\"ko\":\"시스템 전체 관리 권한\",\"en\":\"Full system administration access\"}",
                Set.of("*"));

        createSystemRole(orgId, "ORG_ADMIN", "{\"ko\":\"조직 관리자\",\"en\":\"Organization Admin\"}", "{\"ko\":\"조직 내 모든 리소스 제어 권한\",\"en\":\"Full control over organization resources\"}",
                Set.of("org:*", "domain:*", "node:*", "field:*", "dq:*", "user:*", "role:*"));

        createSystemRole(orgId, "DATA_STEWARD", "{\"ko\":\"데이터 스튜어드\",\"en\":\"Data Steward\"}", "{\"ko\":\"데이터 품질 및 모델 관리 권한\",\"en\":\"Data quality and model management\"}",
                Set.of("domain:read", "domain:write", "node:*", "field:*", "dq:read", "dq:write"));

        createSystemRole(orgId, "DOMAIN_EDITOR", "{\"ko\":\"도메인 편집자\",\"en\":\"Domain Editor\"}", "{\"ko\":\"도메인 및 필드 생성/편집 권한\",\"en\":\"Create and edit domains and fields\"}",
                Set.of("domain:read", "node:read", "field:read", "node:write", "field:write"));

        createSystemRole(orgId, "DQ_MANAGER", "{\"ko\":\"데이터 품질 관리자\",\"en\":\"DQ Manager\"}", "{\"ko\":\"품질 규칙 및 검사 결과 관리\",\"en\":\"Manage DQ rules and scan results\"}",
                Set.of("dq:read", "dq:write", "dq_rule:*", "dq_scan:*"));

        createSystemRole(orgId, "VIEWER", "{\"ko\":\"조회자\",\"en\":\"Viewer\"}", "{\"ko\":\"도메인, 노드, 필드, 레코드 및 품질 정보 읽기 전용\",\"en\":\"Read-only access to domains, nodes, fields, records and DQ\"}",
                Set.of("domain:read", "node:read", "field:read", "record:read", "dq:read"));

        createSystemRole(orgId, "ROLE_USER", "{\"ko\":\"일반 사용자\",\"en\":\"General User\"}", "{\"ko\":\"기본 사용자 접근 권한\",\"en\":\"Basic user access\"}",
                Set.of("domain:read", "node:read", "record:read"));
    }

    private void createSystemRole(UUID orgId, String name, String displayName, String description, Set<String> permissions) {
        String altName = name.equals("ROLE_ADMIN") ? "ADMIN" : (name.equals("ROLE_USER") ? "USER" : (name.equals("ADMIN") ? "ROLE_ADMIN" : (name.equals("USER") ? "ROLE_USER" : name)));
        
        var existingOpt = roleRepository.findByOrganizationIdAndName(orgId, name);
        if (existingOpt.isEmpty()) {
            existingOpt = roleRepository.findByOrganizationIdAndName(orgId, altName);
        }

        if (existingOpt.isEmpty()) {
            Role role = new Role();
            role.setOrganizationId(orgId);
            role.setName(name);
            role.setDisplayName(displayName);
            role.setDescription(description);
            role.setPermissions(permissions);
            role.setIsSystemRole(true);
            roleRepository.save(role);
        } else {
            // 이미 존재하는 경우에도 ROLE_ADMIN은 permissions에 '*'가 확실히 포함되도록 DB 업데이트
            existingOpt.ifPresent(role -> {
                if ("ROLE_ADMIN".equals(name) || "ADMIN".equals(name)) {
                    if (role.getPermissions() == null || !role.getPermissions().contains("*")) {
                        java.util.Set<String> perms = role.getPermissions() == null ? new java.util.HashSet<>() : new java.util.HashSet<>(role.getPermissions());
                        perms.add("*");
                        role.setPermissions(perms);
                        roleRepository.save(role);
                    }
                }
            });
        }
    }
}
