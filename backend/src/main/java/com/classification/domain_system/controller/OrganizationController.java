package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.Department;
import com.classification.domain_system.entity.Team;
import com.classification.domain_system.repository.OrganizationRepository;
import com.classification.domain_system.repository.DepartmentRepository;
import com.classification.domain_system.repository.TeamRepository;
import com.classification.domain_system.service.RoleInitializer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final RoleInitializer roleInitializer;
    private final com.classification.domain_system.repository.RoleRepository roleRepository;
    private final com.classification.domain_system.repository.UserRoleRepository userRoleRepository;

    public OrganizationController(OrganizationRepository organizationRepository,
                                  DepartmentRepository departmentRepository,
                                  TeamRepository teamRepository,
                                  RoleInitializer roleInitializer,
                                  com.classification.domain_system.repository.RoleRepository roleRepository,
                                  com.classification.domain_system.repository.UserRoleRepository userRoleRepository) {
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.teamRepository = teamRepository;
        this.roleInitializer = roleInitializer;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<Organization> getOrganization(@PathVariable UUID id) {
        return organizationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization org) {
        Organization saved = organizationRepository.save(org);
        roleInitializer.createDefaultRolesForOrg(saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID id, @RequestBody Organization req) {
        return organizationRepository.findById(id)
                .map(existing -> {
                    existing.setDisplayName(req.getDisplayName());
                    existing.setDescription(req.getDescription());
                    existing.setIcon(req.getIcon());
                    return ResponseEntity.ok(organizationRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        return organizationRepository.findById(id)
                .map(org -> {
                    List<Team> teams = teamRepository.findByOrganizationId(id);
                    if (teams != null && !teams.isEmpty()) {
                        teamRepository.deleteAll(teams);
                    }
                    List<Department> depts = departmentRepository.findByOrganizationId(id);
                    if (depts != null && !depts.isEmpty()) {
                        departmentRepository.deleteAll(depts);
                    }
                    List<com.classification.domain_system.entity.Role> roles = roleRepository.findByOrganizationId(id);
                    if (roles != null && !roles.isEmpty()) {
                        for (com.classification.domain_system.entity.Role r : roles) {
                            userRoleRepository.deleteByRoleId(r.getId());
                        }
                        roleRepository.deleteAll(roles);
                    }
                    organizationRepository.delete(org);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{orgId}/departments")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<List<Department>> getDepartments(@PathVariable UUID orgId) {
        return ResponseEntity.ok(departmentRepository.findByOrganizationId(orgId));
    }

    @PostMapping("/{orgId}/departments")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Department> createDepartment(@PathVariable UUID orgId, @RequestBody Department dept) {
        dept.setOrganizationId(orgId);
        if (dept.getRoles() != null && !dept.getRoles().isEmpty()) {
            java.util.Set<String> clean = dept.getRoles().stream()
                    .filter(r -> r != null && !r.trim().isEmpty())
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toSet());
            dept.setRoles(clean);
            dept.setRole(String.join(",", clean));
        } else if (dept.getRole() != null) {
            java.util.Set<String> clean = java.util.Arrays.stream(dept.getRole().split(","))
                    .filter(r -> !r.trim().isEmpty())
                    .map(String::trim)
                    .collect(java.util.stream.Collectors.toSet());
            dept.setRoles(clean);
            dept.setRole(String.join(",", clean));
        }
        return ResponseEntity.ok(departmentRepository.save(dept));
    }

    @PutMapping("/{orgId}/departments/{deptId}")
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Department> updateDepartment(@PathVariable UUID orgId, @PathVariable UUID deptId, @RequestBody Department deptReq) {
        return departmentRepository.findById(deptId)
                .map(existing -> {
                    existing.setName(deptReq.getName());
                    existing.setDescription(deptReq.getDescription());
                    existing.setParentDepartmentId(deptReq.getParentDepartmentId());
                    existing.setIcon(deptReq.getIcon());
                    if (deptReq.getRoles() != null) {
                        java.util.Set<String> clean = deptReq.getRoles().stream()
                                .filter(r -> r != null && !r.trim().isEmpty())
                                .map(String::trim)
                                .collect(java.util.stream.Collectors.toSet());
                        existing.getRoles().clear();
                        existing.getRoles().addAll(clean);
                        existing.setRole(String.join(",", clean));
                    } else if (deptReq.getRole() != null) {
                        java.util.Set<String> clean = java.util.Arrays.stream(deptReq.getRole().split(","))
                                .filter(r -> !r.trim().isEmpty())
                                .map(String::trim)
                                .collect(java.util.stream.Collectors.toSet());
                        existing.getRoles().clear();
                        existing.getRoles().addAll(clean);
                        existing.setRole(String.join(",", clean));
                    }
                    return ResponseEntity.ok(departmentRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orgId}/departments/{deptId}")
    @org.springframework.transaction.annotation.Transactional
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID orgId, @PathVariable UUID deptId) {
        return departmentRepository.findById(deptId)
                .map(dept -> {
                    // 하위 부서들의 상위 부서 연결을 삭제되는 부서의 상위 부서로 재연결 (또는 최상위로 이동)
                    List<Department> allDepts = departmentRepository.findByOrganizationId(orgId);
                    for (Department d : allDepts) {
                        if (deptId.equals(d.getParentDepartmentId())) {
                            d.setParentDepartmentId(dept.getParentDepartmentId());
                            departmentRepository.save(d);
                        }
                    }
                    // 하위 팀 삭제
                    List<Team> teams = teamRepository.findByDepartmentId(deptId);
                    if (teams != null && !teams.isEmpty()) {
                        teamRepository.deleteAll(teams);
                    }
                    departmentRepository.delete(dept);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{orgId}/teams")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<List<Team>> getTeams(@PathVariable UUID orgId) {
        return ResponseEntity.ok(teamRepository.findByOrganizationId(orgId));
    }

    @PostMapping("/{orgId}/teams")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Team> createTeam(@PathVariable UUID orgId, @RequestBody Team team) {
        team.setOrganizationId(orgId);
        return ResponseEntity.ok(teamRepository.save(team));
    }
}
