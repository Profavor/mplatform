package com.classification.domain_system.service;

import com.classification.domain_system.dto.RoleBackupDto;
import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final com.classification.domain_system.repository.UserRoleRepository userRoleRepository;

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> getRolesByOrg(UUID orgId) {
        return roleRepository.findByOrganizationIdOrOrganizationIdIsNull(orgId);
    }

    @Transactional
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public Optional<Role> updateRole(UUID id, Role updated) {
        return roleRepository.findById(id).map(existing -> {
            if (updated.getName() != null && !updated.getName().isBlank()) {
                existing.setName(updated.getName().trim());
            }
            existing.setDisplayName(updated.getDisplayName());
            existing.setDescription(updated.getDescription());
            existing.setPermissions(updated.getPermissions());
            return roleRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteRole(UUID id) {
        return roleRepository.findById(id).map(role -> {
            userRoleRepository.deleteByRoleId(id);
            roleRepository.delete(role);
            return true;
        }).orElse(false);
    }

    @Transactional(readOnly = true)
    public List<RoleBackupDto> exportRolesForOrg(UUID orgId) {
        List<Role> roles = roleRepository.findByOrganizationId(orgId);
        return roles.stream().map(role -> RoleBackupDto.builder()
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .permissions(role.getPermissions() != null ? new HashSet<>(role.getPermissions()) : new HashSet<>())
                .isSystemRole(role.getIsSystemRole())
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional
    public void importRolesForOrg(UUID orgId, List<RoleBackupDto> backups) {
        if (backups == null || backups.isEmpty()) return;

        for (RoleBackupDto backup : backups) {
            Optional<Role> existingOpt = roleRepository.findByOrganizationIdAndName(orgId, backup.getName());
            
            Role role;
            if (existingOpt.isPresent()) {
                role = existingOpt.get();
                // Upsert logic: Update fields
                if (backup.getDisplayName() != null) role.setDisplayName(backup.getDisplayName());
                if (backup.getDescription() != null) role.setDescription(backup.getDescription());
                if (backup.getIsSystemRole() != null) role.setIsSystemRole(backup.getIsSystemRole());
                
                // Merge permissions (add new ones, keep existing ones) or just replace? 
                // Replacing is usually better for a "Restore from template" feature to ensure exact match.
                // Let's replace the permissions entirely based on the backup.
                role.setPermissions(backup.getPermissions() != null ? new HashSet<>(backup.getPermissions()) : new HashSet<>());
            } else {
                role = new Role();
                role.setOrganizationId(orgId);
                role.setName(backup.getName());
                role.setDisplayName(backup.getDisplayName());
                role.setDescription(backup.getDescription());
                role.setPermissions(backup.getPermissions() != null ? new HashSet<>(backup.getPermissions()) : new HashSet<>());
                role.setIsSystemRole(backup.getIsSystemRole() != null ? backup.getIsSystemRole() : false);
            }
            
            roleRepository.save(role);
        }
        log.info("Successfully imported {} roles for organization {}", backups.size(), orgId);
    }
}
