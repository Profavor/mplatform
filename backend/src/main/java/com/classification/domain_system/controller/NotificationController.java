package com.classification.domain_system.controller;

import com.classification.domain_system.dto.NotificationDto;
import com.classification.domain_system.service.NotificationService;
import com.classification.domain_system.service.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseNotificationService sseNotificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        return sseNotificationService.subscribe(userId);
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto.NotificationResponse>> getMyNotifications(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(notificationService.getMyNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Boolean>> markAsRead(
            @PathVariable UUID id,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        boolean updated = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(Map.of("success", updated));
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("updatedCount", count));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'notification:write')")
    public ResponseEntity<NotificationDto.NotificationResponse> createNotification(
            @RequestBody NotificationDto.NotificationCreateRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(request));
    }
}
