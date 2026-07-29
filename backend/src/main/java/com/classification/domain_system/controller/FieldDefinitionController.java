package com.classification.domain_system.controller;

import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import com.classification.domain_system.service.FieldDefinitionService;
import com.classification.domain_system.dto.FieldDefinitionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/api/nodes/{nodeId}/fields")
@RequiredArgsConstructor
public class FieldDefinitionController {
    
    private final FieldDefinitionService fieldService;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.classification.domain_system.service.SchemaHistoryService schemaHistoryService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'field:write')")
    public ResponseEntity<FieldDefinition> addField(
            @PathVariable UUID nodeId, 
            @RequestBody FieldDefinitionRequest request) {
        return ResponseEntity.ok(fieldService.addField(nodeId, request));
    }

    @GetMapping("/effective/page")
    @PreAuthorize("hasPermission(null, 'field:read')")
    public ResponseEntity<PageResponse<FieldDefinition>> getEffectiveFieldsPage(
            @PathVariable UUID nodeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Page<FieldDefinition> p = fieldService.getEffectiveFieldsPage(nodeId, PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.of(p));
    }

    @GetMapping("/effective")
    @PreAuthorize("hasPermission(null, 'field:read')")
    public ResponseEntity<List<FieldDefinition>> getEffectiveFields(@PathVariable UUID nodeId) {
        return ResponseEntity.ok(fieldService.getEffectiveFields(nodeId));
    }

    @GetMapping("/effective/as-of")
    @PreAuthorize("hasPermission(null, 'field:read')")
    public ResponseEntity<List<FieldDefinition>> getEffectiveFieldsAsOf(
            @PathVariable UUID nodeId,
            @RequestParam String asOf) {
        java.time.LocalDateTime targetAsOf = parseDateTime(asOf);
        List<FieldDefinition> result = schemaHistoryService != null
                ? schemaHistoryService.getEffectiveFieldsAsOf(nodeId, targetAsOf)
                : fieldService.getEffectiveFields(nodeId);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{fieldId}")
    @PreAuthorize("hasPermission(null, 'field:write')")
    public ResponseEntity<FieldDefinition> updateField(
            @PathVariable UUID nodeId,
            @PathVariable UUID fieldId,
            @RequestBody FieldDefinitionRequest request) {
        return ResponseEntity.ok(fieldService.updateField(nodeId, fieldId, request));
    }

    private java.time.LocalDateTime parseDateTime(String asOfStr) {
        if (asOfStr == null || asOfStr.isBlank()) {
            return java.time.LocalDateTime.now();
        }
        try {
            if (asOfStr.contains("T")) {
                if (asOfStr.contains("+") || asOfStr.endsWith("Z")) {
                    return java.time.ZonedDateTime.parse(asOfStr).toLocalDateTime();
                }
                return java.time.LocalDateTime.parse(asOfStr);
            }
            return java.time.LocalDate.parse(asOfStr).atStartOfDay();
        } catch (Exception e) {
            return java.time.LocalDateTime.now();
        }
    }
}
