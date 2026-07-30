package com.classification.domain_system.service;

import com.classification.domain_system.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SseNotificationServiceTest {

    private SseNotificationService sseNotificationService;
    private String userId;

    @BeforeEach
    void setUp() {
        sseNotificationService = new SseNotificationService();
        userId = UUID.randomUUID().toString();
    }

    @Test
    @DisplayName("testSubscribe: Returns SseEmitter for user ID")
    void testSubscribe() {
        SseEmitter emitter = sseNotificationService.subscribe(userId);

        assertThat(emitter).isNotNull();
        assertThat(emitter.getTimeout()).isEqualTo(1800000L); // 30 mins
    }

    @Test
    @DisplayName("testSendNotification: Emits event to active subscriber")
    void testSendNotification() {
        SseEmitter emitter = sseNotificationService.subscribe(userId);
        assertThat(emitter).isNotNull();

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(userId);
        notification.setTitle("Test Event");
        notification.setMessage("Test content");

        assertDoesNotThrow(() -> sseNotificationService.sendNotification(userId, notification));
    }
}
