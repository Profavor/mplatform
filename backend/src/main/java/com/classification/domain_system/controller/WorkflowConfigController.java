package com.classification.domain_system.controller;

import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.service.WorkflowConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/workflow-configs")
@RequiredArgsConstructor
public class WorkflowConfigController {

    private final WorkflowConfigService workflowConfigService;

    @GetMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'workflow:read')")
    public ResponseEntity<List<WorkflowConfig>> getByDomain(@PathVariable UUID domainId) {
        return ResponseEntity.ok(workflowConfigService.getByDomain(domainId));
    }
    
    @GetMapping("/domain/{domainId}/all")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'workflow:read')")
    public ResponseEntity<List<WorkflowConfig>> getAllByDomain(@PathVariable UUID domainId) {
        return ResponseEntity.ok(workflowConfigService.getAllByDomain(domainId));
    }

    @GetMapping("/node/{nodeId}")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'workflow:read')")
    public ResponseEntity<List<WorkflowConfig>> getByNode(@PathVariable UUID nodeId) {
        return ResponseEntity.ok(workflowConfigService.getByNode(nodeId));
    }

    @PostMapping("/domain/{domainId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'workflow:write')")
    public ResponseEntity<List<WorkflowConfig>> saveForDomain(@PathVariable UUID domainId, @RequestBody List<WorkflowConfig> configs) {
        return ResponseEntity.ok(workflowConfigService.saveForDomain(domainId, configs));
    }

    @PostMapping("/node/{nodeId}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'workflow:write')")
    public ResponseEntity<List<WorkflowConfig>> saveForNode(@PathVariable UUID nodeId, @RequestBody List<WorkflowConfig> configs) {
        return ResponseEntity.ok(workflowConfigService.saveForNode(nodeId, configs));
    }

    @GetMapping("/page")
    @PreAuthorize("hasPermission(null, 'admin:read') or hasPermission(null, 'workflow:read')")
    public ResponseEntity<org.springframework.data.domain.Page<WorkflowConfig>> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) UUID domainId,
            @RequestParam(required = false) UUID nodeId,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(workflowConfigService.getPage(page, size, actionType, domainId, nodeId, query));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'workflow:write')")
    public ResponseEntity<WorkflowConfig> saveSingle(@RequestBody WorkflowConfig config) {
        return ResponseEntity.ok(workflowConfigService.saveSingle(config));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'admin:write') or hasPermission(null, 'workflow:write')")
    public ResponseEntity<Void> deleteWorkflowConfig(@PathVariable UUID id) {
        workflowConfigService.deleteWorkflowConfig(id);
        return ResponseEntity.noContent().build();
    }
}
