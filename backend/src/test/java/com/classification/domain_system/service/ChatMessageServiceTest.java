package com.classification.domain_system.service;

import com.classification.domain_system.entity.ChatMessage;
import com.classification.domain_system.entity.ChatMessageRoom;
import com.classification.domain_system.entity.ChatMessageRoomMember;
import com.classification.domain_system.repository.ChatMessageRepository;
import com.classification.domain_system.repository.ChatMessageRoomMemberRepository;
import com.classification.domain_system.repository.ChatMessageRoomRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.websocket.WebSocketPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock private ChatMessageRoomRepository roomRepository;
    @Mock private ChatMessageRoomMemberRepository memberRepository;
    @Mock private ChatMessageRepository messageRepository;
    @Mock private UserRepository userRepository;
    @Mock private WebSocketPublisher webSocketPublisher;

    @InjectMocks
    private ChatMessageService chatMessageService;

    @Test
    @DisplayName("성공 - 방 생성 및 멤버 등록이 정상 작동한다")
    void createRoom_Success() {
        String creatorId = "user-1";
        List<String> members = List.of("user-2", "user-3");

        given(roomRepository.save(any(ChatMessageRoom.class))).willAnswer(inv -> {
            ChatMessageRoom r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ChatMessageRoom room = chatMessageService.createRoom("개발팀 그룹채팅", true, creatorId, members);

        assertThat(room).isNotNull();
        assertThat(room.getName()).isEqualTo("개발팀 그룹채팅");
        assertThat(room.getIsGroup()).isTrue();
        verify(roomRepository).save(any(ChatMessageRoom.class));
    }

    @Test
    @DisplayName("성공 - 메시지 전송 시 소켓 브로드캐스팅 및 방 최신 메시지가 갱신된다")
    void sendMessage_Success() {
        UUID roomId = UUID.randomUUID();
        String senderId = "user-1";

        ChatMessageRoom room = new ChatMessageRoom();
        room.setId(roomId);
        room.setName("테스트방");

        given(roomRepository.findById(roomId)).willReturn(Optional.of(room));
        given(messageRepository.save(any(ChatMessage.class))).willAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        ChatMessageService.ChatMessageDto msg = chatMessageService.sendMessage(roomId, senderId, "TEXT", "안녕하세요!", null, null, null);

        assertThat(msg).isNotNull();
        assertThat(msg.getContent()).isEqualTo("안녕하세요!");
        assertThat(room.getLastMessage()).contains("안녕하세요!");
        verify(webSocketPublisher).publishToRoom(eq(roomId), any());
    }

    @Test
    @DisplayName("성공 - 7일 경과 메시지 자동 정제 배치가 수행된다")
    void cleanupOldMessages_Success() {
        given(messageRepository.deleteMessagesOlderThan(any(LocalDateTime.class))).willReturn(15);

        int deleted = chatMessageService.cleanupOldMessages();

        assertThat(deleted).isEqualTo(15);
        verify(messageRepository).deleteMessagesOlderThan(any(LocalDateTime.class));
    }
}
