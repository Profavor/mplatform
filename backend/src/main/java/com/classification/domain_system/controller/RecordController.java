package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.service.ApprovalService;
import com.classification.domain_system.service.BatchValidationService;
import com.classification.domain_system.service.RecordService;
import com.classification.domain_system.dto.BatchValidationResult;
import com.classification.domain_system.dto.RecordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Map;
import org.springframework.data.domain.Page;
import com.classification.domain_system.dto.PageResponse;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/nodes/{nodeId}/records")
@RequiredArgsConstructor
public class RecordController {
    
    private final ApprovalService approvalService;
    private final RecordService recordService;
    private final BatchValidationService batchValidationService;
    
    @PostMapping
    @PreAuthorize("hasPermission(null, 'record:write') or hasPermission(null, 'workflow:request')")
    public ResponseEntity<ApprovalRequest> createRecordRequest(
            @PathVariable UUID nodeId, 
            @RequestBody RecordRequest request) {
        if (request != null && request.getData() != null) {
            request.setData(recordService.processDataForSave(nodeId, request.getData()));
        }
        return ResponseEntity.ok(approvalService.requestRecordCreation(nodeId, request));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasPermission(null, 'record:write') or hasPermission(null, 'workflow:request')")
    public ResponseEntity<List<ApprovalRequest>> createBatchRecords(
            @PathVariable UUID nodeId, 
            @RequestBody List<RecordRequest> requests) {
        List<ApprovalRequest> approvals = new ArrayList<>();
        for (RecordRequest req : requests) {
            approvals.add(approvalService.requestRecordCreation(nodeId, req));
        }
        return ResponseEntity.ok(approvals);
    }

    @PostMapping("/batch-validate")
    @PreAuthorize("hasPermission(null, 'record:write') or hasPermission(null, 'workflow:request')")
    public ResponseEntity<BatchValidationResult> validateBatchRecords(
            @PathVariable UUID nodeId,
            @RequestBody List<RecordRequest> requests) {
        return ResponseEntity.ok(batchValidationService.validateBatch(nodeId, requests));
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
        
        Page<Record> records = recordService.getRecords(nodeId, status, includeChildren, page, size, allParams);
        return ResponseEntity.ok(PageResponse.of(records));
    }
}
