package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.enums.ApprovalStatus;
import com.classification.domain_system.entity.enums.ApprovalTargetType;
import com.classification.domain_system.entity.enums.RecordStatus;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.CustomAccessDeniedException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.classification.domain_system.repository.*;
import com.classification.domain_system.websocket.WebSocketPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalDecisionService {

    private final ApprovalRequestRepository approvalRepository;
    private final ApprovalStepRepository stepRepository;
    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final ApprovalNotificationFacade notificationFacade;
    @org.springframework.context.annotation.Lazy
    private final ApprovalDelegationService delegationService;
    private final WebSocketPublisher webSocketPublisher;
    private final BatchJobRepository batchJobRepository;
    private final StagingRecordRepository stagingRecordRepository;

    public boolean isStepAssigneeOrRoleMatch(ApprovalStep step, String approverId) {
        if (step == null || approverId == null) return false;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null) {
            boolean hasAdminPerm = auth.getAuthorities().stream()
                    .anyMatch(a -> {
                        String authStr = a.getAuthority();
                        return "admin:write".equalsIgnoreCase(authStr) || "*".equals(authStr) || "*:*".equalsIgnoreCase(authStr);
                    });
            if (hasAdminPerm) {
                return true;
            }
        }

        if (approverId.equals(step.getAssigneeId())) {
            return true;
        }

        if (step.getAssigneeRole() != null && !step.getAssigneeRole().isBlank()) {
            User user = userRepository.findById(approverId).orElse(null);
            if (user != null && step.getAssigneeRole().equalsIgnoreCase(user.getRole())) {
                return true;
            }
        }

        if (step.getAssigneeId() != null && delegationService != null) {
            if (delegationService.isDelegatedApprover(approverId, step.getAssigneeId())) {
                return true;
            }
        }
        return false;
    }

    public void revertRecordStatusOnRejection(ApprovalRequest approval) {
        if (ApprovalTargetType.RECORD.name().equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
            record.setStatus(ApprovalStatus.REJECTED.name());
            recordRepository.saveAndFlush(record);
        } else if (ApprovalTargetType.RECORD_UPDATE.name().equals(approval.getTargetType()) || ApprovalTargetType.RECORD_DELETE.name().equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found"));
            record.setStatus(RecordStatus.ACTIVE.name());
            recordRepository.saveAndFlush(record);
        } else if (ApprovalTargetType.BATCH_RECORD.name().equals(approval.getTargetType())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode changesNode = mapper.readTree(approval.getChanges());
                List<UUID> recordIds = new ArrayList<>();
                if (changesNode.has("recordIds") && changesNode.get("recordIds").isArray()) {
                    for (JsonNode id : changesNode.get("recordIds")) {
                        recordIds.add(UUID.fromString(id.asText()));
                    }
                }
                List<Record> records = recordRepository.findAllById(recordIds);
                for (Record record : records) {
                    record.setStatus(ApprovalStatus.REJECTED.name());
                }
                recordRepository.saveAllAndFlush(records);
                
                UUID batchId = UUID.fromString(changesNode.get("batchId").asText());
                batchJobRepository.findById(batchId).ifPresent(job -> {
                    job.setStatus("FAILED");
                    batchJobRepository.save(job);
                });
                
                List<com.classification.domain_system.entity.StagingRecord> stagings = stagingRecordRepository.findByBatchId(batchId);
                for (com.classification.domain_system.entity.StagingRecord sr : stagings) {
                    if ("PENDING_APPROVAL".equals(sr.getStatus())) {
                        sr.setStatus("ERROR");
                        sr.setErrorMessage("Approval rejected");
                    }
                }
                stagingRecordRepository.saveAll(stagings);
            } catch (Exception e) {
                log.error("Error applying rejection for BATCH_RECORD", e);
            }
        } else if (approval.getTargetType() != null && approval.getTargetType().startsWith("SCHEMA_")) {
            log.info("Schema change request {} was rejected, no record status to revert", approval.getId());
        }
    }

    public void revertRecordStatusOnCancellation(ApprovalRequest approval) {
        if (ApprovalTargetType.RECORD.name().equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId()).orElse(null);
            if (record != null) {
                record.setStatus(ApprovalStatus.CANCELLED.name());
                recordRepository.saveAndFlush(record);
            }
        } else if (ApprovalTargetType.RECORD_UPDATE.name().equals(approval.getTargetType()) || ApprovalTargetType.RECORD_DELETE.name().equals(approval.getTargetType())) {
            Record record = recordRepository.findById(approval.getTargetId()).orElse(null);
            if (record != null) {
                record.setStatus(RecordStatus.ACTIVE.name());
                recordRepository.saveAndFlush(record);
            }
        } else if (ApprovalTargetType.BATCH_RECORD.name().equals(approval.getTargetType())) {
            revertRecordStatusOnRejection(approval);
        }
    }

    public void broadcastRejectionEvent(ApprovalRequest approval) {
        if (webSocketPublisher != null) {
            Map<String, Object> payload = Map.of(
                    "eventType", ApprovalStatus.REJECTED.name(),
                    "approvalId", approval.getId(),
                    "status", ApprovalStatus.REJECTED.name(),
                    "targetType", approval.getTargetType() != null ? approval.getTargetType() : "",
                    "targetId", approval.getTargetId() != null ? approval.getTargetId() : ""
            );
            webSocketPublisher.publishApprovalEvent("/topic/approvals/" + approval.getId(), payload);
            webSocketPublisher.publishApprovalEvent("/topic/approvals/status-changes", payload);
        }
    }

    public void broadcastCancellationEvent(ApprovalRequest approval) {
        if (webSocketPublisher != null) {
            Map<String, Object> payload = Map.of(
                    "eventType", ApprovalStatus.CANCELLED.name(),
                    "approvalId", approval.getId(),
                    "status", ApprovalStatus.CANCELLED.name(),
                    "targetType", approval.getTargetType() != null ? approval.getTargetType() : "",
                    "targetId", approval.getTargetId() != null ? approval.getTargetId() : ""
            );
            webSocketPublisher.publishApprovalEvent("/topic/approvals/" + approval.getId(), payload);
            webSocketPublisher.publishApprovalEvent("/topic/approvals/status-changes", payload);
        }
    }

    @Transactional
    public ApprovalRequest approveStep(UUID stepId, String approverId, String comment) {
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!isStepAssigneeOrRoleMatch(step, approverId)) {
            throw new CustomAccessDeniedException(ErrorCode.NOT_STEP_ASSIGNEE, "You are not the assignee for this step");
        }
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        boolean isProxy = step.getAssigneeId() != null && !approverId.equals(step.getAssigneeId()) && delegationService != null && delegationService.isDelegatedApprover(approverId, step.getAssigneeId());
        String finalComment = (isProxy ? "[대결] " : "") + (comment != null ? comment : "");

        step.setStatus(ApprovalStatus.APPROVED.name());
        step.setComment(finalComment);
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        if (notificationFacade != null) {
            try {
                String approverName = userRepository.findById(approverId).map(User::getUsername).orElse(approverId);
                notificationFacade.processStepApprovalNotifications(approval, null, approverName);
            } catch (Exception ignored) {}
        }
        notificationFacade.publishApprovalStepApproved(approval, step);
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest rejectStep(UUID stepId, String approverId, String comment) {
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!isStepAssigneeOrRoleMatch(step, approverId)) {
            throw new CustomAccessDeniedException(ErrorCode.NOT_STEP_ASSIGNEE, "You are not the assignee for this step");
        }
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }

        boolean isProxy = step.getAssigneeId() != null && !approverId.equals(step.getAssigneeId()) && delegationService != null && delegationService.isDelegatedApprover(approverId, step.getAssigneeId());
        String finalComment = (isProxy ? "[대결] " : "") + (comment != null ? comment : "");
        
        step.setStatus(ApprovalStatus.REJECTED.name());
        step.setComment(finalComment);
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        approval.setStatus(ApprovalStatus.REJECTED.name());
        approvalRepository.saveAndFlush(approval);
        
        if (notificationFacade != null) {
            try {
                String approverName = userRepository.findById(approverId).map(User::getUsername).orElse(approverId);
                notificationFacade.processStepRejectionNotifications(approval, null, approverName);
            } catch (Exception ignored) {}
        }
        
        revertRecordStatusOnRejection(approval);
        notificationFacade.sendRejectionNotification(approval, approverId, comment);
        broadcastRejectionEvent(approval);
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest adminApproveStep(UUID stepId, String adminId, String comment) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
                
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus(ApprovalStatus.APPROVED.name());
        step.setComment((comment != null && !comment.trim().isEmpty() ? comment + " " : "") + "(Admin Proxy)");
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        notificationFacade.publishApprovalStepApproved(approval, step);
        
        return approval;
    }
    
    @Transactional
    public ApprovalRequest adminRejectStep(UUID stepId, String adminId, String comment) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
                
        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Step not found"));
                
        if (!ApprovalStatus.PENDING.name().equals(step.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Step is not pending");
        }
        
        step.setStatus(ApprovalStatus.REJECTED.name());
        step.setComment((comment != null && !comment.trim().isEmpty() ? comment + " " : "") + "(Admin Proxy)");
        stepRepository.saveAndFlush(step);
        
        ApprovalRequest approval = step.getApprovalRequest();
        approval.setStatus(ApprovalStatus.REJECTED.name());
        approvalRepository.saveAndFlush(approval);
        
        revertRecordStatusOnRejection(approval);
        notificationFacade.sendRejectionNotification(approval, adminId, comment);
        broadcastRejectionEvent(approval);
        
        return approval;
    }

    @Transactional
    public ApprovalRequest cancelApprovalRequest(UUID requestId, String userId, String reason) {
        ApprovalRequest approval = approvalRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));

        boolean isRequester = approval.getRequesterId() != null && approval.getRequesterId().equals(userId);
        if (!isRequester) {
            throw new CustomAccessDeniedException("Only the requester can cancel their own approval request.");
        }

        if (!ApprovalStatus.PENDING.name().equalsIgnoreCase(approval.getStatus())) {
            throw new BusinessException(ErrorCode.STEP_NOT_PENDING, "Only pending approval requests can be cancelled.");
        }

        approval.setStatus(ApprovalStatus.CANCELLED.name());
        if (StringUtils.hasText(reason)) {
            approval.setReason(reason);
        }

        List<ApprovalStep> steps = stepRepository.findByApprovalRequestIdOrderByStepOrderAsc(approval.getId());
        if (steps != null && !steps.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (ApprovalStep step : steps) {
                if (ApprovalStatus.PENDING.name().equals(step.getStatus()) || ApprovalStatus.WAITING.name().equals(step.getStatus())) {
                    step.setStatus(ApprovalStatus.CANCELLED.name());
                    step.setUpdatedAt(now);
                    if (StringUtils.hasText(reason)) {
                        step.setComment(reason);
                    }
                }
            }
            stepRepository.saveAllAndFlush(steps);
            approval.setSteps(steps);
        }

        approvalRepository.saveAndFlush(approval);
        revertRecordStatusOnCancellation(approval);

        if (notificationFacade != null) {
            try {
                String cancelerName = userRepository.findById(userId).map(User::getUsername).orElse(userId);
                notificationFacade.processCancellationNotifications(approval, cancelerName);
                notificationFacade.publishApprovalRequestCancelled(approval, reason);
            } catch (Exception ignored) {}
        }

        broadcastCancellationEvent(approval);

        return approval;
    }
}
