package com.classification.domain_system.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketPublisher webSocketPublisher;

    @Test
    @DisplayName("성공 - publishNotification 호출 시 해당 사용자 주제로 메세지가 전송된다")
    void publishNotification_Success() {
        String userId = "user-123";
        Map<String, String> payload = Map.of("message", "test");

        webSocketPublisher.publishNotification(userId, payload);

        verify(messagingTemplate).convertAndSend(eq("/topic/notifications/user-123"), eq(payload));
    }

    @Test
    @DisplayName("성공 - publishApprovalEvent 호출 시 목적지로 메세지가 전송된다")
    void publishApprovalEvent_Success() {
        String destination = "/topic/approvals/app-1";
        Map<String, String> payload = Map.of("eventType", "APPROVED");

        webSocketPublisher.publishApprovalEvent(destination, payload);

        verify(messagingTemplate).convertAndSend(eq(destination), eq(payload));
    }
}
