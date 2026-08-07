package com.classification.domain_system.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    
    private final java.util.Set<String> onlineUsers = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public java.util.Set<String> getOnlineUsers() {
        return onlineUsers;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        if (event.getUser() instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            if (auth.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> details = (Map<String, String>) auth.getDetails();
                String username = details.get("username");
                String userId = details.get("userId");
                
                if (username != null) {
                    onlineUsers.add(username);
                    log.info("[STOMP] User Connected: {}", username);
                    broadcastPresence(userId, username, "ONLINE");
                }
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            if (auth.getDetails() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> details = (Map<String, String>) auth.getDetails();
                String username = details.get("username");
                String userId = details.get("userId");
                
                if (username != null) {
                    onlineUsers.remove(username);
                    log.info("[STOMP] User Disconnected: {}", username);
                    broadcastPresence(userId, username, "OFFLINE");
                }
            }
        }
    }

    private void broadcastPresence(String userId, String username, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "PRESENCE_UPDATE");
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("status", status);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/chat/presence", (Object) payload);
    }
}
