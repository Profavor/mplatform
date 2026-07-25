package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.ApprovalService;
import com.classification.domain_system.repository.RecordRepository;
import com.classification.domain_system.dto.RecordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import org.springframework.data.domain.Page;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/nodes/{nodeId}/records")
@RequiredArgsConstructor
public class RecordController {
    
    private final ApprovalService approvalService;
    private final RecordRepository recordRepository;
    private final ClassificationNodeRepository classificationNodeRepository;
    private final com.classification.domain_system.service.RecordService recordService;
    
    @PostMapping
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<ApprovalRequest> createRecordRequest(
            @PathVariable UUID nodeId, 
            @RequestBody RecordRequest request) {
        if (request != null && request.getData() != null) {
            request.setData(recordService.processDataForSave(nodeId, request.getData()));
        }
        return ResponseEntity.ok(approvalService.requestRecordCreation(nodeId, request));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasPermission(null, 'record:write')")
    public ResponseEntity<List<ApprovalRequest>> createBatchRecords(
            @PathVariable UUID nodeId, 
            @RequestBody List<RecordRequest> requests) {
        List<ApprovalRequest> approvals = new ArrayList<>();
        for (RecordRequest req : requests) {
            approvals.add(approvalService.requestRecordCreation(nodeId, req));
        }
        return ResponseEntity.ok(approvals);
    }
    
    @GetMapping
    @PreAuthorize("hasPermission(null, 'record:read')")
    public ResponseEntity<PageResponse<Record>> getRecords(
            @PathVariable UUID nodeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "false") boolean includeChildren,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam Map<String, String> allParams) {
        
        List<UUID> targetNodeIds = new ArrayList<>();
        targetNodeIds.add(nodeId);
        
        if (includeChildren) {
            List<ClassificationNode> children = classificationNodeRepository.findByParentIdAndIsDeletedFalseOrderByOrderAsc(nodeId);
            targetNodeIds.addAll(children.stream().map(ClassificationNode::getId).collect(Collectors.toList()));
        }

        java.util.Map<String, String> searchParams = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
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

        Page<Record> records = recordRepository.findDynamicRecords(
                targetNodeIds, status, searchParams, PageRequest.of(page, size, sort));
        records = recordService.prepareRecordsForRead(records);
        return ResponseEntity.ok(PageResponse.of(records));
    }
}
