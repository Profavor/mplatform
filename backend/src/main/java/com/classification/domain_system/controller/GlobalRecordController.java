package com.classification.domain_system.controller;

import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.RecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import org.springframework.data.domain.Page;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class GlobalRecordController {
    private final RecordRepository recordRepository;
    private final com.classification.domain_system.service.ApprovalService approvalService;
    private final com.classification.domain_system.service.RecordService recordService;
    private final com.classification.domain_system.service.MultiAxisRecordService multiAxisRecordService;
    
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<Record> getRecord(@PathVariable UUID id) {
        return recordRepository.findById(id)
                .map(recordService::prepareRecordForRead)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/update-request")
    @PreAuthorize("hasPermission(null, 'record:write') or hasPermission(null, 'workflow:request')")
    public ResponseEntity<?> updateRecordRequest(
            @PathVariable UUID id, 
            @RequestBody com.classification.domain_system.dto.RecordRequest request) {
        try {
            if (request != null && request.getData() != null) {
                Record record = recordRepository.findById(id).orElse(null);
                if (record != null && record.getNode() != null) {
                    request.setData(recordService.processDataForSave(record.getNode().getId(), request.getData()));
                }
            }
            return ResponseEntity.ok(approvalService.requestRecordUpdate(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/delete-request")
    @PreAuthorize("hasPermission(null, 'record:write') or hasPermission(null, 'workflow:request')")
    public ResponseEntity<?> deleteRecordRequest(
            @PathVariable UUID id,
            @RequestBody com.classification.domain_system.dto.RecordRequest request) {
        try {
            return ResponseEntity.ok(approvalService.requestRecordDeletion(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/domain/{domainId}")
    public ResponseEntity<PageResponse<Record>> getRecordsByDomain(
            @PathVariable UUID domainId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam java.util.Map<String, String> allParams) {
        
        java.util.Map<String, String> searchParams = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("search_")) {
                searchParams.put(entry.getKey().substring(7), entry.getValue());
            }
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
        String sortField = allParams.get("sortField");
        String sortOrder = allParams.get("sortOrder");
        if (sortField == null && allParams.containsKey("sort")) {
            String sortParam = allParams.get("sort");
            String[] parts = sortParam.split(",");
            sortField = parts[0];
            if (parts.length > 1) sortOrder = parts[1];
        }
        if (sortField != null && !sortField.isEmpty()) {
            org.springframework.data.domain.Sort.Direction dir = "DESC".equalsIgnoreCase(sortOrder)
                    ? org.springframework.data.domain.Sort.Direction.DESC
                    : org.springframework.data.domain.Sort.Direction.ASC;
            sort = org.springframework.data.domain.Sort.by(dir, sortField);
        }

        Page<Record> records = recordRepository.findDynamicRecordsByDomain(
                domainId, searchParams, PageRequest.of(page, size, sort));
        records = recordService.prepareRecordsForRead(records);
        return ResponseEntity.ok(PageResponse.of(records));
    }

    @GetMapping("/{id}/secondary-nodes")
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<java.util.List<com.classification.domain_system.dto.RecordSecondaryNodeResponse>> getSecondaryNodes(@PathVariable UUID id) {
        return ResponseEntity.ok(multiAxisRecordService.getSecondaryNodes(id));
    }

    @PostMapping("/{id}/secondary-nodes")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<java.util.List<com.classification.domain_system.dto.RecordSecondaryNodeResponse>> setSecondaryNodes(
            @PathVariable UUID id,
            @RequestBody com.classification.domain_system.dto.RecordSecondaryNodeRequest request) {
        java.util.List<UUID> nodeIds = request != null ? request.getNodeIds() : java.util.Collections.emptyList();
        return ResponseEntity.ok(multiAxisRecordService.setSecondaryNodes(id, nodeIds));
    }
}
