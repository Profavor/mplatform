package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Department;
import com.classification.domain_system.entity.Organization;
import com.classification.domain_system.entity.Team;
import com.classification.domain_system.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<Organization> getOrganization(@PathVariable UUID id) {
        return organizationService.getOrganization(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization org) {
        return ResponseEntity.ok(organizationService.createOrganization(org));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Organization> updateOrganization(@PathVariable UUID id, @RequestBody Organization req) {
        return organizationService.updateOrganization(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        boolean deleted = organizationService.deleteOrganization(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{orgId}/departments")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<List<Department>> getDepartments(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.getDepartments(orgId));
    }

    @PostMapping("/{orgId}/departments")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Department> createDepartment(@PathVariable UUID orgId, @RequestBody Department dept) {
        return ResponseEntity.ok(organizationService.createDepartment(orgId, dept));
    }

    @PutMapping("/{orgId}/departments/{deptId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Department> updateDepartment(@PathVariable UUID orgId, @PathVariable UUID deptId, @RequestBody Department deptReq) {
        return organizationService.updateDepartment(orgId, deptId, deptReq)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orgId}/departments/{deptId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID orgId, @PathVariable UUID deptId) {
        boolean deleted = organizationService.deleteDepartment(orgId, deptId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{orgId}/teams")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'org:read')")
    public ResponseEntity<List<Team>> getTeams(@PathVariable UUID orgId) {
        return ResponseEntity.ok(organizationService.getTeams(orgId));
    }

    @PostMapping("/{orgId}/teams")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'org:write')")
    public ResponseEntity<Team> createTeam(@PathVariable UUID orgId, @RequestBody Team team) {
        return ResponseEntity.ok(organizationService.createTeam(orgId, team));
    }
}
