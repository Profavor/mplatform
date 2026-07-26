package com.classification.domain_system.controller;

import com.classification.domain_system.entity.PermissionGroup;
import com.classification.domain_system.entity.PermissionItem;
import com.classification.domain_system.service.PermissionMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/permissions/groups")
@RequiredArgsConstructor
public class PermissionMasterController {

    private final PermissionMasterService permissionMasterService;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'role:read')")
    public ResponseEntity<List<PermissionGroup>> getAllGroups() {
        return ResponseEntity.ok(permissionMasterService.getAllPermissionGroups());
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<PermissionGroup> createGroup(@RequestBody PermissionGroup group) {
        return ResponseEntity.ok(permissionMasterService.createGroup(group));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<PermissionGroup> updateGroup(@PathVariable String id, @RequestBody PermissionGroup group) {
        return ResponseEntity.ok(permissionMasterService.updateGroup(id, group));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        permissionMasterService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/items")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<PermissionItem> addItemToGroup(@PathVariable String groupId, @RequestBody PermissionItem item) {
        return ResponseEntity.ok(permissionMasterService.addItemToGroup(groupId, item));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'role:write')")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID itemId) {
        permissionMasterService.deleteItem(itemId);
        return ResponseEntity.ok().build();
    }
}
