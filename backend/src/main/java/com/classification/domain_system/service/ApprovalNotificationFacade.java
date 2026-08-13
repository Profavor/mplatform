package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.ApprovalStep;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.event.ApprovalRequestCreatedEvent;
import com.classification.domain_system.event.ApprovalStepApprovedEvent;
import com.classification.domain_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApprovalNotificationFacade {

    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public void publishApprovalRequestCreated(ApprovalRequest approval) {
        if (approval != null) {
            eventPublisher.publishEvent(new ApprovalRequestCreatedEvent(approval));
        }
    }

    public void publishApprovalStepApproved(ApprovalRequest approval, ApprovalStep step) {
        if (approval != null && step != null) {
            eventPublisher.publishEvent(new ApprovalStepApprovedEvent(approval, step));
        }
    }

    public void processStepApprovalNotifications(ApprovalRequest approval, String approverId, String approverName) {
        if (notificationService != null && approval != null && approverName != null) {
            try {
                notificationService.updateApprovalNotificationsToProcessed(approval.getId(), approverName, "APPROVED");
            } catch (Exception ex) {
                log.warn("Failed to update notification status", ex);
            }
        }
    }

    public void processStepRejectionNotifications(ApprovalRequest approval, String approverId, String approverName) {
        if (notificationService != null && approval != null && approverName != null) {
            try {
                notificationService.updateApprovalNotificationsToProcessed(approval.getId(), approverName, "REJECTED");
            } catch (Exception ex) {
                log.warn("Failed to update notification status", ex);
            }
        }
    }

    public void sendRejectionNotification(ApprovalRequest approval, String rejecterId, String comment) {
        if (approval == null || notificationService == null || approval.getRequesterId() == null) return;
        try {
            String rejecterName = userRepository.findById(rejecterId)
                    .map(User::getUsername)
                    .orElse(rejecterId);
            String actionLabel = resolveActionLabel(approval.getTargetType());
            
            String msg = rejecterName + "님이 " + actionLabel + " 요청을 반려하였습니다.";
            if (comment != null && !comment.isBlank()) {
                msg += " (반려 사유: " + comment + ")";
            }

            notificationService.createNotification(
                    approval.getRequesterId(),
                    "@i18n:notifications.approval_rejected",
                    msg,
                    "APPROVAL",
                    "/approvals?requestId=" + approval.getId()
            );
        } catch (Exception ex) {
            log.warn("Failed to send rejection notification for request {}", approval.getId(), ex);
        }
    }

    public void sendFinalApprovalNotification(ApprovalRequest approval) {
        if (approval == null || notificationService == null || approval.getRequesterId() == null) return;
        try {
            String actionLabel = resolveActionLabel(approval.getTargetType());
            String msg = actionLabel + " 요청이 최종 승인되었습니다.";

            notificationService.createNotification(
                    approval.getRequesterId(),
                    "@i18n:notifications.approval_completed",
                    msg,
                    "APPROVAL",
                    "/approvals?requestId=" + approval.getId()
            );
        } catch (Exception ex) {
            log.warn("Failed to send final approval notification for request {}", approval.getId(), ex);
        }
    }
    
    private String resolveActionLabel(String targetType) {
        if (targetType == null) return "결재";
        return switch (targetType) {
            case "RECORD" -> "데이터 등록";
            case "RECORD_UPDATE" -> "데이터 수정";
            case "RECORD_DELETE" -> "데이터 삭제";
            case "RECORD_MERGE" -> "데이터 병합";
            case "SCHEMA_CHANGE" -> "스키마 변경";
            case "DOMAIN_ACCESS" -> "도메인 권한";
            case "DATA_EXPORT" -> "데이터 반출";
            default -> "결재";
        };
    }
}
