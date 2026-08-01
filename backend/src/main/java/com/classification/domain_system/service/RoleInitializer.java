package com.classification.domain_system.service;

import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer {

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public void syncDefaultRolesForAllOrganizations() {
        if (organizationRepository == null) return;
        log.info("Starting default roles and permissions sync across all organizations...");
        organizationRepository.findAll().forEach(org -> {
            if (org != null && org.getId() != null) {
                createDefaultRolesForOrg(org.getId());
            }
        });
        log.info("Completed default roles and permissions sync across all organizations.");
    }

    @Transactional
    public void createDefaultRolesForOrg(UUID orgId) {
        if (orgId == null) return;

        // DB 내 INTGRATION 오타 레코드 자동 마이그레이션
        roleRepository.findByOrganizationId(orgId).forEach(role -> {
            if ("INTGRATION".equalsIgnoreCase(role.getName()) || "ROLE_INTGRATION".equalsIgnoreCase(role.getName())) {
                role.setName("INTEGRATION");
                roleRepository.save(role);
                log.info("Migrated role name from INTGRATION to INTEGRATION for org {}", orgId);
            }
        });

        createSystemRole(orgId, "ROLE_ADMIN", "{\"ko\":\"시스템 관리자\",\"en\":\"System Admin\"}", "{\"ko\":\"시스템 전체 관리 권한\",\"en\":\"Full system administration access\"}",
                Set.of("*", "admin:read", "admin:write"));

        createSystemRole(orgId, "ORG_ADMIN", "{\"ko\":\"조직 관리자\",\"en\":\"Organization Admin\"}", "{\"ko\":\"조직 내 모든 리소스 제어 권한\",\"en\":\"Full control over organization resources\"}",
                Set.of("org:*", "domain:*", "node:*", "field:*", "dq:*", "user:*", "role:*", "admin:read", "record:*"));

        createSystemRole(orgId, "DATA_STEWARD", "{\"ko\":\"데이터 스튜어드\",\"en\":\"Data Steward\"}", "{\"ko\":\"데이터 품질 및 모델 관리 권한\",\"en\":\"Data quality and model management\"}",
                Set.of("domain:read", "domain:write", "node:*", "field:*", "dq:read", "dq:write", "record:read", "record:write"));

        createSystemRole(orgId, "DOMAIN_EDITOR", "{\"ko\":\"도메인 편집자\",\"en\":\"Domain Editor\"}", "{\"ko\":\"도메인 및 필드 생성/편집 권한\",\"en\":\"Create and edit domains and fields\"}",
                Set.of("domain:read", "domain:write", "node:read", "node:write", "field:read", "field:write", "record:read"));

        createSystemRole(orgId, "DQ_MANAGER", "{\"ko\":\"데이터 품질 관리자\",\"en\":\"DQ Manager\"}", "{\"ko\":\"품질 규칙 및 검사 결과 관리\",\"en\":\"Manage DQ rules and scan results\"}",
                Set.of("dq:read", "dq:write", "dq_rule:*", "dq_scan:*"));

        createSystemRole(orgId, "INTEGRATION", "{\"ko\":\"연계 관리자\",\"en\":\"Integration Manager\"}", "{\"ko\":\"외부 시스템 연동 및 채널 관리 권한\",\"en\":\"External system integration and channel management\"}",
                Set.of("integration:*", "channel:*", "org:read", "domain:read", "node:read", "field:read", "user:read"));

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
            role.setPermissions(new HashSet<>(permissions));
            role.setIsSystemRole(true);
            roleRepository.save(role);
        } else {
            existingOpt.ifPresent(role -> {
                Set<String> curPerms = role.getPermissions() == null ? new HashSet<>() : new HashSet<>(role.getPermissions());
                boolean updated = false;
                for (String perm : permissions) {
                    if (curPerms.add(perm)) {
                        updated = true;
                    }
                }
                if (updated) {
                    role.setPermissions(curPerms);
                    roleRepository.save(role);
                }
            });
        }
    }
}

