package com.classification.domain_system.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishNotification(String userId, Object notification) {
        if (userId == null || userId.isBlank()) return;
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
        } catch (Exception e) {
            log.warn("Failed to publish websocket notification to user {}", userId, e);
        }
    }

    public void publishApprovalEvent(String destination, Object payload) {
        if (destination == null || destination.isBlank()) return;
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (Exception e) {
            log.warn("Failed to publish approval event to {}", destination, e);
        }
    }

    public void publishToRoom(java.util.UUID roomId, Object payload) {
        if (roomId == null) return;
        try {
            messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, payload);
        } catch (Exception e) {
            log.warn("Failed to publish chat message to room {}", roomId, e);
        }
    }
}
