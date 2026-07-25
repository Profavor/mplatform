package com.classification.domain_system.service;

import com.classification.domain_system.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseNotificationService {

    private static final long TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutes
    private final Map<UUID, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);

        emitterMap.put(userId, emitter);

        emitter.onCompletion(() -> emitterMap.remove(userId, emitter));
        emitter.onTimeout(() -> emitterMap.remove(userId, emitter));
        emitter.onError(e -> emitterMap.remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to Notification Stream for user " + userId));
        } catch (Exception e) {
            log.error("Error sending initial SSE connect event to user {}", userId, e);
            emitterMap.remove(userId, emitter);
        }

        return emitter;
    }

    public void sendNotification(UUID userId, Notification notification) {
        SseEmitter emitter = emitterMap.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
                log.info("Sent SSE notification to user {}: {}", userId, notification.getTitle());
            } catch (Exception e) {
                log.warn("Failed to send SSE notification to user {}, removing emitter: {}", userId, e.getMessage());
                emitterMap.remove(userId);
            }
        }
    }
}
