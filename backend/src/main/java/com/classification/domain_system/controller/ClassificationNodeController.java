package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.service.ClassificationNodeService;
import com.classification.domain_system.dto.ClassificationNodeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/domains/{domainId}/nodes")
@RequiredArgsConstructor
public class ClassificationNodeController {
    
    private final ClassificationNodeService nodeService;
    
    @PostMapping
    @PreAuthorize("hasPermission(null, 'node:write')")
    public ResponseEntity<ClassificationNode> createNode(
            @PathVariable UUID domainId, 
            @RequestBody ClassificationNodeRequest request) {
        return ResponseEntity.ok(nodeService.createNode(domainId, request));
    }
    
    @GetMapping("/tree")
    @PreAuthorize("hasPermission(null, 'node:read')")
    public ResponseEntity<List<ClassificationNode>> getTree(
            @PathVariable UUID domainId,
            @RequestParam(required = false) UUID axisId) {
        return ResponseEntity.ok(nodeService.getTree(domainId, axisId));
    }
    
    @PutMapping("/{nodeId}")
    @PreAuthorize("hasPermission(null, 'node:write')")
    public ResponseEntity<ClassificationNode> updateNode(
            @PathVariable UUID domainId,
            @PathVariable UUID nodeId,
            @RequestBody ClassificationNodeRequest request) {
        return ResponseEntity.ok(nodeService.updateNode(domainId, nodeId, request));
    }

    @DeleteMapping("/{nodeId}")
    @PreAuthorize("hasPermission(null, 'node:write')")
    public ResponseEntity<Void> deleteNode(
            @PathVariable UUID domainId,
            @PathVariable UUID nodeId) {
        nodeService.deleteNode(domainId, nodeId);
        return ResponseEntity.noContent().build();
    }
}
