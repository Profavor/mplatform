package com.classification.domain_system.service;

import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.entity.Notification;
import com.classification.domain_system.entity.User;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.repository.NotificationRepository;
import com.classification.domain_system.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseNotificationService sseNotificationService;
    private final com.classification.domain_system.websocket.WebSocketPublisher webSocketPublisher;
    private final ApprovalRequestRepository approvalRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(String userId, String title, String message, String type, String linkUrl) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setLinkUrl(linkUrl);
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        if (sseNotificationService != null) {
            try {
                sseNotificationService.sendNotification(userId, saved);
                User targetUser = userRepository.findById(userId).orElse(null);
                if (targetUser != null && targetUser.getUsername() != null && !targetUser.getUsername().equals(userId)) {
                    sseNotificationService.sendNotification(targetUser.getUsername(), saved);
                }
            } catch (Exception e) {
                log.error("Failed to push SSE notification for user {}", userId, e);
            }
        }

        if (webSocketPublisher != null) {
            try {
                webSocketPublisher.publishNotification(userId, saved);
                User targetUser = userRepository.findById(userId).orElse(null);
                if (targetUser != null && targetUser.getUsername() != null && !targetUser.getUsername().equals(userId)) {
                    webSocketPublisher.publishNotification(targetUser.getUsername(), saved);
                }
            } catch (Exception e) {
                log.error("Failed to push WebSocket notification for user {}", userId, e);
            }
        }

        return saved;
    }

    @Transactional
    public Notification markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with ID: " + id));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(String userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void deleteNotification(UUID id) {
        notificationRepository.deleteById(id);
    }

    @Transactional
    public int deleteAllUserNotifications(String userId) {
        return notificationRepository.deleteByUserId(userId);
    }

    @Transactional
    public void markApprovalNotificationsAsRead(UUID approvalRequestId) {
        if (approvalRequestId == null) return;
        try {
            List<Notification> pendingNotifs = notificationRepository.findByLinkUrlContainingAndIsReadFalse(approvalRequestId.toString());
            for (Notification n : pendingNotifs) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        } catch (Exception e) {
            log.warn("Failed to mark approval notifications as read for request {}", approvalRequestId, e);
        }
    }

    @Transactional
    public void updateApprovalNotificationsToProcessed(UUID approvalId, String approverName, String actionType) {
        if (approvalId == null) return;
        try {
            String linkUrlPart = "requestId=" + approvalId;
            List<Notification> list = notificationRepository.findByLinkUrlContaining(linkUrlPart);
            String actionLabel = "REJECTED".equalsIgnoreCase(actionType) ? "반려" : "승인";
            String statusPrefix = "[처리 완료] ";
            String statusText = (approverName != null && !approverName.isBlank() ? approverName : "담당자") + "님에 의해 " + actionLabel + " 처리되었습니다.";

            for (Notification n : list) {
                String origMsg = n.getMessage() != null ? n.getMessage() : "";
                if (!origMsg.startsWith(statusPrefix)) {
                    n.setMessage(statusPrefix + statusText + " (" + origMsg + ")");
                    notificationRepository.save(n);
                    if (webSocketPublisher != null) {
                        try {
                            webSocketPublisher.publishNotification(n.getUserId(), n);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update approval notifications to processed for request: {}", approvalId, e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        page.getContent().forEach(this::enrichNotification);
        return page;
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private void enrichNotification(Notification notification) {
        if (notification == null) return;
        String message = notification.getMessage();
        String linkUrl = notification.getLinkUrl();

        if (message != null && (message.contains("received:") || message.contains("completed for request") || message.contains("fully approved") || (linkUrl != null && linkUrl.startsWith("/approvals/")))) {
            UUID approvalId = null;
            if (linkUrl != null && linkUrl.startsWith("/approvals/")) {
                try {
                    approvalId = UUID.fromString(linkUrl.substring("/approvals/".length()));
                } catch (Exception ignored) {}
            }
            if (approvalId == null && message.contains(":")) {
                String[] parts = message.split(":");
                if (parts.length > 1) {
                    try {
                        approvalId = UUID.fromString(parts[1].trim());
                    } catch (Exception ignored) {}
                }
            }

            if (approvalId != null) {
                final UUID targetApprovalId = approvalId;
                approvalRepository.findById(targetApprovalId).ifPresent(approval -> {
                    String actionLabel = resolveActionLabel(approval.getTargetType());
                    String requesterName = resolveUserName(approval.getRequesterId());
                    String domainName = resolveDomainName(approval);
                    String classificationName = resolveClassificationName(approval);
                    String summary = extractChangeSummary(approval);

                    notification.setTitle("@i18n:notifications.approval_pending");
                    notification.setMessage(buildNotificationMessage(actionLabel, requesterName, domainName, classificationName, summary));
                    notification.setLinkUrl("/approvals?requestId=" + targetApprovalId);
                });
            }
        }
    }

    private String resolveActionLabel(String targetType) {
        if (targetType == null) return "결재";
        switch (targetType) {
            case "RECORD": return "신규 등록";
            case "RECORD_UPDATE": return "정보 변경";
            case "RECORD_DELETE": return "삭제/폐기";
            default:
                if (targetType.startsWith("SCHEMA_")) return "스키마 변경";
                return "결재";
        }
    }

    private String resolveUserName(String userId) {
        if (userId == null) return "사용자";
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("사용자");
    }

    private String resolveDomainName(ApprovalRequest approval) {
        if (approval.getClassificationNode() != null && approval.getClassificationNode().getDomain() != null) {
            Map<String, String> nameMap = approval.getClassificationNode().getDomain().getName();
            if (nameMap != null && nameMap.containsKey("ko")) return nameMap.get("ko");
            if (nameMap != null && !nameMap.isEmpty()) return nameMap.values().iterator().next();
        }
        return "도메인";
    }

    private String resolveClassificationName(ApprovalRequest approval) {
        if (approval.getClassificationNode() != null) {
            Map<String, String> nameMap = approval.getClassificationNode().getName();
            if (nameMap != null && nameMap.containsKey("ko")) return nameMap.get("ko");
            if (nameMap != null && !nameMap.isEmpty()) return nameMap.values().iterator().next();
        }
        return "분류";
    }

    private String extractChangeSummary(ApprovalRequest approval) {
        if (approval.getChanges() == null || approval.getChanges().isBlank()) return "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(approval.getChanges());
            List<String> summaryParts = new ArrayList<>();

            if (root.has("before") && root.has("after")) {
                JsonNode beforeNode = root.get("before");
                JsonNode afterNode = root.get("after");
                afterNode.fieldNames().forEachRemaining(key -> {
                    if (summaryParts.size() < 3 && !key.startsWith("_") && !key.equals("id") && !key.equals("version")) {
                        String bVal = beforeNode.has(key) && beforeNode.get(key).isValueNode() ? beforeNode.get(key).asText() : "";
                        String aVal = afterNode.has(key) && afterNode.get(key).isValueNode() ? afterNode.get(key).asText() : "";
                        if (!bVal.equals(aVal)) {
                            summaryParts.add(key + ": " + (bVal.isBlank() ? "없음" : bVal) + " ➔ " + (aVal.isBlank() ? "없음" : aVal));
                        }
                    }
                });
            } else {
                JsonNode dataNode = root;
                if (dataNode.has("after")) dataNode = dataNode.get("after");
                if (dataNode.has("request")) dataNode = dataNode.get("request");
                
                final JsonNode targetNode = dataNode;
                List<String> priorityKeys = List.of("name", "code", "title", "empNo", "userName", "idAttr");
                for (String pKey : priorityKeys) {
                    if (targetNode.has(pKey) && targetNode.get(pKey).isValueNode()) {
                        summaryParts.add(pKey + ": " + targetNode.get(pKey).asText());
                    }
                }

                targetNode.fieldNames().forEachRemaining(key -> {
                    if (summaryParts.size() < 3 && !priorityKeys.contains(key) && !key.startsWith("_") && !key.equals("id") && !key.equals("version")) {
                        JsonNode val = targetNode.get(key);
                        if (val.isValueNode()) {
                            summaryParts.add(key + ": " + val.asText());
                        }
                    }
                });
            }
            return String.join(", ", summaryParts);
        } catch (Exception e) {
            return "";
        }
    }

    private String buildNotificationMessage(String actionLabel, String requesterName, String domainName, String classificationName, String summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(domainName).append(" > ").append(classificationName).append("] ");
        sb.append(requesterName).append("님의 ").append(actionLabel).append(" 요청");
        if (!summary.isBlank()) {
            sb.append(" (").append(summary).append(")");
        }
        return sb.toString();
    }
}
