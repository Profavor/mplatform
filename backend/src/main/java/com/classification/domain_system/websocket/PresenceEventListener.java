package com.classification.domain_system.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    
    // username -> Set<sessionId>
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    public Set<String> getOnlineUsers() {
        return Collections.unmodifiableSet(userSessions.keySet());
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
                    String sessionId = extractSessionId(event);
                    Set<String> sessions = userSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet());
                    boolean wasEmpty = sessions.isEmpty();
                    if (sessionId != null) {
                        sessions.add(sessionId);
                    } else {
                        sessions.add("default");
                    }
                    
                    if (wasEmpty) {
                        log.info("[STOMP] User Connected: {}", username);
                        broadcastPresence(userId, username, "ONLINE");
                    }
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
                    String sessionId = event.getSessionId();
                    if (sessionId == null) {
                        sessionId = extractSessionId(event);
                    }
                    
                    Set<String> sessions = userSessions.get(username);
                    if (sessions != null) {
                        if (sessionId != null) {
                            sessions.remove(sessionId);
                        }
                        if (sessions.isEmpty() || sessionId == null) {
                            userSessions.remove(username);
                            log.info("[STOMP] User Disconnected: {}", username);
                            broadcastPresence(userId, username, "OFFLINE");
                        }
                    }
                }
            }
        }
    }

    private String extractSessionId(org.springframework.web.socket.messaging.AbstractSubProtocolEvent event) {
        if (event.getMessage() != null && event.getMessage().getHeaders() != null) {
            return SimpMessageHeaderAccessor.getSessionId(event.getMessage().getHeaders());
        }
        return null;
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
