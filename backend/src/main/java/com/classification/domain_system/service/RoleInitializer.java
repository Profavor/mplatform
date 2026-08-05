package com.classification.domain_system.service;

import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.dto.RoleSeedDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleInitializer {

    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;

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

        List<RoleSeedDto> defaultRoles = loadDefaultRoles();
        if (defaultRoles == null || defaultRoles.isEmpty()) {
            log.warn("No default roles found in default_roles.json!");
            return;
        }

        for (RoleSeedDto dto : defaultRoles) {
            createSystemRole(orgId, dto.getName(), dto.getDisplayName(), dto.getDescription(),
                    dto.getPermissions() != null ? new HashSet<>(dto.getPermissions()) : new HashSet<>(),
                    dto.getIsSystemRole() != null ? dto.getIsSystemRole() : false);
        }
    }

    private List<RoleSeedDto> loadDefaultRoles() {
        try {
            java.io.InputStream is = null;
            String userDir = System.getProperty("user.dir");
            java.io.File localFile = java.nio.file.Paths.get(userDir, "src", "main", "resources", "default_roles.json").toFile();
            
            if (localFile.exists()) {
                is = new java.io.FileInputStream(localFile);
                log.info("Using local filesystem seed: {}", localFile.getAbsolutePath());
            } else {
                ClassPathResource resource = new ClassPathResource("default_roles.json");
                if (resource.exists()) {
                    is = resource.getInputStream();
                    log.info("Using classpath seed");
                }
            }
            
            if (is == null) {
                log.warn("default_roles.json not found in resources!");
                return null;
            }
            
            try (java.io.InputStream finalIs = is) {
                return objectMapper.readValue(finalIs, new TypeReference<List<RoleSeedDto>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to load default_roles.json", e);
            return null;
        }
    }

    private void createSystemRole(UUID orgId, String name, String displayName, String description, Set<String> permissions, boolean isSystemRole) {
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
            role.setIsSystemRole(isSystemRole);
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
                
                if (!java.util.Objects.equals(role.getDisplayName(), displayName)) {
                    role.setDisplayName(displayName);
                    updated = true;
                }
                
                if (!java.util.Objects.equals(role.getDescription(), description)) {
                    role.setDescription(description);
                    updated = true;
                }
                
                if (updated) {
                    role.setPermissions(curPerms);
                    roleRepository.save(role);
                }
            });
        }
    }
}
