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

    /**
     * 실시간 SSE(Server-Sent Events) 알림 스트림 구독
     * 표준 브라우저 EventSource API는 커스텀 Authorization 헤더를 지원하지 않으므로,
     * SecurityConfig 필터체인에서 permitAll로 허용하고 전달된 인증(쿠키/파라미터/헤더)이 있을 경우 해당 사용자로,
     * 없을 경우 anonymous로 안전하게 구독을 연결합니다.
     */
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

    @RequestMapping(value = "/{id}/read", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Map<String, Boolean>> markAsRead(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        try {
            UUID uuid = UUID.fromString(id);
            boolean updated = notificationService.markAsRead(uuid, userId);
            return ResponseEntity.ok(Map.of("success", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", true));
        }
    }

    @RequestMapping(value = {"/mark-all-read", "/read-all"}, method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Map<String, Integer>> markAllAsRead(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("updatedCount", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteNotification(
            @PathVariable String id,
            Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        try {
            UUID uuid = UUID.fromString(id);
            boolean deleted = notificationService.deleteNotification(uuid, userId);
            return ResponseEntity.ok(Map.of("success", deleted));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of("success", true));
        }
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Integer>> clearAllNotifications(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "anonymous";
        int count = notificationService.clearAllNotifications(userId);
        return ResponseEntity.ok(Map.of("deletedCount", count));
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'notification:write')")
    public ResponseEntity<NotificationDto.NotificationResponse> createNotification(
            @RequestBody NotificationDto.NotificationCreateRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(request));
    }
}
