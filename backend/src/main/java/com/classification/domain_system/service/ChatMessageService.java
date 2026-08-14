package com.classification.domain_system.service;

import com.classification.domain_system.entity.User;
import com.classification.domain_system.entity.ChatMessage;
import com.classification.domain_system.entity.ChatMessageRoom;
import com.classification.domain_system.entity.ChatMessageRoomMember;
import com.classification.domain_system.repository.ChatMessageRepository;
import com.classification.domain_system.repository.ChatMessageRoomMemberRepository;
import com.classification.domain_system.repository.ChatMessageRoomRepository;
import com.classification.domain_system.repository.UserRepository;
import com.classification.domain_system.websocket.WebSocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    @Value("${chat.retention-days}")
    private int retentionDays = 7;

    private final ChatMessageRoomRepository roomRepository;
    private final ChatMessageRoomMemberRepository memberRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WebSocketPublisher webSocketPublisher;
    private final SseNotificationService sseNotificationService;

    public String resolveUserId(String idOrUsername) {
        if (idOrUsername == null || idOrUsername.isBlank()) return idOrUsername;
        var userOpt = userRepository.findById(idOrUsername);
        if (userOpt.isPresent() && userOpt.get().getId() != null) {
            return userOpt.get().getId();
        }
        var userByName = userRepository.findByUsername(idOrUsername);
        if (userByName.isPresent() && userByName.get().getId() != null) {
            return userByName.get().getId();
        }
        return idOrUsername;
    }

    private boolean isUuidString(String str) {
        if (str == null) return false;
        return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    @Transactional
    public ChatMessageRoom createRoom(String roomName, Boolean isGroup, String creatorId, List<String> memberUserIds) {
        String resolvedCreatorId = resolveUserId(creatorId);
        ChatMessageRoom room = new ChatMessageRoom();
        room.setName(roomName);
        room.setIsGroup(isGroup != null && isGroup);
        room.setCreatedBy(resolvedCreatorId);
        room.setLastMessage("채팅방이 생성되었습니다.");
        room.setLastMessageAt(LocalDateTime.now());
        ChatMessageRoom savedRoom = roomRepository.save(room);

        List<String> allMembers = new ArrayList<>();
        if (resolvedCreatorId != null && !resolvedCreatorId.isBlank()) {
            allMembers.add(resolvedCreatorId);
        }
        if (memberUserIds != null) {
            for (String uid : memberUserIds) {
                String resolvedMemberId = resolveUserId(uid);
                if (resolvedMemberId != null && !allMembers.contains(resolvedMemberId)) {
                    allMembers.add(resolvedMemberId);
                }
            }
        }

        for (String uid : allMembers) {
            ChatMessageRoomMember member = new ChatMessageRoomMember();
            member.setRoom(savedRoom);
            member.setUserId(uid);
            memberRepository.save(member);
        }

        return savedRoom;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageRoomMember> getRoomMembersRaw(UUID roomId) {
        return memberRepository.findByRoomId(roomId);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RoomMemberDto {
        private String userId;
        private String username;
        private String role;
        private LocalDateTime joinedAt;
    }

    @Transactional(readOnly = true)
    public List<RoomMemberDto> getRoomMembers(UUID roomId) {
        List<ChatMessageRoomMember> members = memberRepository.findByRoomId(roomId);
        List<RoomMemberDto> dtos = new ArrayList<>();
        java.util.Set<String> seenUsernames = new java.util.HashSet<>();

        for (ChatMessageRoomMember m : members) {
            String uid = m.getUserId();
            String username = null;
            String role = "ROLE_USER";
            var userOpt = userRepository.findById(uid);
            if (userOpt.isPresent()) {
                username = userOpt.get().getUsername();
                role = userOpt.get().getRole();
            } else {
                var userByUsername = userRepository.findByUsername(uid);
                if (userByUsername.isPresent()) {
                    username = userByUsername.get().getUsername();
                    role = userByUsername.get().getRole();
                }
            }

            if (username == null || username.isBlank()) {
                if (isUuidString(uid)) {
                    username = "USER-" + uid.substring(0, Math.min(uid.length(), 8));
                } else {
                    username = uid;
                }
            }

            String dedupKey = username.toLowerCase();
            if (!seenUsernames.add(dedupKey)) {
                continue;
            }

            dtos.add(new RoomMemberDto(uid, username, role, m.getJoinedAt()));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageRoom> getUserRooms(String userId) {
        return roomRepository.findRoomsByUserId(userId);
    }

    @Transactional
    public void leaveRoom(UUID roomId, String userId) {
        if (roomId == null || userId == null) return;
        
        // Find member by username or id
        Optional<ChatMessageRoomMember> memberOpt = memberRepository.findByRoomIdAndUserId(roomId, userId);
        if (memberOpt.isEmpty()) {
            User u = userRepository.findByUsername(userId).orElse(null);
            if (u != null) {
                memberOpt = memberRepository.findByRoomIdAndUserId(roomId, u.getId());
                if (memberOpt.isPresent()) {
                    memberRepository.deleteByRoomIdAndUserId(roomId, u.getId());
                }
            }
        } else {
            memberRepository.deleteByRoomIdAndUserId(roomId, userId);
        }
        
        // 브로드캐스트 (나간 사람의 ID를 content로 전달, 프론트에서 다국어 처리)
        sendMessage(roomId, "SYSTEM", "LEAVE", userId, null, null, null);
    }

    @Transactional
    public void deleteRoom(UUID roomId, String userId) {
        if (roomId == null || userId == null) return;
        
        ChatMessageRoom room = roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        // Validate if the user is the creator
        String uId = userId;
        User u = userRepository.findByUsername(userId).orElse(null);
        if (u != null && !userId.equals(room.getCreatedBy())) {
            uId = u.getId();
        }
        
        if (!room.getCreatedBy().equals(userId) && (u != null && !room.getCreatedBy().equals(u.getId()))) {
            throw new IllegalStateException("Only the room creator can delete this room.");
        }
        
        messageRepository.deleteByRoomId(roomId);
        memberRepository.deleteByRoomId(roomId);
        roomRepository.delete(room);
    }

    @Transactional
    public void delegateCreator(UUID roomId, String currentUserId, String newCreatorId) {
        if (roomId == null || currentUserId == null || newCreatorId == null) return;
        
        ChatMessageRoom room = roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        // Validate if the user is the creator
        String uId = currentUserId;
        User u = userRepository.findByUsername(currentUserId).orElse(null);
        if (u != null && !currentUserId.equals(room.getCreatedBy())) {
            uId = u.getId();
        }
        
        if (!room.getCreatedBy().equals(currentUserId) && (u != null && !room.getCreatedBy().equals(u.getId()))) {
            throw new IllegalStateException("Only the room creator can delegate the creator role.");
        }
        
        room.setCreatedBy(newCreatorId);
        roomRepository.save(room);
    }

    @Transactional
    public void inviteMembers(UUID roomId, String requesterId, List<String> newMemberIds, Integer pastMessageHours) {
        if (roomId == null || requesterId == null || newMemberIds == null || newMemberIds.isEmpty()) return;
        
        ChatMessageRoom room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));
            
        // Check if requester is in the room
        boolean isRequesterInRoom = memberRepository.findByRoomId(roomId).stream()
            .anyMatch(m -> m.getUserId().equals(requesterId));
            
        if (!isRequesterInRoom && !room.getCreatedBy().equals(requesterId)) {
            // Also check UUID mapping
            User u = userRepository.findByUsername(requesterId).orElse(null);
            boolean isUuidInRoom = u != null && memberRepository.findByRoomId(roomId).stream()
                .anyMatch(m -> m.getUserId().equals(u.getId()));
                
            if (!isUuidInRoom && (u == null || !room.getCreatedBy().equals(u.getId()))) {
                throw new IllegalStateException("Only room members can invite others.");
            }
        }
        
        // Add new members if they aren't already in the room
        List<ChatMessageRoomMember> currentMembers = memberRepository.findByRoomId(roomId);
        int hoursToMinus = pastMessageHours != null ? pastMessageHours : 0;
        if (hoursToMinus > 48) hoursToMinus = 48;
        LocalDateTime joinedTime = java.time.LocalDateTime.now().minusHours(hoursToMinus);
        
        for (String rawMemberId : newMemberIds) {
            String newMemberId = resolveUserId(rawMemberId);
            boolean alreadyExists = currentMembers.stream().anyMatch(m -> {
                String existingUid = m.getUserId();
                return existingUid.equals(newMemberId) || resolveUserId(existingUid).equals(newMemberId);
            });
            if (!alreadyExists) {
                ChatMessageRoomMember newMember = new ChatMessageRoomMember();
                newMember.setRoom(room);
                newMember.setUserId(newMemberId);
                newMember.setJoinedAt(joinedTime);
                newMember.setLastReadAt(java.time.LocalDateTime.now());
                memberRepository.save(newMember);
                
                // 브로드캐스트 (초대된 사람의 ID를 content로 전달)
                sendMessage(roomId, "SYSTEM", "JOIN", newMemberId, null, null, null);
            }
        }
    }

    @Transactional
    public void kickMember(UUID roomId, String requesterId, String targetUserId) {
        if (roomId == null || requesterId == null || targetUserId == null) return;
        
        ChatMessageRoom room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));
            
        // Validate if the user is the creator
        String uId = requesterId;
        User u = userRepository.findByUsername(requesterId).orElse(null);
        if (u != null && !requesterId.equals(room.getCreatedBy())) {
            uId = u.getId();
        }
        
        if (!room.getCreatedBy().equals(requesterId) && (u != null && !room.getCreatedBy().equals(u.getId()))) {
            throw new IllegalStateException("Only the room creator can kick members.");
        }
        
        // Cannot kick yourself
        if (requesterId.equals(targetUserId) || (u != null && u.getId().equals(targetUserId))) {
            throw new IllegalArgumentException("Cannot kick yourself.");
        }
        
        memberRepository.deleteByRoomIdAndUserId(roomId, targetUserId);
        
        // 브로드캐스트 (강퇴된 사람의 ID를 content로 전달)
        sendMessage(roomId, "SYSTEM", "LEAVE", targetUserId, null, null, null);
    }

    @lombok.Data
    public static class ChatMessageDto {
        private UUID id;
        private UUID roomId;
        private String senderId;
        private String senderName;
        private String messageType;
        private String content;
        private String fileUrl;
        private String fileName;
        private Long fileSize;
        private LocalDateTime createdAt;
        private int unreadCount;
    }

    @Transactional
    public void markRoomAsRead(UUID roomId, String userId) {
        if (roomId == null || userId == null) return;
        Optional<ChatMessageRoomMember> memberOpt = memberRepository.findByRoomIdAndUserId(roomId, userId);
        if (memberOpt.isEmpty()) {
            User u = userRepository.findByUsername(userId).orElse(null);
            if (u != null) {
                memberOpt = memberRepository.findByRoomIdAndUserId(roomId, u.getId());
            }
        }

        if (memberOpt.isPresent()) {
            ChatMessageRoomMember m = memberOpt.get();
            // 시간 오차 방지를 위해 plusSeconds(1)로 lastReadAt 지정
            m.setLastReadAt(LocalDateTime.now().plusSeconds(1));
            memberRepository.save(m);

            List<ChatMessageRoomMember> allMembers = memberRepository.findByRoomId(roomId);
            Map<String, Object> readEvent = Map.of(
                    "eventType", "ROOM_READ",
                    "roomId", roomId.toString(),
                    "userId", userId
            );

            // 소켓 및 SSE로 읽음 상태 업데이트 푸시
            if (webSocketPublisher != null) {
                try {
                    webSocketPublisher.publishToRoom(roomId, readEvent);
                } catch (Exception ignored) {}
            }

            for (ChatMessageRoomMember mem : allMembers) {
                if (!mem.getUserId().equals(userId) && !mem.getUserId().equals(m.getUserId())) {
                    if (sseNotificationService != null) {
                        try { sseNotificationService.sendNotification(mem.getUserId(), readEvent); } catch (Exception ignored) {}
                    }
                    if (webSocketPublisher != null) {
                        try { webSocketPublisher.publishNotification(mem.getUserId(), readEvent); } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public long getTotalUnreadCount(String userId) {
        List<ChatMessageRoom> userRooms = roomRepository.findRoomsByUserId(userId);
        long totalUnread = 0;
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        for (ChatMessageRoom room : userRooms) {
            var memberOpt = memberRepository.findByRoomIdAndUserId(room.getId(), userId);
            LocalDateTime lastRead = memberOpt.isPresent() && memberOpt.get().getLastReadAt() != null 
                    ? memberOpt.get().getLastReadAt() 
                    : sevenDaysAgo;
            List<ChatMessage> msgs = messageRepository.findByRoomIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(room.getId(), lastRead);
            totalUnread += msgs.stream().filter(m -> !userId.equals(m.getSenderId())).count();
        }
        return totalUnread;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getRoomMessages(UUID roomId, String userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<ChatMessageRoomMember> members = memberRepository.findByRoomId(roomId);
        
        LocalDateTime userJoinedAt = sevenDaysAgo;
        if (userId != null) {
            for (ChatMessageRoomMember m : members) {
                if (m.getUserId().equals(userId)) {
                    userJoinedAt = m.getJoinedAt() != null ? m.getJoinedAt() : sevenDaysAgo;
                    break;
                }
            }
        }
        
        LocalDateTime fetchAfter = userJoinedAt.isAfter(sevenDaysAgo) ? userJoinedAt : sevenDaysAgo;
        
        List<ChatMessage> msgs = messageRepository.findByRoomIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(roomId, fetchAfter);

        List<ChatMessageDto> dtos = new ArrayList<>();
        for (ChatMessage msg : msgs) {
            ChatMessageDto dto = new ChatMessageDto();
            dto.setId(msg.getId());
            dto.setRoomId(msg.getRoomId());
            dto.setSenderId(msg.getSenderId());
            dto.setSenderName(msg.getSenderName());
            dto.setMessageType(msg.getMessageType());
            dto.setContent(msg.getContent());
            dto.setFileUrl(msg.getFileUrl());
            dto.setFileName(msg.getFileName());
            dto.setFileSize(msg.getFileSize());
            dto.setCreatedAt(msg.getCreatedAt());

            // unreadCount 계산 (나 이외의 멤버 중 msg.createdAt > member.lastReadAt 인 사람 수)
            int unread = 0;
            for (ChatMessageRoomMember m : members) {
                if (m.getUserId().equals(msg.getSenderId()) || m.getUserId().equals(msg.getSenderName())) continue;
                if (m.getLastReadAt() == null || m.getLastReadAt().isBefore(msg.getCreatedAt().minusSeconds(1))) {
                    unread++;
                }
            }
            dto.setUnreadCount(unread);
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public ChatMessageDto sendMessage(UUID roomId, String senderId, String messageType, String content, String fileUrl, String fileName, Long fileSize) {
        ChatMessageRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found: " + roomId));

        String senderName = userRepository.findById(senderId)
                .map(com.classification.domain_system.entity.User::getUsername)
                .orElse(senderId);

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setSenderId(senderId);
        msg.setSenderName(senderName);
        msg.setMessageType(messageType != null ? messageType : "TEXT");
        msg.setContent(content);
        msg.setFileUrl(fileUrl);
        msg.setFileName(fileName);
        msg.setFileSize(fileSize);
        msg.setCreatedAt(LocalDateTime.now());

        ChatMessage saved = messageRepository.save(msg);

        // 메시지 보낸 사람은 방을 최신으로 읽은 상태로 갱신
        markRoomAsRead(roomId, senderId);

        String summary = content;
        if ("IMAGE".equals(messageType)) summary = "[이미지]";
        else if ("FILE".equals(messageType)) summary = "[파일] " + (fileName != null ? fileName : "");
        else if ("EMOJI".equals(messageType)) summary = content;

        room.setLastMessage((senderName != null ? senderName + ": " : "") + summary);
        room.setLastMessageAt(LocalDateTime.now());
        roomRepository.save(room);

        List<ChatMessageRoomMember> members = memberRepository.findByRoomId(roomId);
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(saved.getId());
        dto.setRoomId(saved.getRoomId());
        dto.setSenderId(saved.getSenderId());
        dto.setSenderName(saved.getSenderName());
        dto.setMessageType(saved.getMessageType());
        dto.setContent(saved.getContent());
        dto.setFileUrl(saved.getFileUrl());
        dto.setFileName(saved.getFileName());
        dto.setFileSize(saved.getFileSize());
        dto.setCreatedAt(saved.getCreatedAt());
        int unread = 0;
        for (ChatMessageRoomMember m : members) {
            if (m.getUserId().equals(senderId) || m.getUserId().equals(senderName)) continue;
            if (m.getLastReadAt() == null || m.getLastReadAt().isBefore(saved.getCreatedAt().minusSeconds(1))) {
                unread++;
            }
        }
        dto.setUnreadCount(unread);

        // STOMP WebSocket으로 방 구독자들에게 실시간 브로드캐스트
        if (webSocketPublisher != null) {
            try {
                webSocketPublisher.publishToRoom(roomId, dto);
            } catch (Exception e) {
                log.error("Failed to publish chat message via WebSocket", e);
            }
        }

        // 방 멤버 개인 채널(SSE/WebSocket)로 실시간 CHAT_MESSAGE 알림 발송
        for (ChatMessageRoomMember m : members) {
            if (!m.getUserId().equals(senderId)) {
                Map<String, Object> chatNotif = Map.of(
                        "eventType", "CHAT_MESSAGE",
                        "roomId", roomId.toString(),
                        "senderId", senderId,
                        "senderName", senderName != null ? senderName : senderId,
                        "content", summary
                );
                if (sseNotificationService != null) {
                    try { sseNotificationService.sendNotification(m.getUserId(), chatNotif); } catch (Exception ignored) {}
                }
                if (webSocketPublisher != null) {
                    try { webSocketPublisher.publishNotification(m.getUserId(), chatNotif); } catch (Exception ignored) {}
                }
            }
        }

        return dto;
    }

    @Transactional
    public void deleteMessage(UUID messageId, String userId) {
        if (messageId == null) return;
        ChatMessage msg = messageRepository.findById(messageId).orElse(null);
        if (msg == null) return;

        UUID roomId = msg.getRoomId();

        // 물리 파일 삭제 (fileUrl이 존재하는 경우)
        if (msg.getFileUrl() != null && msg.getFileUrl().startsWith("/api/chat/files/")) {
            try {
                String fileName = msg.getFileUrl().replace("/api/chat/files/", "");
                java.io.File physicalFile = new java.io.File(System.getProperty("user.dir") + "/uploads/chat", fileName);
                if (physicalFile.exists()) {
                    physicalFile.delete();
                }
            } catch (Exception ignored) {}
        }

        // DB에서 해당 메시지 레코드 개별 명시적 완전 삭제
        messageRepository.delete(msg);

        Map<String, Object> deleteEvent = Map.of(
                "eventType", "MESSAGE_DELETED",
                "roomId", roomId.toString(),
                "messageId", messageId.toString()
        );

        if (webSocketPublisher != null) {
            try {
                webSocketPublisher.publishToRoom(roomId, deleteEvent);
            } catch (Exception ignored) {}
        }

        List<ChatMessageRoomMember> members = memberRepository.findByRoomId(roomId);
        for (ChatMessageRoomMember m : members) {
            if (!m.getUserId().equals(userId)) {
                if (sseNotificationService != null) {
                    try { sseNotificationService.sendNotification(m.getUserId(), deleteEvent); } catch (Exception ignored) {}
                }
                if (webSocketPublisher != null) {
                    try { webSocketPublisher.publishNotification(m.getUserId(), deleteEvent); } catch (Exception ignored) {}
                }
            }
        }
    }

    /**
     * 지정된 보존 기간(yml: chat.retention-days) 경과 메시지 및 첨부 물리 파일 자동 정제 배치 스케줄러 (매일 새벽 3시 실행)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public int cleanupOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

        // 보존 기간 경과 메시지에 포함된 물리 첨부파일 완전 삭제
        try {
            List<ChatMessage> oldMessages = messageRepository.findByCreatedAtBefore(cutoff);
            if (oldMessages != null && !oldMessages.isEmpty()) {
                for (ChatMessage msg : oldMessages) {
                    if (msg.getFileUrl() != null && msg.getFileUrl().startsWith("/api/chat/files/")) {
                        String fileName = msg.getFileUrl().replace("/api/chat/files/", "");
                        java.io.File physicalFile = new java.io.File(System.getProperty("user.dir") + "/uploads/chat", fileName);
                        if (physicalFile.exists()) {
                            boolean deleted = physicalFile.delete();
                            log.info("[CHAT_CLEANUP_BATCH] Deleted physical file: {} (Success: {})", fileName, deleted);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("[CHAT_CLEANUP_BATCH] Failed to delete physical chat files", e);
        }

        int deletedCount = messageRepository.deleteMessagesOlderThan(cutoff);
        log.info("[CHAT_CLEANUP_BATCH] Purged {} chat messages older than {} days (Cutoff: {})", deletedCount, retentionDays, cutoff);
        return deletedCount;
    }
}
