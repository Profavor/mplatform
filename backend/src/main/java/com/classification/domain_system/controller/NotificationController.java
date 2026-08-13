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

import com.classification.domain_system.security.JwtUtil;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseNotificationService sseNotificationService;
    private final AuthContext authContext;
    private final JwtUtil jwtUtil;
    private final com.classification.domain_system.security.SecurityUtils securityUtils;

    private String getCurrentUserId(String paramUserId) {
        String authUserId = securityUtils.getCurrentUserId();
        if (paramUserId != null && !paramUserId.isBlank()) {
            if (authUserId != null && !paramUserId.equals(authUserId)) {
                // To prevent IDOR, if a user requests data for another user, deny access.
                // Alternatively, we could check for admin role here, but throwing AccessDeniedException is safer.
                throw new org.springframework.security.access.AccessDeniedException("User does not have permission to access notifications for another user");
            }
            return paramUserId;
        }
        if (authUserId != null) {
            return authUserId;
        }
        throw new IllegalArgumentException("User ID is required");
    }

    @GetMapping
    public ResponseEntity<PageResponse<Notification>> getNotifications(
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String currentUserId = getCurrentUserId(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUserNotifications(currentUserId, pageable);
        return ResponseEntity.ok(PageResponse.of(notifications));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID id, @RequestParam(required = false) String userId) {
        String currentUserId = getCurrentUserId(userId);
        return ResponseEntity.ok(notificationService.markAsRead(id, currentUserId));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@RequestParam(required = false) String userId) {
        String currentUserId = getCurrentUserId(userId);
        int count = notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(Map.of("updatedCount", count));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam(required = false) String userId) {
        String currentUserId = getCurrentUserId(userId);
        long count = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id, @RequestParam(required = false) String userId) {
        String currentUserId = getCurrentUserId(userId);
        notificationService.deleteNotification(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Integer>> deleteAllNotifications(@RequestParam(required = false) String userId) {
        String currentUserId = getCurrentUserId(userId);
        int count = notificationService.deleteAllUserNotifications(currentUserId);
        return ResponseEntity.ok(Map.of("deletedCount", count));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Integer>> deleteAllNotificationsShortcut(@RequestParam(required = false) String userId) {
        return deleteAllNotifications(userId);
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam(required = false) String userId, @RequestParam(required = false) String token) {
        String currentUserId = userId;
        String currentUsername = null;

        if (token != null && !token.isBlank() && jwtUtil != null && jwtUtil.isTokenValid(token)) {
            try {
                Claims claims = jwtUtil.extractAllClaims(token);
                if (currentUserId == null || currentUserId.isBlank()) {
                    currentUserId = (String) claims.get("userId");
                    if (currentUserId == null || currentUserId.isBlank()) {
                        currentUserId = (String) claims.get("uuid");
                    }
                }
                currentUsername = claims.getSubject();
            } catch (Exception ignored) {}
        }

        if (currentUserId == null || currentUserId.isBlank()) {
            try {
                currentUserId = getCurrentUserId(userId);
            } catch (Exception e) {
                if (currentUsername != null) {
                    currentUserId = currentUsername;
                } else {
                    throw e;
                }
            }
        }

        if (currentUsername != null && !currentUsername.equals(currentUserId)) {
            sseNotificationService.subscribe(currentUsername);
        }

        return sseNotificationService.subscribe(currentUserId);
    }
}
