package com.classification.domain_system.service;

import com.classification.domain_system.dto.MemoApprovalRequest;
import com.classification.domain_system.dto.RecordRequest;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.WorkflowConfig;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * ApprovalFacade - 결재 관련 주요 서브 서비스들을 통합 위임하는 파사드 서비스.
 * 1. 권한/필드 검증: ApprovalFieldPermissionService
 * 2. 결재 요청 생성: ApprovalRequestCreationService
 * 3. 결재 승인/반려/취소: ApprovalDecisionService
 * 4. 결재 조회 및 마스킹: ApprovalQueryService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {

    private final ApprovalFieldPermissionService fieldPermissionService;
    private final ApprovalRequestCreationService creationService;
    private final ApprovalDecisionService decisionService;
    private final ApprovalQueryService queryService;

    // --- Query & Role ---
    public String resolveRoleDisplayName(String roleCode) {
        return queryService.resolveRoleDisplayName(roleCode);
    }

    // --- Field Permissions ---
    public List<String> extractEditableFields(WorkflowConfig config, String requesterId, String userRole) {
        return fieldPermissionService.extractEditableFields(config, requesterId, userRole);
    }

    public List<String> extractReadOnlyFields(WorkflowConfig config, String requesterId, String userRole) {
        return fieldPermissionService.extractReadOnlyFields(config, requesterId, userRole);
    }

    public List<String> extractHiddenFields(WorkflowConfig config, String requesterId, String userRole) {
        return fieldPermissionService.extractHiddenFields(config, requesterId, userRole);
    }

    public JsonNode extractRuleName(WorkflowConfig config, String requesterId, String userRole) {
        return fieldPermissionService.extractRuleName(config, requesterId, userRole);
    }

    public void validateUserActionPermission(WorkflowConfig config, String requesterId, String userRole, String requestedAction) {
        fieldPermissionService.validateUserActionPermission(config, requesterId, userRole, requestedAction);
    }

    // --- Request Creation ---
    @Transactional
    public ApprovalRequest requestRecordCreation(UUID nodeId, RecordRequest request) {
        return creationService.requestRecordCreation(nodeId, request);
    }

    @Transactional
    public ApprovalRequest requestBatchRecordCreation(UUID domainId, UUID batchId, List<UUID> recordIds, String requesterId) {
        return creationService.requestBatchRecordCreation(domainId, batchId, recordIds, requesterId);
    }

    @Transactional
    public ApprovalRequest requestRecordUpdate(UUID recordId, RecordRequest request) {
        return creationService.requestRecordUpdate(recordId, request);
    }

    @Transactional
    public ApprovalRequest requestRecordDeletion(UUID recordId, RecordRequest request) {
        return creationService.requestRecordDeletion(recordId, request);
    }

    @Transactional
    public ApprovalRequest requestRecordMerge(RecordMergeService.MergeRequest request, String requesterId) {
        return creationService.requestRecordMerge(request, requesterId);
    }

    @Transactional
    public ApprovalRequest requestMemoApproval(MemoApprovalRequest request, String requesterId) {
        return creationService.requestMemoApproval(request, requesterId);
    }

    // --- Decision (Approve / Reject / Cancel) ---
    @Transactional
    public ApprovalRequest approveStep(UUID stepId, String approverId, String comment) {
        return decisionService.approveStep(stepId, approverId, comment);
    }

    @Transactional
    public ApprovalRequest rejectStep(UUID stepId, String approverId, String comment) {
        return decisionService.rejectStep(stepId, approverId, comment);
    }

    @Transactional
    public ApprovalRequest adminApproveStep(UUID stepId, String adminId, String comment) {
        return decisionService.adminApproveStep(stepId, adminId, comment);
    }

    @Transactional
    public ApprovalRequest adminRejectStep(UUID stepId, String adminId, String comment) {
        return decisionService.adminRejectStep(stepId, adminId, comment);
    }

    @Transactional
    public ApprovalRequest cancelApprovalRequest(UUID requestId, String userId, String reason) {
        return decisionService.cancelApprovalRequest(requestId, userId, reason);
    }

    // --- Queries ---
    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getPendingRequests(Pageable pageable) {
        return queryService.getPendingRequests(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getAllRequests(String search, String status, String filterModel, Pageable pageable) {
        return queryService.getAllRequests(search, status, filterModel, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, Pageable pageable) {
        return queryService.getMyTodos(assigneeId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, String userRole, Pageable pageable) {
        return queryService.getMyTodos(assigneeId, userRole, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, Collection<String> userRoles, Pageable pageable) {
        return queryService.getMyTodos(assigneeId, userRoles, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalStep> getMyTodos(String assigneeId, String username, Collection<String> userRoles, Pageable pageable) {
        return queryService.getMyTodos(assigneeId, username, userRoles, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getMyRequests(String requesterId, Pageable pageable) {
        return queryService.getMyRequests(requesterId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> getMyRequests(String requesterId, String username, Pageable pageable) {
        return queryService.getMyRequests(requesterId, username, pageable);
    }

    public ApprovalRequest maskChangesForRead(ApprovalRequest approval) {
        return queryService.maskChangesForRead(approval);
    }

    @Transactional(readOnly = true)
    public ApprovalRequest getRequestById(UUID id) {
        return queryService.getRequestById(id);
    }
}
