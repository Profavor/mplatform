package com.classification.domain_system.service;

import com.classification.domain_system.dto.NotificationDto;
import com.classification.domain_system.entity.Notification;
import com.classification.domain_system.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto.NotificationResponse> getMyNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public boolean markAsRead(UUID id, String userId) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n != null && n.getUserId().equals(userId)) {
            n.setRead(true);
            notificationRepository.save(n);
            return true;
        }
        return false;
    }

    @Transactional
    public int markAllAsRead(String userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public NotificationDto.NotificationResponse createNotification(NotificationDto.NotificationCreateRequest request) {
        if (request == null || request.getUserId() == null) {
            return null;
        }
        return createNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getLinkUrl()
        );
    }

    @Transactional
    public NotificationDto.NotificationResponse createNotification(String userId, String title, String message, String type, String linkUrl) {
        if (userId == null) {
            log.warn("Notification skipped: userId is null");
            return null;
        }
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title != null ? title : "알림")
                .message(message != null ? message : "")
                .type(type != null ? type : "SYSTEM")
                .linkUrl(linkUrl)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional
    public void updateApprovalNotificationsToProcessed(UUID approvalRequestId, String approverName, String status) {
        log.info("Approval notification processed for request {}: approver={}, status={}", approvalRequestId, approverName, status);
    }

    private NotificationDto.NotificationResponse toResponse(Notification n) {
        if (n == null) return null;
        return NotificationDto.NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .linkUrl(n.getLinkUrl())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
