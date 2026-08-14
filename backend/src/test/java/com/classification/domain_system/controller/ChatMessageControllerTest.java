package com.classification.domain_system.controller;

import com.classification.domain_system.context.AuthContext;
import com.classification.domain_system.entity.ChatMessageRoom;
import com.classification.domain_system.exception.CustomAccessDeniedException;
import com.classification.domain_system.service.ChatMessageService;
import com.classification.domain_system.websocket.PresenceEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private AuthContext authContext;

    @Mock
    private PresenceEventListener presenceEventListener;

    @InjectMocks
    private ChatMessageController chatMessageController;

    @Test
    @DisplayName("온라인 사용자 목록 조회 성공")
    void testGetOnlineUsers() {
        Set<String> mockUsers = Set.of("user1", "user2");
        when(presenceEventListener.getOnlineUsers()).thenReturn(mockUsers);

        ResponseEntity<Set<String>> response = chatMessageController.getOnlineUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("user1"));
    }

    @Test
    @DisplayName("로그인 사용자의 대화방 목록 조회 성공")
    void testGetMyRooms_Success() {
        String userId = "user123";
        when(authContext.getUserId()).thenReturn(userId);

        ChatMessageRoom room = new ChatMessageRoom();
        room.setId(UUID.randomUUID());
        room.setName("일반 대화방");
        room.setIsGroup(false);

        when(chatMessageService.getUserRooms(userId)).thenReturn(List.of(room));

        ResponseEntity<List<ChatMessageRoom>> response = chatMessageController.getMyRooms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("일반 대화방", response.getBody().get(0).getName());
    }

    @Test
    @DisplayName("대화방 생성 성공")
    void testCreateRoom_Success() {
        String userId = "creator123";
        when(authContext.getUserId()).thenReturn(userId);

        ChatMessageController.CreateRoomRequest req = new ChatMessageController.CreateRoomRequest();
        req.setRoomName("새 프로젝트 대화방");
        req.setIsGroup(true);
        req.setMemberUserIds(List.of("user1", "user2"));

        ChatMessageRoom created = new ChatMessageRoom();
        created.setId(UUID.randomUUID());
        created.setName("새 프로젝트 대화방");
        created.setIsGroup(true);

        when(chatMessageService.createRoom(eq("새 프로젝트 대화방"), eq(true), eq(userId), anyList()))
                .thenReturn(created);

        ResponseEntity<ChatMessageRoom> response = chatMessageController.createRoom(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("새 프로젝트 대화방", response.getBody().getName());
    }

    @Test
    @DisplayName("대화방 메시지 내역 조회 성공")
    void testGetRoomMessages_Success() {
        String userId = "user123";
        UUID roomId = UUID.randomUUID();
        when(authContext.getUserId()).thenReturn(userId);

        ChatMessageService.ChatMessageDto msgDto = new ChatMessageService.ChatMessageDto();
        msgDto.setContent("안녕하세요");
        when(chatMessageService.getRoomMessages(roomId, userId)).thenReturn(List.of(msgDto));

        ResponseEntity<List<ChatMessageService.ChatMessageDto>> response = chatMessageController.getRoomMessages(roomId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("안녕하세요", response.getBody().get(0).getContent());
    }

    @Test
    @DisplayName("대화방 메시지 읽음 처리 성공")
    void testMarkRoomAsRead_Success() {
        String userId = "user123";
        UUID roomId = UUID.randomUUID();
        when(authContext.getUserId()).thenReturn(userId);

        ResponseEntity<?> response = chatMessageController.markRoomAsRead(roomId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatMessageService).markRoomAsRead(roomId, userId);
    }

    @Test
    @DisplayName("대화방 나가기(Leave) 성공")
    void testLeaveRoom_Success() {
        String userId = "user123";
        UUID roomId = UUID.randomUUID();
        when(authContext.getUserId()).thenReturn(userId);

        ResponseEntity<?> response = chatMessageController.leaveRoom(roomId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatMessageService).leaveRoom(roomId, userId);
    }

    @Test
    @DisplayName("대화방 삭제 성공")
    void testDeleteRoom_Success() {
        String userId = "user123";
        UUID roomId = UUID.randomUUID();
        when(authContext.getUserId()).thenReturn(userId);

        ResponseEntity<?> response = chatMessageController.deleteRoom(roomId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatMessageService).deleteRoom(roomId, userId);
    }

    @Test
    @DisplayName("미인증 사용자 요청 시 CustomAccessDeniedException 예외 발생")
    void testUnauthenticatedUser_ThrowsException() {
        when(authContext.getUserId()).thenReturn(null);

        assertThrows(CustomAccessDeniedException.class, () -> {
            chatMessageController.getMyRooms();
        });
    }
}
