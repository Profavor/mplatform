package com.classification.domain_system.controller;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.service.ApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import org.springframework.data.domain.Page;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.PageRequest;
import com.classification.domain_system.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.exception.CustomAccessDeniedException;

@RestController
@RequestMapping("/api/approval-requests")
@RequiredArgsConstructor
public class ApprovalController {
    
    private final ApprovalService approvalService;
    private final AuthContext authContext;
    private final com.classification.domain_system.repository.UserRepository userRepository;
    
    private UUID getAuthenticatedUserId() {
        String userIdStr = authContext.getUserId();
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new CustomAccessDeniedException("Unauthenticated user context");
        }
        return UUID.fromString(userIdStr);
    }
    
    @GetMapping("/effective-workflow/{nodeId}")
    public ResponseEntity<Boolean> hasEffectiveWorkflow(@PathVariable UUID nodeId, @RequestParam String actionType) {
        com.classification.domain_system.entity.WorkflowConfig config = approvalService.resolveWorkflow(nodeId, actionType);
        boolean hasWorkflow = config != null && config.getStepsConfig() != null && !config.getStepsConfig().isEmpty() && !config.getStepsConfig().equals("{\"steps\":[],\"observerIds\":[]}");
        return ResponseEntity.ok(hasWorkflow);
    }

    @GetMapping("/available-workflows/{nodeId}")
    public ResponseEntity<List<com.classification.domain_system.entity.WorkflowConfig>> getAvailableWorkflows(
            @PathVariable UUID nodeId, 
            @RequestParam String actionType) {
        List<com.classification.domain_system.entity.WorkflowConfig> list = approvalService.resolveWorkflows(nodeId, actionType);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/effective-permission/{nodeId}")
    public ResponseEntity<Map<String, Object>> getEffectivePermission(
            @PathVariable UUID nodeId, 
            @RequestParam String actionType,
            @RequestParam(required = false) UUID workflowId) {
        UUID authenticatedUserId = null;
        String userRole = null;
        try {
            authenticatedUserId = getAuthenticatedUserId();
            com.classification.domain_system.entity.User u = userRepository.findById(authenticatedUserId.toString()).orElse(null);
            if (u != null) {
                userRole = u.getRole();
            }
        } catch (Exception e) {
            // Unauthenticated or guest context
        }

        com.classification.domain_system.entity.WorkflowConfig config = workflowId != null 
                ? approvalService.resolveWorkflowById(workflowId) 
                : approvalService.resolveWorkflow(nodeId, actionType);
        boolean hasWorkflow = config != null && config.getStepsConfig() != null && !config.getStepsConfig().isEmpty();

        List<String> editableFields = approvalService.extractEditableFields(config, authenticatedUserId, userRole);
        List<String> readOnlyFields = approvalService.extractReadOnlyFields(config, authenticatedUserId, userRole);
        List<String> hiddenFields = approvalService.extractHiddenFields(config, authenticatedUserId, userRole);
        com.fasterxml.jackson.databind.JsonNode ruleName = approvalService.extractRuleName(config, authenticatedUserId, userRole);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("actionType", actionType);
        result.put("hasWorkflow", hasWorkflow);
        result.put("workflowId", config != null ? config.getId() : null);
        result.put("workflowName", config != null ? config.getName() : null);
        result.put("ruleName", ruleName != null ? ruleName : java.util.Collections.emptyMap());
        result.put("editableFields", editableFields != null ? editableFields : java.util.Collections.emptyList());
        result.put("readOnlyFields", readOnlyFields != null ? readOnlyFields : java.util.Collections.emptyList());
        result.put("hiddenFields", hiddenFields != null ? hiddenFields : java.util.Collections.emptyList());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping
    public ResponseEntity<PageResponse<ApprovalRequest>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(PageResponse.of(approvalService.getPendingRequests(PageRequest.of(page, size))));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<ApprovalRequest>> getAllRequests(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String filterModel,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.unsorted();
        if (sort != null && !sort.trim().isEmpty()) {
            String[] parts = sort.split(",");
            String colId = parts[0].trim();
            org.springframework.data.domain.Sort.Direction dir = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()) 
                    ? org.springframework.data.domain.Sort.Direction.DESC 
                    : org.springframework.data.domain.Sort.Direction.ASC;
            
            String property = colId;
            if ("classificationName".equals(colId)) {
                property = "classificationNode.name";
            } else if ("domainName".equals(colId)) {
                property = "classificationNode.domain.name";
            }
            
            if (java.util.List.of("createdAt", "status", "targetType", "requesterId", "classificationNode.name", "classificationNode.domain.name", "idAttribute", "nameAttribute", "summary").contains(property)) {
                sortObj = org.springframework.data.domain.Sort.by(dir, property);
            }
        }
        
        return ResponseEntity.ok(PageResponse.of(approvalService.getAllRequests(search, status, filterModel, PageRequest.of(page, size, sortObj))));
    }
    
    @GetMapping("/todos")
    public ResponseEntity<PageResponse<ApprovalStep>> getMyTodos(
            @RequestParam UUID assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(PageResponse.of(approvalService.getMyTodos(assigneeId, PageRequest.of(page, size))));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<PageResponse<ApprovalRequest>> getMyRequests(
            @RequestParam UUID requesterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(PageResponse.of(approvalService.getMyRequests(requesterId, PageRequest.of(page, size))));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApprovalRequest> getRequestById(@PathVariable UUID id) {
        return ResponseEntity.ok(approvalService.getRequestById(id));
    }
    
    @PostMapping("/steps/{stepId}/approve")
    public ResponseEntity<ApprovalRequest> approveStep(
            @PathVariable UUID stepId, 
            @RequestBody(required = false) Map<String, String> payload) {
        UUID approverId = getAuthenticatedUserId();
        String comment = payload != null ? payload.get("comment") : null;
        return ResponseEntity.ok(approvalService.approveStep(stepId, approverId, comment));
    }
    
    @PostMapping("/steps/{stepId}/reject")
    public ResponseEntity<ApprovalRequest> rejectStep(
            @PathVariable UUID stepId, 
            @RequestBody(required = false) Map<String, String> payload) {
        UUID approverId = getAuthenticatedUserId();
        String comment = payload != null ? payload.get("comment") : null;
        return ResponseEntity.ok(approvalService.rejectStep(stepId, approverId, comment));
    }
    
    @PostMapping("/steps/{stepId}/admin-approve")
    public ResponseEntity<ApprovalRequest> adminApproveStep(
            @PathVariable UUID stepId, 
            @RequestBody(required = false) Map<String, String> payload) {
        UUID adminId = getAuthenticatedUserId();
        String comment = payload != null ? payload.get("comment") : null;
        return ResponseEntity.ok(approvalService.adminApproveStep(stepId, adminId, comment));
    }
    
    @PostMapping("/steps/{stepId}/admin-reject")
    public ResponseEntity<ApprovalRequest> adminRejectStep(
            @PathVariable UUID stepId, 
            @RequestBody(required = false) Map<String, String> payload) {
        UUID adminId = getAuthenticatedUserId();
        String comment = payload != null ? payload.get("comment") : null;
        return ResponseEntity.ok(approvalService.adminRejectStep(stepId, adminId, comment));
    }
}
