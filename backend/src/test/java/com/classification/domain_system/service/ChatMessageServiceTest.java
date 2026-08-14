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

    @Test
    @DisplayName("성공 - 방 참여자 목록 조회 시 UUID와 username으로 중복 저장된 경우 사용자명을 정상 매핑하고 중복 제거하여 반환한다")
    void getRoomMembers_DedupAndResolveUser() {
        UUID roomId = UUID.randomUUID();
        ChatMessageRoom room = new ChatMessageRoom();
        room.setId(roomId);
        room.setName("테스트방");

        com.classification.domain_system.entity.User superAdmin = new com.classification.domain_system.entity.User();
        superAdmin.setId("user-uuid-1234");
        superAdmin.setUsername("superadmin");
        superAdmin.setRole("ROLE_ADMIN");

        // 동일 사용자가 Keycloak UUID("user-uuid-1234")와 로컬 username("superadmin")으로 2개 저장된 상태 시뮬레이션
        ChatMessageRoomMember m1 = new ChatMessageRoomMember();
        m1.setRoom(room);
        m1.setUserId("user-uuid-1234");
        m1.setJoinedAt(LocalDateTime.now().minusHours(1));

        ChatMessageRoomMember m2 = new ChatMessageRoomMember();
        m2.setRoom(room);
        m2.setUserId("superadmin");
        m2.setJoinedAt(LocalDateTime.now());

        given(memberRepository.findByRoomId(roomId)).willReturn(List.of(m1, m2));
        given(userRepository.findById("user-uuid-1234")).willReturn(Optional.of(superAdmin));
        given(userRepository.findById("superadmin")).willReturn(Optional.empty());
        given(userRepository.findByUsername("superadmin")).willReturn(Optional.of(superAdmin));

        List<ChatMessageService.RoomMemberDto> members = chatMessageService.getRoomMembers(roomId);

        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUsername()).isEqualTo("superadmin");
        assertThat(members.get(0).getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("성공 - 방 생성 시 생성자 및 멤버 목록의 ID/Username이 DB 사용자로 정규화되고 중복이 방지된다")
    void createRoom_ResolveUser() {
        String creatorUsername = "superadmin";
        List<String> rawMembers = List.of("superadmin", "user-uuid-1234");

        com.classification.domain_system.entity.User superAdmin = new com.classification.domain_system.entity.User();
        superAdmin.setId("user-uuid-1234");
        superAdmin.setUsername("superadmin");
        superAdmin.setRole("ROLE_ADMIN");

        given(userRepository.findById("superadmin")).willReturn(Optional.empty());
        given(userRepository.findByUsername("superadmin")).willReturn(Optional.of(superAdmin));
        given(userRepository.findById("user-uuid-1234")).willReturn(Optional.of(superAdmin));

        given(roomRepository.save(any(ChatMessageRoom.class))).willAnswer(inv -> {
            ChatMessageRoom r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ChatMessageRoom room = chatMessageService.createRoom("신규방", true, creatorUsername, rawMembers);

        assertThat(room).isNotNull();
        assertThat(room.getCreatedBy()).isEqualTo("user-uuid-1234");
        // superadmin이 creator이자 멤버 2개로 들어왔지만 단 1개의 member로만 저장되어야 함
        verify(memberRepository, org.mockito.Mockito.times(1)).save(any(ChatMessageRoomMember.class));
    }
}
