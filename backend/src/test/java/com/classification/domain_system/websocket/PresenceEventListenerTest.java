package com.classification.domain_system.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceEventListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private PresenceEventListener presenceEventListener;

    @BeforeEach
    void setUp() {
        presenceEventListener = new PresenceEventListener(messagingTemplate);
    }

    private SessionConnectedEvent createConnectEvent(String username, String userId, String sessionId) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.CONNECT_ACK);
        accessor.setSessionId(sessionId);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null);
        Map<String, String> details = new HashMap<>();
        details.put("username", username);
        details.put("userId", userId);
        auth.setDetails(details);

        return new SessionConnectedEvent(this, message, auth);
    }

    private SessionDisconnectEvent createDisconnectEvent(String username, String userId, String sessionId) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(SimpMessageType.DISCONNECT_ACK);
        accessor.setSessionId(sessionId);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null);
        Map<String, String> details = new HashMap<>();
        details.put("username", username);
        details.put("userId", userId);
        auth.setDetails(details);

        return new SessionDisconnectEvent(this, message, sessionId, CloseStatus.NORMAL, auth);
    }

    @Test
    @DisplayName("최초 세션 연결 시 ONLINE 브로드캐스트가 전송되고 온라인 목록에 추가되어야 함")
    void testFirstSessionConnect() {
        SessionConnectedEvent event = createConnectEvent("user1", "uid-1", "sess-1");

        presenceEventListener.handleWebSocketConnectListener(event);

        Set<String> onlineUsers = presenceEventListener.getOnlineUsers();
        assertTrue(onlineUsers.contains("user1"));
        assertEquals(1, onlineUsers.size());

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat/presence"), payloadCaptor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertEquals("PRESENCE_UPDATE", payload.get("type"));
        assertEquals("ONLINE", payload.get("status"));
        assertEquals("user1", payload.get("username"));
        assertEquals("uid-1", payload.get("userId"));
    }

    @Test
    @DisplayName("동일 사용자가 다중 세션(탭) 연결 시 ONLINE 브로드캐스트는 최초 1회만 전송되어야 함")
    void testMultipleSessionsConnect() {
        SessionConnectedEvent event1 = createConnectEvent("user1", "uid-1", "sess-1");
        SessionConnectedEvent event2 = createConnectEvent("user1", "uid-1", "sess-2");

        presenceEventListener.handleWebSocketConnectListener(event1);
        presenceEventListener.handleWebSocketConnectListener(event2);

        Set<String> onlineUsers = presenceEventListener.getOnlineUsers();
        assertTrue(onlineUsers.contains("user1"));
        assertEquals(1, onlineUsers.size());

        // 최초 1회만 ONLINE 브로드캐스트 전송
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat/presence"), any(Object.class));
    }

    @Test
    @DisplayName("다중 세션 중 1개 세션이 끊겨도 다른 세션이 남아있으면 OFFLINE이 전송되지 않아야 함")
    void testDisconnectOneOfMultipleSessions() {
        SessionConnectedEvent event1 = createConnectEvent("user1", "uid-1", "sess-1");
        SessionConnectedEvent event2 = createConnectEvent("user1", "uid-1", "sess-2");
        presenceEventListener.handleWebSocketConnectListener(event1);
        presenceEventListener.handleWebSocketConnectListener(event2);

        SessionDisconnectEvent disconnect1 = createDisconnectEvent("user1", "uid-1", "sess-1");
        presenceEventListener.handleWebSocketDisconnectListener(disconnect1);

        Set<String> onlineUsers = presenceEventListener.getOnlineUsers();
        assertTrue(onlineUsers.contains("user1"));

        // 아직 ONLINE 브로드캐스트 1회만 호출되었고, OFFLINE은 호출되지 않아야 함
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/chat/presence"), any(Object.class));
    }

    @Test
    @DisplayName("사용자의 모든 세션이 끊겼을 때 비로소 OFFLINE 브로드캐스트가 전송되고 온라인 목록에서 제거되어야 함")
    void testDisconnectAllSessions() {
        SessionConnectedEvent event1 = createConnectEvent("user1", "uid-1", "sess-1");
        presenceEventListener.handleWebSocketConnectListener(event1);

        SessionDisconnectEvent disconnect1 = createDisconnectEvent("user1", "uid-1", "sess-1");
        presenceEventListener.handleWebSocketDisconnectListener(disconnect1);

        Set<String> onlineUsers = presenceEventListener.getOnlineUsers();
        assertFalse(onlineUsers.contains("user1"));
        assertEquals(0, onlineUsers.size());

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/chat/presence"), payloadCaptor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> offlinePayload = (Map<String, Object>) payloadCaptor.getAllValues().get(1);
        assertEquals("PRESENCE_UPDATE", offlinePayload.get("type"));
        assertEquals("OFFLINE", offlinePayload.get("status"));
        assertEquals("user1", offlinePayload.get("username"));
    }
}
