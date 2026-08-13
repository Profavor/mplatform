package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Role;
import com.classification.domain_system.repository.RoleRepository;
import com.classification.domain_system.repository.UserRoleRepository;
import com.classification.domain_system.service.RoleInitializer;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleInitializer roleInitializer;
    private final com.classification.domain_system.service.SystemSeedDumpService systemSeedDumpService;

    public RoleController(RoleRepository roleRepository, UserRoleRepository userRoleRepository, RoleInitializer roleInitializer, com.classification.domain_system.service.SystemSeedDumpService systemSeedDumpService) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleInitializer = roleInitializer;
        this.systemSeedDumpService = systemSeedDumpService;
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'role:read')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/org/{orgId}")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'role:read')")
    public ResponseEntity<List<Role>> getRolesByOrg(@PathVariable UUID orgId) {
        return ResponseEntity.ok(roleRepository.findByOrganizationIdOrOrganizationIdIsNull(orgId));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity.ok(roleRepository.save(role));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<Role> updateRole(@PathVariable UUID id, @RequestBody Role updated) {
        return roleRepository.findById(id)
                .map(existing -> {
                    if (updated.getName() != null && !updated.getName().isBlank()) {
                        existing.setName(updated.getName().trim());
                    }
                    existing.setDisplayName(updated.getDisplayName());
                    existing.setDescription(updated.getDescription());
                    existing.setPermissions(updated.getPermissions());
                    return ResponseEntity.ok(roleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        return roleRepository.findById(id)
                .map(role -> {
                    userRoleRepository.deleteByRoleId(id);
                    roleRepository.delete(role);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync-defaults")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<Void> syncDefaultRolesForAllOrganizations() {
        roleInitializer.syncDefaultRolesForAllOrganizations();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/org/{orgId}/sync-defaults")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<Void> syncDefaultRolesForOrg(@PathVariable UUID orgId) {
        roleInitializer.createDefaultRolesForOrg(orgId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dump-seed")
    @PreAuthorize("hasPermission(null, 'admin:write')")
    public ResponseEntity<Void> dumpSeedFiles(@RequestParam(required = false) UUID orgId) {
        systemSeedDumpService.dumpCurrentStateToSeedFiles(orgId);
        return ResponseEntity.ok().build();
    }
}

