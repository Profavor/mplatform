package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.service.ClassificationNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NodeMetadataController {

    private final ClassificationNodeService nodeService;

    @GetMapping("/{nodeId}")
    @PreAuthorize("hasPermission(null, 'node:read')")
    public ResponseEntity<ClassificationNode> getNode(@PathVariable UUID nodeId) {
        return ResponseEntity.ok(nodeService.getNode(nodeId));
    }

    @GetMapping("/{nodeId}/info")
    @PreAuthorize("hasPermission(null, 'node:read')")
    public ResponseEntity<ClassificationNode> getNodeInfo(@PathVariable UUID nodeId) {
        return ResponseEntity.ok(nodeService.getNode(nodeId));
    }
}
