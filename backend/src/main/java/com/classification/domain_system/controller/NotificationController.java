package com.classification.domain_system.controller;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.dto.PageResponse;
import com.classification.domain_system.entity.Notification;
import com.classification.domain_system.service.NotificationService;
import com.classification.domain_system.service.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseNotificationService sseNotificationService;
    private final AuthContext authContext;

    private UUID getCurrentUserId(UUID paramUserId) {
        if (paramUserId != null) {
            return paramUserId;
        }
        if (authContext != null && authContext.getUserId() != null) {
            try {
                return UUID.fromString(authContext.getUserId());
            } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("User ID is required");
    }

    @GetMapping
    public ResponseEntity<PageResponse<Notification>> getNotifications(
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID currentUserId = getCurrentUserId(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(currentUserId, pageable);
        return ResponseEntity.ok(PageResponse.of(notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@RequestParam(required = false) UUID userId) {
        UUID currentUserId = getCurrentUserId(userId);
        int count = notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(Map.of("updatedCount", count));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam(required = false) UUID userId) {
        UUID currentUserId = getCurrentUserId(userId);
        long count = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Integer>> deleteAllNotifications(@RequestParam(required = false) UUID userId) {
        UUID currentUserId = getCurrentUserId(userId);
        int count = notificationService.deleteAllUserNotifications(currentUserId);
        return ResponseEntity.ok(Map.of("deletedCount", count));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Integer>> deleteAllNotificationsShortcut(@RequestParam(required = false) UUID userId) {
        return deleteAllNotifications(userId);
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam(required = false) UUID userId) {
        UUID currentUserId = getCurrentUserId(userId);
        return sseNotificationService.subscribe(currentUserId);
    }
}
