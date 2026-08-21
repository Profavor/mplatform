package com.classification.domain_system.controller;

import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.SchemaHistory;
import com.classification.domain_system.repository.SchemaHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SchemaHistoryController {

    private final com.classification.domain_system.service.SchemaHistoryService schemaHistoryService;

    @GetMapping("/api/domains/{domainId}/schema-history")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<PageResponse<SchemaHistory>> getSchemaHistory(
            @PathVariable UUID domainId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
            
        org.springframework.data.domain.Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("changedAt").descending());
        return ResponseEntity.ok(PageResponse.of(schemaHistoryService.getSchemaHistory(domainId, targetType, action, pageable)));
    }
    
    @GetMapping("/api/schema-history/{id}")
    @PreAuthorize("hasPermission(null, 'domain:read')")
    public ResponseEntity<SchemaHistory> getSchemaHistoryById(@PathVariable UUID id) {
        return schemaHistoryService.getSchemaHistoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
